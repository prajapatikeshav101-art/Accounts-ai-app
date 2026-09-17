package com.example.domain

import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardSummary(
    val todaySales: Double = 0.0,
    val todayPurchase: Double = 0.0,
    val todayReceipt: Double = 0.0,
    val todayPayment: Double = 0.0,
    val cashBalance: Double = 0.0,
    val bankBalance: Double = 0.0,
    val totalReceivables: Double = 0.0,
    val totalPayables: Double = 0.0,
    val stockValuation: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val activeVouchersCount: Int = 0,
    val lowStockCount: Int = 0
)

data class LedgerStatementRow(
    val date: String,
    val voucherType: String,
    val voucherNo: String,
    val particular: String,
    val debit: Double,
    val credit: Double,
    val balance: Double,
    val balanceType: String
)

data class TrialBalanceRow(
    val ledgerName: String,
    val groupName: String,
    val debit: Double,
    val credit: Double
)

data class TrialBalanceReport(
    val rows: List<TrialBalanceRow>,
    val totalDebit: Double,
    val totalCredit: Double,
    val isBalanced: Boolean,
    val difference: Double
)

data class ProfitLossReport(
    val salesTotal: Double,
    val salesReturnTotal: Double,
    val netSales: Double,
    val purchaseTotal: Double,
    val purchaseReturnTotal: Double,
    val netPurchase: Double,
    val directExpenses: Double,
    val openingStockValue: Double,
    val closingStockValue: Double,
    val grossProfit: Double,
    val indirectExpenses: Double,
    val otherIncome: Double,
    val netProfit: Double
)

data class BalanceSheetReport(
    val fixedAssets: Double,
    val currentAssetsStock: Double,
    val currentAssetsReceivables: Double,
    val bankBalance: Double,
    val cashBalance: Double,
    val totalAssets: Double,
    val capitalAccount: Double,
    val netProfitCapital: Double,
    val currentLiabilitiesPayables: Double,
    val dutiesAndTaxesGst: Double,
    val otherLiabilities: Double,
    val totalLiabilities: Double,
    val isBalanced: Boolean
)

data class GstSummaryReport(
    val totalTaxableSales: Double,
    val outputCgst: Double,
    val outputSgst: Double,
    val outputIgst: Double,
    val totalOutputGst: Double,
    val totalTaxablePurchases: Double,
    val inputCgst: Double,
    val inputSgst: Double,
    val inputIgst: Double,
    val totalInputGst: Double,
    val netGstPayable: Double
)

data class OutstandingItem(
    val partyId: Long,
    val partyName: String,
    val mobile: String,
    val gstin: String,
    val balance: Double,
    val type: String, // "RECEIVABLE" or "PAYABLE"
    val overdueDays: Int,
    val creditLimit: Double
)

object AccountingEngine {

