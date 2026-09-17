package com.example.ui.ledgers

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity
import com.example.domain.AccountingEngine
import com.example.domain.LedgerStatementRow
import com.example.ui.AccountsViewModel
import com.example.ui.components.StatusChip
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen

@Composable
fun LedgersScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val ledgers by viewModel.ledgers.collectAsStateWithLifecycle()
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var ledgerToEdit by remember { mutableStateOf<LedgerEntity?>(null) }
    var ledgerForStatement by remember { mutableStateOf<LedgerEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val filterTypes = listOf("ALL", "CUSTOMER", "SUPPLIER", "BANK", "CASH", "EXPENSE")

    val filteredLedgers = ledgers.filter { l ->
        val matchesType = selectedTypeFilter == "ALL" || l.partyType == selectedTypeFilter
        val matchesSearch = searchQuery.isBlank() ||
                l.name.contains(searchQuery, ignoreCase = true) ||
                l.mobileNumber.contains(searchQuery, ignoreCase = true) ||
                l.gstin.contains(searchQuery, ignoreCase = true) ||
                l.groupName.contains(searchQuery, ignoreCase = true)
        matchesType && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_ledger")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Ledger")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("ledgers_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search account, customer, mobile, or GSTIN...") },
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
                    .testTag("ledger_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Filter Chips
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

            // Ledger List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredLedgers, key = { it.id }) { ledger ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { ledgerForStatement = ledger }
                            .testTag("ledger_card_${ledger.name.replace(" ", "_")}"),
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(status = ledger.partyType)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = ledger.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Group: ${ledger.groupName}" + (if (ledger.mobileNumber.isNotBlank()) " • 📞 ${ledger.mobileNumber}" else ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (ledger.gstin.isNotBlank()) {
                                        Text(
                                            text = "GSTIN: ${ledger.gstin} (${ledger.state})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "₹${String.format("%,.2f", ledger.currentBalance)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ledger.currentBalanceType == "DR") SuccessGreen else PrimaryNavy
                                    )
                                    Text(
                                        text = "${ledger.currentBalanceType} Balance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { ledgerToEdit = ledger }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { ledgerForStatement = ledger }) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = "Statement", tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Dialog
    if (showCreateDialog || ledgerToEdit != null) {
        val target = ledgerToEdit
        LedgerFormDialog(
            initialLedger = target,
            onDismiss = {
                showCreateDialog = false
                ledgerToEdit = null
            },
            onSave = { saved ->
                viewModel.saveLedger(saved)
                showCreateDialog = false
                ledgerToEdit = null
            }
        )
    }

    // Ledger Statement Dialog
    ledgerForStatement?.let { ledger ->
        val statementRows = AccountingEngine.generateLedgerStatement(ledger, vouchers)
        LedgerStatementDialog(
            ledger = ledger,
            rows = statementRows,
            onDismiss = { ledgerForStatement = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerFormDialog(
    initialLedger: LedgerEntity?,
    onDismiss: () -> Unit,
    onSave: (LedgerEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialLedger?.name ?: "") }
    var groupName by remember { mutableStateOf(initialLedger?.groupName ?: "Sundry Debtors") }
    var partyType by remember { mutableStateOf(initialLedger?.partyType ?: "CUSTOMER") }
    var mobile by remember { mutableStateOf(initialLedger?.mobileNumber ?: "") }
    var email by remember { mutableStateOf(initialLedger?.email ?: "") }
    var address by remember { mutableStateOf(initialLedger?.address ?: "") }
    var gstin by remember { mutableStateOf(initialLedger?.gstin ?: "") }
    var state by remember { mutableStateOf(initialLedger?.state ?: "Gujarat") }
    var opBal by remember { mutableStateOf(initialLedger?.openingBalance?.toString() ?: "0.0") }
    var opType by remember { mutableStateOf(initialLedger?.openingBalanceType ?: "DR") }
    var creditLimit by remember { mutableStateOf(initialLedger?.creditLimit?.toString() ?: "0.0") }

    var typeExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialLedger == null) "Create Ledger Account" else "Edit Ledger Account", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().testTag("ledger_form_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account / Party Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_ledger_name"),
                        singleLine = true
                    )
                }
                item {
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = "$partyType ($groupName)",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Party Type / Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            listOf(
                                "CUSTOMER" to "Sundry Debtors",
                                "SUPPLIER" to "Sundry Creditors",
                                "BANK" to "Bank Accounts",
                                "CASH" to "Cash-in-Hand",
                                "EXPENSE" to "Indirect Expenses",
                                "INCOME" to "Sales Accounts",
                                "CAPITAL" to "Capital Account"
                            ).forEach { (t, g) ->
                                DropdownMenuItem(
                                    text = { Text("$t ($g)") },
                                    onClick = {
                                        partyType = t
                                        groupName = g
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = gstin,
                            onValueChange = { gstin = it },
                            label = { Text("GSTIN (15 Digits)") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            modifier = Modifier.weight(0.8f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Full Address") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = opBal,
                            onValueChange = { opBal = it },
                            label = { Text("Opening Balance (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(selected = opType == "DR", onClick = { opType = "DR" }, label = { Text("Debit (DR)") })
                            Spacer(modifier = Modifier.width(4.dp))
                            FilterChip(selected = opType == "CR", onClick = { opType = "CR" }, label = { Text("Credit (CR)") })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val ob = opBal.toDoubleOrNull() ?: 0.0
                        onSave(
                            LedgerEntity(
                                id = initialLedger?.id ?: 0,
                                companyId = initialLedger?.companyId ?: 1L,
                                name = name,
                                groupName = groupName,
                                partyType = partyType,
                                mobileNumber = mobile,
                                email = email,
                                address = address,
                                gstin = gstin,
                                state = state,
                                openingBalance = ob,
                                openingBalanceType = opType,
                                currentBalance = if (initialLedger != null) initialLedger.currentBalance else ob,
                                currentBalanceType = opType,
                                creditLimit = creditLimit.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_ledger")
            ) {
                Text("Save Ledger")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun LedgerStatementDialog(
    ledger: LedgerEntity,
    rows: List<LedgerStatementRow>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = "Statement: ${ledger.name}", fontWeight = FontWeight.Bold)
                Text(text = "Current Balance: ₹${String.format("%,.2f", ledger.currentBalance)} ${ledger.currentBalanceType}", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Date / Particular", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Text("Dr / Cr / Bal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                }

                items(rows) { r ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text(text = "${r.date} • ${r.voucherType} ${r.voucherNo}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                                Text(text = r.particular, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(0.8f)) {
                                if (r.debit > 0) {
                                    Text(text = "Dr: ₹${String.format("%,.2f", r.debit)}", color = SuccessGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                                if (r.credit > 0) {
                                    Text(text = "Cr: ₹${String.format("%,.2f", r.credit)}", color = PrimaryNavy, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                                Text(text = "Bal: ₹${String.format("%,.2f", r.balance)} ${r.balanceType}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
