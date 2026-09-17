package com.example.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.VoucherEntity
import com.example.ui.AccountsViewModel
import com.example.ui.components.QuickActionButton
import com.example.ui.components.StatusChip
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.TrendMiniGraph
import com.example.ui.navigation.AppStrings
import com.example.ui.theme.IndianRupeeGold
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SecondaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TertiaryEmerald
import com.example.ui.theme.WarningAmber

@Composable
fun DashboardScreen(
    viewModel: AccountsViewModel,
    onNavigateToCreateVoucher: (String) -> Unit,
    onNavigateToCreateLedger: () -> Unit,
    onNavigateToCreateItem: () -> Unit,
    onNavigateToAiChecker: () -> Unit,
    onNavigateToAiAssistant: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToExcel: () -> Unit,
    onVoucherClick: (VoucherEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentCompany by viewModel.currentCompany.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()
    val auditIssues by viewModel.auditIssues.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val companyName = currentCompany?.name ?: "Keshav Traders"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Company Banner & Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("company_banner_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = companyName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentCompany?.businessType ?: "Trading"} • GSTIN: ${currentCompany?.gstin ?: "Registered"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { onNavigateToAiAssistant() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Assistant",
                                    tint = IndianRupeeGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = AppStrings.get("est_profit", language),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "₹${String.format("%,.2f", summary.estimatedProfit)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "FY: ${currentCompany?.financialYear ?: "2025-26"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                            Text(
                                text = "${summary.activeVouchersCount} Active Entries",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = IndianRupeeGold
                            )
                        }
                    }
                }
            }
        }

        // 2. AI Checker Alert banner (if issues detected)
        if (auditIssues.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAiChecker() }
                        .testTag("ai_checker_alert_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFFE0B2),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🤖 AI Accounting Check: ${auditIssues.size} Discrepancies",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = auditIssues.firstOrNull()?.problem ?: "Review audit recommendations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037),
                                maxLines = 1
                            )
                        }
                        TextButton(onClick = { onNavigateToAiChecker() }) {
                            Text("Review", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }

        // 3. Quick Actions
        item {
            Column {
                Text(
                    text = AppStrings.get("quick_actions", language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_sale", language),
                            icon = Icons.Default.ShoppingCart,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { onNavigateToCreateVoucher("SALES") }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_purchase", language),
                            icon = Icons.Default.ShoppingBag,
                            containerColor = Color(0xFFEDE7F6),
                            contentColor = Color(0xFF512DA8),
                            onClick = { onNavigateToCreateVoucher("PURCHASE") }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_receipt", language),
                            icon = Icons.Default.ArrowDownward,
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32),
                            onClick = { onNavigateToCreateVoucher("RECEIPT") }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_payment", language),
                            icon = Icons.Default.ArrowUpward,
                            containerColor = Color(0xFFFFEBEE),
                            contentColor = Color(0xFFC62828),
                            onClick = { onNavigateToCreateVoucher("PAYMENT") }
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_customer", language),
                            icon = Icons.Default.PersonAdd,
                            containerColor = Color(0xFFE1F5FE),
                            contentColor = Color(0xFF0277BD),
                            onClick = onNavigateToCreateLedger
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_item", language),
                            icon = Icons.Default.Inventory2,
                            containerColor = Color(0xFFFFF8E1),
                            contentColor = Color(0xFFF57F17),
                            onClick = onNavigateToCreateItem
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_ai_check", language),
                            icon = Icons.Default.AutoAwesome,
                            containerColor = Color(0xFFFFF3E0),
                            contentColor = WarningAmber,
                            badgeCount = auditIssues.size,
                            onClick = onNavigateToAiChecker
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_excel", language),
                            icon = Icons.Default.UploadFile,
                            containerColor = Color(0xFFE0F2F1),
                            contentColor = TertiaryEmerald,
                            onClick = onNavigateToExcel
                        )
                    }
                    item {
                        QuickActionButton(
                            label = AppStrings.get("action_reports", language),
                            icon = Icons.Default.Analytics,
                            containerColor = Color(0xFFECEFF1),
                            contentColor = Color(0xFF37474F),
                            onClick = onNavigateToReports
                        )
                    }
                }
            }
        }

        // 4. Primary Metrics Grid (Rows of 2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Today's Sales & Purchases
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = AppStrings.get("today_sales", language),
                        amount = summary.todaySales,
                        icon = Icons.Default.ShoppingCart,
                        iconColor = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCreateVoucher("SALES") }
                    )
                    SummaryMetricCard(
                        title = AppStrings.get("today_purchase", language),
                        amount = summary.todayPurchase,
                        icon = Icons.Default.ShoppingBag,
                        iconColor = Color(0xFF512DA8),
                        bgColor = Color(0xFFEDE7F6),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCreateVoucher("PURCHASE") }
                    )
                }

                // Today's Receipt & Payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = AppStrings.get("today_receipt", language),
                        amount = summary.todayReceipt,
                        icon = Icons.Default.ArrowDownward,
                        iconColor = Color(0xFF00897B),
                        bgColor = Color(0xFFE0F2F1),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCreateVoucher("RECEIPT") }
                    )
                    SummaryMetricCard(
                        title = AppStrings.get("today_payment", language),
                        amount = summary.todayPayment,
                        icon = Icons.Default.ArrowUpward,
                        iconColor = Color(0xFFD32F2F),
                        bgColor = Color(0xFFFFEBEE),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCreateVoucher("PAYMENT") }
                    )
                }

                // Cash in Hand & Bank Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = AppStrings.get("cash_balance", language),
                        amount = summary.cashBalance,
                        subtitle = "Cash A/C",
                        icon = Icons.Default.MonetizationOn,
                        iconColor = Color(0xFFF57F17),
                        bgColor = Color(0xFFFFF8E1),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = AppStrings.get("bank_balance", language),
                        amount = summary.bankBalance,
                        subtitle = "SBI + HDFC",
                        icon = Icons.Default.Payment,
                        iconColor = Color(0xFF0277BD),
                        bgColor = Color(0xFFE1F5FE),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Receivables & Payables
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = AppStrings.get("receivables", language),
                        amount = summary.totalReceivables,
                        subtitle = "From Debtors",
                        icon = Icons.Default.ArrowDownward,
                        iconColor = Color(0xFF1E88E5),
                        bgColor = Color(0xFFE3F2FD),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                    SummaryMetricCard(
                        title = AppStrings.get("payables", language),
                        amount = summary.totalPayables,
                        subtitle = "To Creditors",
                        icon = Icons.Default.ArrowUpward,
                        iconColor = Color(0xFFC2185B),
                        bgColor = Color(0xFFFCE4EC),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                }

                // Stock Valuation & Estimated Profit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = AppStrings.get("stock_value", language),
                        amount = summary.stockValuation,
                        subtitle = "${summary.lowStockCount} items low",
                        icon = Icons.Default.Inventory2,
                        iconColor = Color(0xFF00796B),
                        bgColor = Color(0xFFE0F2F1),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = AppStrings.get("est_profit", language),
                        amount = summary.estimatedProfit,
                        subtitle = "Trading Margin",
                        icon = Icons.Default.Analytics,
                        iconColor = Color(0xFF2E7D32),
                        bgColor = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReports
                    )
                }
            }
        }

        // 5. Visual Trend Graphs
        item {
            TrendMiniGraph(
                title = "Sales Velocity & Invoicing Trend",
                points = listOf(14000f, 22000f, 18500f, 28000f, 24000f, 31000f, summary.todaySales.toFloat().coerceAtLeast(15000f)),
                lineColor = PrimaryNavy,
                fillColor = SecondaryBlue
            )
        }

        // 6. Recent Vouchers Feed
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToReports) {
                    Text("View Day Book")
                }
            }
        }

        items(vouchers.take(5)) { v ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onVoucherClick(v) }
                    .testTag("voucher_item_${v.voucherNumber}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusChip(status = v.voucherType)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = v.voucherNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = v.partyLedgerName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${v.dateString} • Mode: ${v.paymentMode}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "₹${String.format("%,.2f", v.grandTotal)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (v.voucherType in listOf("SALES", "RECEIPT")) SuccessGreen else MaterialTheme.colorScheme.onSurface
                        )
                        if (v.status == "CANCELLED") {
                            Text(
                                text = "CANCELLED",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