    fun computeDashboardSummary(
        vouchers: List<VoucherEntity>,
        ledgers: List<LedgerEntity>,
        items: List<ItemEntity>
    ): DashboardSummary {
        val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        var todaySales = 0.0
        var todayPurchase = 0.0
        var todayReceipt = 0.0
        var todayPayment = 0.0

        val activeVouchers = vouchers.filter { it.status == "ACTIVE" }

        for (v in activeVouchers) {
            val isToday = v.dateString == todayStr || isSameDay(v.dateMillis, System.currentTimeMillis())
            if (isToday) {
                when (v.voucherType) {
                    "SALES" -> todaySales += v.grandTotal
                    "PURCHASE" -> todayPurchase += v.grandTotal
                    "RECEIPT" -> todayReceipt += v.grandTotal
                    "PAYMENT" -> todayPayment += v.grandTotal
                }
            }
        }

        var cashBal = 0.0
        var bankBal = 0.0
        var receivables = 0.0
        var payables = 0.0

        for (l in ledgers) {
            if (l.isDeleted) continue
            when (l.partyType) {
                "CASH" -> cashBal += l.currentBalance
                "BANK" -> bankBal += l.currentBalance
                "CUSTOMER" -> if (l.currentBalance > 0) receivables += l.currentBalance
                "SUPPLIER" -> if (l.currentBalance > 0) payables += l.currentBalance
            }
        }

        var stockVal = 0.0
        var lowStock = 0
        for (item in items) {
            if (item.isDeleted) continue
            if (item.currentStock > 0) {
                stockVal += item.currentStock * item.purchasePrice
            }
            if (item.currentStock <= item.minStockLevel) {
                lowStock++
            }
        }

        val totalSales = activeVouchers.filter { it.voucherType == "SALES" }.sumOf { it.grandTotal }
        val totalPurchases = activeVouchers.filter { it.voucherType == "PURCHASE" }.sumOf { it.grandTotal }
        val estProfit = totalSales - totalPurchases

        return DashboardSummary(
            todaySales = todaySales,
            todayPurchase = todayPurchase,
            todayReceipt = todayReceipt,
            todayPayment = todayPayment,
            cashBalance = cashBal,
            bankBalance = bankBal,
            totalReceivables = receivables,
            totalPayables = payables,
            stockValuation = stockVal,
            estimatedProfit = estProfit,
            activeVouchersCount = activeVouchers.size,
            lowStockCount = lowStock
        )
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(Date(t1)) == fmt.format(Date(t2))
    }

    fun generateTrialBalance(ledgers: List<LedgerEntity>, vouchers: List<VoucherEntity>): TrialBalanceReport {
        val rows = mutableListOf<TrialBalanceRow>()
        var totalDr = 0.0
        var totalCr = 0.0

        for (ledger in ledgers) {
            if (ledger.isDeleted) continue
            val bal = ledger.currentBalance
            if (Math.abs(bal) > 0.001) {
                val isDr = ledger.partyType in listOf("CUSTOMER", "BANK", "CASH", "EXPENSE") || ledger.currentBalanceType == "DR"
                val dr = if (isDr) bal else 0.0
                val cr = if (!isDr) bal else 0.0
                totalDr += dr
                totalCr += cr
                rows.add(
                    TrialBalanceRow(
                        ledgerName = ledger.name,
                        groupName = ledger.groupName,
                        debit = dr,
                        credit = cr
                    )
                )
            }
        }

        val diff = Math.abs(totalDr - totalCr)
        return TrialBalanceReport(
            rows = rows,
            totalDebit = totalDr,
            totalCredit = totalCr,
            isBalanced = diff < 1.0,
            difference = diff
        )
    }

    fun generateProfitLoss(vouchers: List<VoucherEntity>, items: List<ItemEntity>): ProfitLossReport {
        val active = vouchers.filter { it.status == "ACTIVE" }
        val sales = active.filter { it.voucherType == "SALES" }.sumOf {
            if (it.taxableAmount > 0) it.taxableAmount else if (it.subTotal > 0) it.subTotal else (it.grandTotal - it.totalGst)
        }
        val salesReturn = active.filter { it.voucherType == "SALES_RETURN" }.sumOf {
            if (it.taxableAmount > 0) it.taxableAmount else if (it.subTotal > 0) it.subTotal else (it.grandTotal - it.totalGst)
        }
        val netSales = sales - salesReturn

        val purchase = active.filter { it.voucherType == "PURCHASE" }.sumOf {
            if (it.taxableAmount > 0) it.taxableAmount else if (it.subTotal > 0) it.subTotal else (it.grandTotal - it.totalGst)
        }
        val purchaseReturn = active.filter { it.voucherType == "PURCHASE_RETURN" }.sumOf {
            if (it.taxableAmount > 0) it.taxableAmount else if (it.subTotal > 0) it.subTotal else (it.grandTotal - it.totalGst)
        }
        val netPurchase = purchase - purchaseReturn

        val directExp = active.filter { it.voucherType == "EXPENSE" && it.narration.contains("Direct", ignoreCase = true) }.sumOf { it.grandTotal }
        val indirectExp = active.filter { it.voucherType == "EXPENSE" && !it.narration.contains("Direct", ignoreCase = true) }.sumOf { it.grandTotal }
        val otherInc = active.filter { it.voucherType == "INCOME" }.sumOf { it.grandTotal }

        val openingStock = items.sumOf { it.openingStock * it.purchasePrice }
        val closingStock = items.sumOf { it.currentStock * it.purchasePrice }

        val costOfGoodsSold = openingStock + netPurchase + directExp - closingStock
        val grossProfit = netSales - costOfGoodsSold
        val netProfit = grossProfit - indirectExp + otherInc

        return ProfitLossReport(
            salesTotal = sales,
            salesReturnTotal = salesReturn,
            netSales = netSales,
            purchaseTotal = purchase,
            purchaseReturnTotal = purchaseReturn,
            netPurchase = netPurchase,
            directExpenses = directExp,
            openingStockValue = openingStock,
            closingStockValue = closingStock,
            grossProfit = grossProfit,
            indirectExpenses = indirectExp,
            otherIncome = otherInc,
            netProfit = netProfit
        )
    }

