package com.example.domain

import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity

data class AccountingAuditIssue(
    val id: String,
    val severity: String, // "CRITICAL", "WARNING", "INFO"
    val category: String, // "GST", "INVOICE", "STOCK", "BALANCE", "DUPLICATE"
    val title: String,
    val voucherOrItemRef: String,
    val problem: String,
    val expected: String,
    val recorded: String,
    val suggestedAction: String
)

object AiAccountingChecker {

    fun runFullAudit(
        vouchers: List<VoucherEntity>,
        ledgers: List<LedgerEntity>,
        items: List<ItemEntity>
    ): List<AccountingAuditIssue> {
        val issues = mutableListOf<AccountingAuditIssue>()
        var issueCounter = 1

        // 1. Check for Duplicate Invoice Numbers
        val invoiceMap = mutableMapOf<String, MutableList<VoucherEntity>>()
        for (v in vouchers.filter { it.status == "ACTIVE" }) {
            if (v.voucherNumber.isNotBlank()) {
                val list = invoiceMap.getOrPut(v.voucherNumber) { mutableListOf() }
                list.add(v)
            }
        }
        for ((vNo, vList) in invoiceMap) {
            if (vList.size > 1) {
                issues.add(
                    AccountingAuditIssue(
                        id = "ISSUE-${issueCounter++}",
                        severity = "CRITICAL",
                        category = "DUPLICATE",
                        title = "Duplicate Invoice Number Detected",
                        voucherOrItemRef = "Voucher # $vNo",
                        problem = "Found ${vList.size} active transactions with the identical invoice/voucher number '$vNo'.",
                        expected = "Each invoice number must be strictly unique within the financial year.",
                        recorded = "${vList.size} duplicate entries recorded.",
                        suggestedAction = "Cancel or re-number the duplicated invoice to avoid tax compliance errors and audit penalties under Section 31 of the CGST Act."
                    )
                )
            }
        }

        // 2. Check for GST Calculation Discrepancies
        for (v in vouchers.filter { it.status == "ACTIVE" && it.voucherType in listOf("SALES", "PURCHASE") }) {
            if (v.taxableAmount > 0) {
                val calculatedGst = v.cgstAmount + v.sgstAmount + v.igstAmount
                val reportedTotal = v.taxableAmount + v.totalGst
                val diff = Math.abs(reportedTotal - v.grandTotal)
                if (diff > 1.0) {
                    issues.add(
                        AccountingAuditIssue(
                            id = "ISSUE-${issueCounter++}",
                            severity = "CRITICAL",
                            category = "GST",
                            title = "GST Math Mismatch",
                            voucherOrItemRef = "Invoice: ${v.voucherNumber}",
                            problem = "Grand Total does not reconcile with Taxable Value + Total GST.",
                            expected = "₹${String.format("%.2f", reportedTotal)}",
                            recorded = "₹${String.format("%.2f", v.grandTotal)}",
                            suggestedAction = "Review invoice tax calculation before filing GSTR-1/GSTR-3B."
                        )
                    )
                }

                // Intra-state vs Inter-state check (CGST+SGST both present or IGST only)
                if ((v.cgstAmount > 0 && v.sgstAmount == 0.0) || (v.sgstAmount > 0 && v.cgstAmount == 0.0)) {
                    issues.add(
                        AccountingAuditIssue(
                            id = "ISSUE-${issueCounter++}",
                            severity = "WARNING",
                            category = "GST",
                            title = "Asymmetrical Intra-state GST",
                            voucherOrItemRef = "Invoice: ${v.voucherNumber}",
                            problem = "CGST and SGST must always be charged equally at half the GST rate on intra-state sales.",
                            expected = "CGST = SGST",
                            recorded = "CGST: ₹${v.cgstAmount}, SGST: ₹${v.sgstAmount}",
                            suggestedAction = "Correct tax breakdown to ensure equal CGST and SGST splits."
                        )
                    )
                }
            }
        }

        // 3. Check for Negative Stock
        for (item in items.filter { !it.isDeleted }) {
            if (item.currentStock < 0) {
                issues.add(
                    AccountingAuditIssue(
                        id = "ISSUE-${issueCounter++}",
                        severity = "CRITICAL",
                        category = "STOCK",
                        title = "Negative Stock Balance",
                        voucherOrItemRef = "Item: ${item.name} (${item.itemCode})",
                        problem = "Current stock is negative (${item.currentStock} ${item.unit}). Items were billed before purchase or stock-in was recorded.",
                        expected = "Stock >= 0",
                        recorded = "${item.currentStock} ${item.unit}",
                        suggestedAction = "Record backdated Purchase Invoice or perform Stock Adjustment to bring inventory back to actual physical quantity."
                    )
                )
            } else if (item.currentStock <= item.minStockLevel) {
                issues.add(
                    AccountingAuditIssue(
                        id = "ISSUE-${issueCounter++}",
                        severity = "WARNING",
                        category = "STOCK",
                        title = "Low Stock Warning",
                        voucherOrItemRef = "Item: ${item.name}",
                        problem = "Item stock (${item.currentStock} ${item.unit}) has dropped below reorder threshold (${item.minStockLevel} ${item.unit}).",
                        expected = "Stock > ${item.minStockLevel}",
                        recorded = "${item.currentStock} ${item.unit}",
                        suggestedAction = "Issue a Purchase Order to supplier to prevent stockout and missed sales."
                    )
                )
            }
        }

        // 4. Check for Unmapped or Missing Ledgers
        val ledgerIds = ledgers.map { it.id }.toSet()
        for (v in vouchers.filter { it.status == "ACTIVE" }) {
            if (!ledgerIds.contains(v.partyLedgerId)) {
                issues.add(
                    AccountingAuditIssue(
                        id = "ISSUE-${issueCounter++}",
                        severity = "CRITICAL",
                        category = "BALANCE",
                        title = "Orphaned Voucher / Missing Ledger",
                        voucherOrItemRef = "Voucher: ${v.voucherNumber}",
                        problem = "Voucher points to deleted or non-existent party ledger ID (${v.partyLedgerId}).",
                        expected = "Valid mapped Ledger in Chart of Accounts",
                        recorded = "Missing Party Ledger",
                        suggestedAction = "Remap the voucher to an active party account in the Ledger Master."
                    )
                )
            }
        }

        // 5. Check for Unusual Transaction Amounts (> 3x average)
        val activeSales = vouchers.filter { it.status == "ACTIVE" && it.voucherType == "SALES" }
        if (activeSales.size >= 3) {
            val avg = activeSales.map { it.grandTotal }.average()
            for (s in activeSales) {
                if (s.grandTotal > avg * 4 && s.grandTotal > 50000.0) {
                    issues.add(
                        AccountingAuditIssue(
                            id = "ISSUE-${issueCounter++}",
                            severity = "INFO",
                            category = "INVOICE",
                            title = "High Value Transaction Anomaly",
                            voucherOrItemRef = "Invoice: ${s.voucherNumber}",
                            problem = "Invoice value of ₹${String.format("%.2f", s.grandTotal)} is significantly higher than historical average of ₹${String.format("%.2f", avg)}.",
                            expected = "Typical order around ₹${String.format("%.2f", avg)}",
                            recorded = "₹${String.format("%.2f", s.grandTotal)}",
                            suggestedAction = "Verify quantity and decimal rates to rule out inadvertent extra zeros."
                        )
                    )
                }
            }
        }

        // 6. Check for Missing GSTIN on high-value B2B parties
        for (l in ledgers.filter { it.partyType in listOf("CUSTOMER", "SUPPLIER") && !it.isDeleted }) {
            if (l.currentBalance > 50000.0 && l.gstin.isBlank()) {
                issues.add(
                    AccountingAuditIssue(
                        id = "ISSUE-${issueCounter++}",
                        severity = "WARNING",
                        category = "GST",
                        title = "Missing GSTIN on High Turnover Party",
                        voucherOrItemRef = "Party: ${l.name}",
                        problem = "Party has outstanding balance of ₹${String.format("%.2f", l.currentBalance)} with no registered GSTIN.",
                        expected = "15-digit valid GSTIN for B2B transactions",
                        recorded = "Unregistered / Blank GSTIN",
                        suggestedAction = "Update party master with GSTIN to claim eligible Input Tax Credit (ITC) or file B2B invoices."
                    )
                )
            }
        }

        return issues
    }
}
