package com.example

import com.example.data.importer.ExcelCsvManager
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity
import com.example.domain.AccountingEngine
import com.example.domain.AiAccountingChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountingEngineTest {

    @Test
    fun `test profit and loss calculation`() {
        val vouchers = listOf(
            VoucherEntity(
                id = 1,
                companyId = 1,
                voucherType = "SALES",
                voucherNumber = "INV-001",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 10,
                partyLedgerName = "Client A",
                grandTotal = 11800.0,
                subTotal = 10000.0,
                totalGst = 1800.0
            ),
            VoucherEntity(
                id = 2,
                companyId = 1,
                voucherType = "PURCHASE",
                voucherNumber = "PUR-001",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 20,
                partyLedgerName = "Vendor B",
                grandTotal = 5900.0,
                subTotal = 5000.0,
                totalGst = 900.0
            )
        )

        val items = listOf(
            ItemEntity(
                id = 1,
                companyId = 1,
                name = "Item 1",
                itemCode = "ITM-1",
                currentStock = 10.0,
                purchasePrice = 500.0,
                openingStock = 10.0
            )
        )

        val pnl = AccountingEngine.generateProfitLoss(vouchers, items)
        assertEquals(10000.0, pnl.netSales, 0.01)
        assertEquals(5000.0, pnl.netPurchase, 0.01)
        assertEquals(5000.0, pnl.grossProfit, 0.01)
        assertEquals(5000.0, pnl.netProfit, 0.01)
    }

    @Test
    fun `test gst summary report`() {
        val vouchers = listOf(
            VoucherEntity(
                id = 1,
                companyId = 1,
                voucherType = "SALES",
                voucherNumber = "INV-101",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 10,
                partyLedgerName = "Client A",
                subTotal = 10000.0,
                cgstAmount = 900.0,
                sgstAmount = 900.0,
                igstAmount = 0.0,
                totalGst = 1800.0,
                grandTotal = 11800.0
            ),
            VoucherEntity(
                id = 2,
                companyId = 1,
                voucherType = "PURCHASE",
                voucherNumber = "PUR-201",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 20,
                partyLedgerName = "Vendor B",
                subTotal = 4000.0,
                cgstAmount = 360.0,
                sgstAmount = 360.0,
                igstAmount = 0.0,
                totalGst = 720.0,
                grandTotal = 4720.0
            )
        )

        val gst = AccountingEngine.generateGstSummary(vouchers)
        assertEquals(1800.0, gst.totalOutputGst, 0.01)
        assertEquals(720.0, gst.totalInputGst, 0.01)
        assertEquals(1080.0, gst.netGstPayable, 0.01)
    }

    @Test
    fun `test ai accounting checker detects negative stock`() {
        val items = listOf(
            ItemEntity(
                id = 1,
                companyId = 1,
                name = "Critical Deficit Item",
                itemCode = "ITM-NEG",
                currentStock = -5.0,
                minStockLevel = 2.0
            )
        )

        val issues = AiAccountingChecker.runFullAudit(
            vouchers = emptyList(),
            ledgers = emptyList(),
            items = items
        )

        val negativeStockIssue = issues.find { it.category == "STOCK" }
        assertTrue("Negative stock issue should be reported", negativeStockIssue != null)
        assertEquals("CRITICAL", negativeStockIssue?.severity)
    }

    @Test
    fun `test ai accounting checker detects duplicate invoice numbers`() {
        val vouchers = listOf(
            VoucherEntity(
                id = 1,
                companyId = 1,
                voucherType = "SALES",
                voucherNumber = "INV-DUP",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 1,
                partyLedgerName = "Party 1",
                grandTotal = 1000.0
            ),
            VoucherEntity(
                id = 2,
                companyId = 1,
                voucherType = "SALES",
                voucherNumber = "INV-DUP",
                dateMillis = 1700000000000L,
                dateString = "17-09-2026",
                partyLedgerId = 2,
                partyLedgerName = "Party 2",
                grandTotal = 2000.0
            )
        )

        val issues = AiAccountingChecker.runFullAudit(
            vouchers = vouchers,
            ledgers = emptyList(),
            items = emptyList()
        )

        val duplicateIssue = issues.find { it.category == "DUPLICATE" }
        assertTrue("Duplicate voucher issue should be reported", duplicateIssue != null)
        assertEquals("CRITICAL", duplicateIssue?.severity)
    }

    @Test
    fun `test csv ledger parser`() {
        val csv = """
            Name,GroupName,PartyType,Mobile,Email,GSTIN,State,OpeningBalance,BalanceType
            Ramesh Patel,Sundry Debtors,CUSTOMER,9876543210,ramesh@test.com,24AAACR1234A1Z5,Gujarat,15000,DR
        """.trimIndent()

        val result = ExcelCsvManager.analyzeAndValidateLedgers(csv, 1L)
        assertEquals(1, result.validRows)
        assertTrue(result.previewRows.first().isValid)
    }
}