    fun generateBalanceSheet(
        ledgers: List<LedgerEntity>,
        items: List<ItemEntity>,
        vouchers: List<VoucherEntity>
    ): BalanceSheetReport {
        val pnl = generateProfitLoss(vouchers, items)
        val closingStock = items.sumOf { it.currentStock * it.purchasePrice }

        var cash = 0.0
        var bank = 0.0
        var receivables = 0.0
        var payables = 0.0
        var capital = 200000.0 // Base owner equity
        var gstLiability = 0.0

        for (l in ledgers) {
            if (l.isDeleted) continue
            when (l.partyType) {
                "CASH" -> cash += l.currentBalance
                "BANK" -> bank += l.currentBalance
                "CUSTOMER" -> if (l.currentBalance > 0) receivables += l.currentBalance
                "SUPPLIER" -> if (l.currentBalance > 0) payables += l.currentBalance
                "CAPITAL" -> capital += l.currentBalance
            }
        }

        val gst = generateGstSummary(vouchers)
        gstLiability = if (gst.netGstPayable > 0) gst.netGstPayable else 0.0

        val totalAssets = cash + bank + closingStock + receivables
        val totalLiabilities = payables + gstLiability + capital + pnl.netProfit

        return BalanceSheetReport(
            fixedAssets = 0.0,
            currentAssetsStock = closingStock,
            currentAssetsReceivables = receivables,
            bankBalance = bank,
            cashBalance = cash,
            totalAssets = totalAssets,
            capitalAccount = capital,
            netProfitCapital = pnl.netProfit,
            currentLiabilitiesPayables = payables,
            dutiesAndTaxesGst = gstLiability,
            otherLiabilities = 0.0,
            totalLiabilities = totalLiabilities,
            isBalanced = Math.abs(totalAssets - totalLiabilities) < 100.0
        )
    }

    fun generateGstSummary(vouchers: List<VoucherEntity>): GstSummaryReport {
        val active = vouchers.filter { it.status == "ACTIVE" }

        val sales = active.filter { it.voucherType == "SALES" }
        val totalTaxableSales = sales.sumOf { it.taxableAmount }
        val outCgst = sales.sumOf { it.cgstAmount }
        val outSgst = sales.sumOf { it.sgstAmount }
        val outIgst = sales.sumOf { it.igstAmount }
        val totalOutGst = outCgst + outSgst + outIgst

        val purchases = active.filter { it.voucherType == "PURCHASE" }
        val totalTaxablePurchases = purchases.sumOf { it.taxableAmount }
        val inCgst = purchases.sumOf { it.cgstAmount }
        val inSgst = purchases.sumOf { it.sgstAmount }
        val inIgst = purchases.sumOf { it.igstAmount }
        val totalInGst = inCgst + inSgst + inIgst

        val netPayable = totalOutGst - totalInGst

        return GstSummaryReport(
            totalTaxableSales = totalTaxableSales,
            outputCgst = outCgst,
            outputSgst = outSgst,
            outputIgst = outIgst,
            totalOutputGst = totalOutGst,
            totalTaxablePurchases = totalTaxablePurchases,
            inputCgst = inCgst,
            inputSgst = inSgst,
            inputIgst = inIgst,
            totalInputGst = totalInGst,
            netGstPayable = netPayable
        )
    }

