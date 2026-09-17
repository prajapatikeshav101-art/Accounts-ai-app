package com.example.ui.vouchers

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.model.CompanyEntity
import com.example.data.model.VoucherEntity
import com.example.ui.AccountsViewModel
import com.example.ui.components.StatusChip
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen

@Composable
fun VouchersScreen(
    viewModel: AccountsViewModel,
    onNavigateToCreate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()
    val company by viewModel.currentCompany.collectAsStateWithLifecycle()

    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedVoucherForDetails by remember { mutableStateOf<VoucherEntity?>(null) }
    var showTypePicker by remember { mutableStateOf(false) }

    val filterTypes = listOf("ALL", "SALES", "PURCHASE", "RECEIPT", "PAYMENT", "CONTRA", "EXPENSE")

    val filteredVouchers = vouchers.filter { v ->
        val matchesType = selectedTypeFilter == "ALL" || v.voucherType.equals(selectedTypeFilter, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                v.voucherNumber.contains(searchQuery, ignoreCase = true) ||
                v.partyLedgerName.contains(searchQuery, ignoreCase = true) ||
                v.narration.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showTypePicker = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_voucher")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Voucher")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("vouchers_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search invoice #, customer, or narration...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("voucher_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Horizontal Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(filterTypes) { type ->
                    FilterChip(
                        selected = selectedTypeFilter == type,
                        onClick = { selectedTypeFilter = type },
                        label = { Text(type) }
                    )
                }
            }

            // Voucher list
            if (filteredVouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "No records",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No vouchers found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to record a sale, purchase, payment or receipt.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredVouchers, key = { it.id }) { v ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVoucherForDetails = v }
                                .testTag("voucher_row_${v.voucherNumber}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(status = v.voucherType)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = v.voucherNumber,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "₹${String.format("%,.2f", v.grandTotal)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (v.voucherType in listOf("SALES", "RECEIPT")) SuccessGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = v.partyLedgerName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = v.dateString,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (v.narration.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = v.narration,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Mode: ${v.paymentMode}" + (if (v.totalGst > 0) " • GST: ₹${String.format("%.2f", v.totalGst)}" else ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    if (v.status == "CANCELLED") {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.errorContainer
                                        ) {
                                            Text(
                                                text = "CANCELLED",
                                                color = MaterialTheme.colorScheme.error,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Type picker dialog
    if (showTypePicker) {
        AlertDialog(
            onDismissRequest = { showTypePicker = false },
            title = { Text("Choose Voucher Type", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SALES" to "Tax Invoice / Sale Bill",
                        "PURCHASE" to "Purchase Bill",
                        "RECEIPT" to "Customer Payment Received",
                        "PAYMENT" to "Supplier / Vendor Payment",
                        "CONTRA" to "Cash to Bank / Bank Transfer",
                        "EXPENSE" to "Direct / Indirect Expense").forEach { (type, desc) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showTypePicker = false
                                    onNavigateToCreate(type)
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = type, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTypePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Voucher Details / Print Preview Modal
    selectedVoucherForDetails?.let { v ->
        VoucherDetailDialog(
            voucher = v,
            company = company ?: CompanyEntity(name = "Keshav Traders"),
            onDismiss = { selectedVoucherForDetails = null },
            onCancelVoucher = {
                viewModel.cancelVoucher(v.id)
                selectedVoucherForDetails = null
            }
        )
    }
}

@Composable
fun VoucherDetailDialog(
    voucher: VoucherEntity,
    company: CompanyEntity,
    onDismiss: () -> Unit,
    onCancelVoucher: () -> Unit
) {
    var showConfirmCancel by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${voucher.voucherType} Details", fontWeight = FontWeight.Bold)
                StatusChip(status = voucher.status)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voucher_details_modal"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = company.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(text = "GSTIN: ${company.gstin} • Mobile: ${company.mobileNumber}", style = MaterialTheme.typography.labelSmall)
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text(text = "Invoice / Voucher No: ${voucher.voucherNumber}", fontWeight = FontWeight.SemiBold)
                        Text(text = "Date: ${voucher.dateString} • Mode: ${voucher.paymentMode}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "Party: ${voucher.partyLedgerName}", fontWeight = FontWeight.SemiBold)
                    }
                }

                // Financial Breakdown
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal / Taxable:", style = MaterialTheme.typography.bodyMedium)
                        Text("₹${String.format("%,.2f", voucher.taxableAmount)}", fontWeight = FontWeight.SemiBold)
                    }
                    if (voucher.cgstAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${String.format("%,.2f", voucher.cgstAmount)}")
                        }
                    }
                    if (voucher.sgstAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${String.format("%,.2f", voucher.sgstAmount)}")
                        }
                    }
                    if (voucher.igstAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IGST:", style = MaterialTheme.typography.bodySmall)
                            Text("₹${String.format("%,.2f", voucher.igstAmount)}")
                        }
                    }
                    if (voucher.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", style = MaterialTheme.typography.bodySmall)
                            Text("-₹${String.format("%,.2f", voucher.discountAmount)}")
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("₹${String.format("%,.2f", voucher.grandTotal)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PrimaryNavy)
                    }
                }

                if (voucher.narration.isNotBlank()) {
                    Text(text = "Note: ${voucher.narration}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }

                // Bank details on invoice
                if (company.bankAccountNo.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(text = "Bank Details for Transfer:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            Text(text = "${company.bankName} • A/C: ${company.bankAccountNo} • IFSC: ${company.bankIfsc}", style = MaterialTheme.typography.labelSmall)
                            if (company.upiId.isNotBlank()) {
                                Text(text = "UPI: ${company.upiId}", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (voucher.status == "ACTIVE") {
                Button(
                    onClick = { showConfirmCancel = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Voucher")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showConfirmCancel) {
        AlertDialog(
            onDismissRequest = { showConfirmCancel = false },
            title = { Text("Confirm Cancellation") },
            text = { Text("Are you sure you want to cancel ${voucher.voucherNumber}? This will automatically reverse ledger balances and stock movements according to double-entry accounting rules.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmCancel = false
                        onCancelVoucher()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Yes, Cancel")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCancel = false }) {
                    Text("No")
                }
            }
        )
    }
}
