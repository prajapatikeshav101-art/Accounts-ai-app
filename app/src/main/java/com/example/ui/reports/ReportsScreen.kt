package com.example.ui.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.AccountingEngine
import com.example.ui.AccountsViewModel
import com.example.ui.components.StatusChip
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen

@Composable
fun ReportsScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()
    val ledgers by viewModel.ledgers.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val company by viewModel.currentCompany.collectAsStateWithLifecycle()

    var selectedReportTab by remember { mutableStateOf("PROFIT_LOSS") }

    val reportTabs = listOf(
        "PROFIT_LOSS" to "Profit & Loss",
        "BALANCE_SHEET" to "Balance Sheet",
        "TRIAL_BALANCE" to "Trial Balance",
        "GST_SUMMARY" to "GST GSTR-3B",
        "RECEIVABLES" to "Receivables",
        "PAYABLES" to "Payables",
        "DAY_BOOK" to "Day Book"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen")
    ) {
        // Horizontal Tabs
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reportTabs) { (key, label) ->
                FilterChip(
                    selected = selectedReportTab == key,
                    onClick = { selectedReportTab = key },
                    label = { Text(label, fontWeight = if (selectedReportTab == key) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedReportTab) {
                "PROFIT_LOSS" -> {
                    val pnl = AccountingEngine.generateProfitLoss(vouchers, items)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Profit & Loss Statement (Trading & Income)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("For Period: ${company?.financialYear ?: "2025-2026"}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Divider()

                                // Trading section
                                ReportLine("Gross Sales Revenue", pnl.salesTotal)
                                if (pnl.salesReturnTotal > 0) ReportLine("Less: Sales Returns", -pnl.salesReturnTotal)
                                ReportLine("Net Sales", pnl.netSales, isBold = true)

                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                ReportLine("Opening Stock", pnl.openingStockValue)
                                ReportLine("Add: Net Purchases", pnl.netPurchase)
                                ReportLine("Add: Direct Freight / Wages", pnl.directExpenses)
                                ReportLine("Less: Closing Stock", -pnl.closingStockValue)

                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GROSS PROFIT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("₹${String.format("%,.2f", pnl.grossProfit)}", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                ReportLine("Less: Indirect Expenses (Rent, Salary, Bills)", -pnl.indirectExpenses)
                                if (pnl.otherIncome > 0) ReportLine("Add: Other Income", pnl.otherIncome)

                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("NET PROFIT", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("₹${String.format("%,.2f", pnl.netProfit)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }
                    }
                }

                "BALANCE_SHEET" -> {
                    val bs = AccountingEngine.generateBalanceSheet(ledgers, items, vouchers)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Balance Sheet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    if (bs.isBalanced) {
                                        Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(6.dp)) {
                                            Text("Balanced", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Divider()

                                Text("ASSETS", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                ReportLine("Closing Stock Inventory", bs.currentAssetsStock)
                                ReportLine("Sundry Debtors (Receivables)", bs.currentAssetsReceivables)
                                ReportLine("Bank Balances", bs.bankBalance)
                                ReportLine("Cash-in-Hand", bs.cashBalance)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL ASSETS", fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%,.2f", bs.totalAssets)}", fontWeight = FontWeight.Bold)
                                }

                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("LIABILITIES & EQUITY", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                ReportLine("Capital Account", bs.capitalAccount)
                                ReportLine("Current Net Profit", bs.netProfitCapital)
                                ReportLine("Sundry Creditors (Payables)", bs.currentLiabilitiesPayables)
                                ReportLine("Duties & Taxes (Net GST Liability)", bs.dutiesAndTaxesGst)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL LIABILITIES", fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%,.2f", bs.totalLiabilities)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "TRIAL_BALANCE" -> {
                    val tb = AccountingEngine.generateTrialBalance(ledgers, vouchers)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Trial Balance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        if (tb.isBalanced) "Balanced ✓" else "Difference: ₹${String.format("%.2f", tb.difference)}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (tb.isBalanced) SuccessGreen else MaterialTheme.colorScheme.error
                                    )
                                }
                                Divider()

                                tb.rows.forEach { r ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1.2f)) {
                                            Text(r.ledgerName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
                                            Text(r.groupName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        }
                                        Row(modifier = Modifier.weight(0.8f), horizontalArrangement = Arrangement.End) {
                                            Text(if (r.debit > 0) "₹${String.format("%,.0f", r.debit)}" else "-", modifier = Modifier.weight(1f))
                                            Text(if (r.credit > 0) "₹${String.format("%,.0f", r.credit)}" else "-", modifier = Modifier.weight(1f))
                                        }
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOTAL DEBITS & CREDITS", fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%,.2f", tb.totalDebit)} | ₹${String.format("%,.2f", tb.totalCredit)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                "GST_SUMMARY" -> {
                    val gst = AccountingEngine.generateGstSummary(vouchers)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("GSTR-3B Monthly Tax Return Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Output Tax (Sales) vs Input Tax Credit (Purchases)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                Divider()

                                Text("OUTWARD SUPPLIES (SALES TAX COLLECTED)", fontWeight = FontWeight.Bold, color = SuccessGreen)
                                ReportLine("Taxable Sales Turnover", gst.totalTaxableSales)
                                ReportLine("Central GST (CGST)", gst.outputCgst)
                                ReportLine("State GST (SGST)", gst.outputSgst)
                                ReportLine("Integrated GST (IGST)", gst.outputIgst)
                                ReportLine("Total Output Tax Liability", gst.totalOutputGst, isBold = true)

                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Text("ELIGIBLE INPUT TAX CREDIT (ITC PURCHASES)", fontWeight = FontWeight.Bold, color = PrimaryNavy)
                                ReportLine("Taxable Purchases Turnover", gst.totalTaxablePurchases)
                                ReportLine("Input CGST", gst.inputCgst)
                                ReportLine("Input SGST", gst.inputSgst)
                                ReportLine("Input IGST", gst.inputIgst)
                                ReportLine("Total ITC Available", gst.totalInputGst, isBold = true)

                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (gst.netGstPayable >= 0) Color(0xFFFFF3E0) else Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(if (gst.netGstPayable >= 0) "NET GST PAYABLE (CASH/CREDIT LEDGER)" else "ITC CREDIT CARRIED FORWARD", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text("₹${String.format("%,.2f", Math.abs(gst.netGstPayable))}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    }
                                }
                            }
                        }
                    }
                }

                "RECEIVABLES" -> {
                    val receivables = AccountingEngine.getOutstandingReceivables(ledgers)
                    item {
                        Text("Outstanding Receivables (${receivables.size} Debtors)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    items(receivables) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(r.partyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    if (r.mobile.isNotBlank()) Text("📞 ${r.mobile}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Text("Aging: ~${r.overdueDays} days overdue", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                                }
                                Text("₹${String.format("%,.2f", r.balance)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = SuccessGreen)
                            }
                        }
                    }
                }

                "PAYABLES" -> {
                    val payables = AccountingEngine.getOutstandingPayables(ledgers)
                    item {
                        Text("Outstanding Payables (${payables.size} Creditors)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    items(payables) { p ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(p.partyName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    if (p.mobile.isNotBlank()) Text("📞 ${p.mobile}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text("₹${String.format("%,.2f", p.balance)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PrimaryNavy)
                            }
                        }
                    }
                }

                "DAY_BOOK" -> {
                    item {
                        Text("Day Book (Chronological Activity)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    items(vouchers.sortedByDescending { it.dateMillis }) { v ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(status = v.voucherType)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(v.voucherNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(v.partyLedgerName, style = MaterialTheme.typography.bodyMedium)
                                    Text("${v.dateString} • Mode: ${v.paymentMode}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text("₹${String.format("%,.2f", v.grandTotal)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportLine(label: String, amount: Double, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyMedium)
        Text(
            (if (amount < 0) "-₹" else "₹") + String.format("%,.2f", Math.abs(amount)),
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