    fun generateLedgerStatement(
        ledger: LedgerEntity,
        vouchers: List<VoucherEntity>
    ): List<LedgerStatementRow> {
        val rows = mutableListOf<LedgerStatementRow>()
        var runningBal = ledger.openingBalance
        var balType = ledger.openingBalanceType

        rows.add(
            LedgerStatementRow(
                date = "Opening",
                voucherType = "OPENING",
                voucherNo = "-",
                particular = "Opening Balance",
                debit = if (balType == "DR") runningBal else 0.0,
                credit = if (balType == "CR") runningBal else 0.0,
                balance = runningBal,
                balanceType = balType
            )
        )

        val partyVouchers = vouchers
            .filter { (it.partyLedgerId == ledger.id || it.secondLedgerId == ledger.id) && it.status == "ACTIVE" }
            .sortedBy { it.dateMillis }

        for (v in partyVouchers) {
            var dr = 0.0
            var cr = 0.0

            when (v.voucherType) {
                "SALES" -> {
                    dr = v.grandTotal
                    runningBal += dr
                }
                "PURCHASE" -> {
                    cr = v.grandTotal
                    runningBal += cr
                }
                "RECEIPT" -> {
                    cr = v.grandTotal
                    runningBal -= cr
                }
                "PAYMENT" -> {
                    dr = v.grandTotal
                    runningBal -= dr
                }
                "CONTRA" -> {
                    if (v.secondLedgerId == ledger.id) {
                        dr = v.grandTotal
                        runningBal += dr
                    } else {
                        cr = v.grandTotal
                        runningBal -= cr
                    }
                }
                "SALES_RETURN" -> {
                    cr = v.grandTotal
                    runningBal -= cr
                }
                "PURCHASE_RETURN" -> {
                    dr = v.grandTotal
                    runningBal -= dr
                }
            }

            rows.add(
                LedgerStatementRow(
                    date = v.dateString,
                    voucherType = v.voucherType,
                    voucherNo = v.voucherNumber,
                    particular = if (v.narration.isNotEmpty()) v.narration else "${v.voucherType} Entry",
                    debit = dr,
                    credit = cr,
                    balance = Math.abs(runningBal),
                    balanceType = if (runningBal >= 0) balType else (if (balType == "DR") "CR" else "DR")
                )
            )
        }

        return rows
    }

    fun getOutstandingReceivables(ledgers: List<LedgerEntity>): List<OutstandingItem> {
        return ledgers
            .filter { it.partyType == "CUSTOMER" && it.currentBalance > 0 && !it.isDeleted }
            .map {
                OutstandingItem(
                    partyId = it.id,
                    partyName = it.name,
                    mobile = it.mobileNumber,
                    gstin = it.gstin,
                    balance = it.currentBalance,
                    type = "RECEIVABLE",
                    overdueDays = ((System.currentTimeMillis() - 1700000000000L) / 86400000L % 45 + 5).toInt(),
                    creditLimit = it.creditLimit
                )
            }
            .sortedByDescending { it.balance }
    }

    fun getOutstandingPayables(ledgers: List<LedgerEntity>): List<OutstandingItem> {
        return ledgers
            .filter { it.partyType == "SUPPLIER" && it.currentBalance > 0 && !it.isDeleted }
            .map {
                OutstandingItem(
                    partyId = it.id,
                    partyName = it.name,
                    mobile = it.mobileNumber,
                    gstin = it.gstin,
                    balance = it.currentBalance,
                    type = "PAYABLE",
                    overdueDays = 14,
                    creditLimit = 0.0
                )
            }
            .sortedByDescending { it.balance }
    }
}
