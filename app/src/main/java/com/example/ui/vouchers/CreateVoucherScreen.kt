package com.example.ui.vouchers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.data.model.LedgerEntity
import com.example.data.model.VoucherEntity
import com.example.data.model.VoucherItemEntity
import com.example.ui.AccountsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DraftItemRow(
    val itemId: Long = 0,
    val itemName: String = "",
    val hsn: String = "",
    val quantity: String = "1",
    val unit: String = "PCS",
    val rate: String = "0.0",
    val discount: String = "0.0",
    val gstRate: Double = 18.0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVoucherScreen(
    initialType: String,
    viewModel: AccountsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val company by viewModel.currentCompany.collectAsStateWithLifecycle()
    val ledgers by viewModel.ledgers.collectAsStateWithLifecycle()
    val itemsList by viewModel.items.collectAsStateWithLifecycle()

    var voucherType by remember { mutableStateOf(initialType) }
    var voucherNumber by remember {
        val nextNum = (company?.nextInvoiceNumber ?: 1005) + (0..99).random()
        val prefix = if (voucherType == "SALES") (company?.invoicePrefix ?: "INV-") else "${voucherType.take(3)}-"
        mutableStateOf("$prefix$nextNum")
    }

    val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    var dateString by remember { mutableStateOf(todayStr) }
    var selectedParty by remember { mutableStateOf<LedgerEntity?>(null) }
    var selectedSecondLedger by remember { mutableStateOf<LedgerEntity?>(null) }
    var paymentMode by remember { mutableStateOf(if (voucherType in listOf("RECEIPT", "PAYMENT", "CONTRA")) "BANK" else "CREDIT") }
    var narration by remember { mutableStateOf("") }
    var simpleAmount by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Line items for Sales / Purchase
    val lineItems = remember {
        mutableStateListOf(
            DraftItemRow(
                itemId = itemsList.firstOrNull()?.id ?: 0,
                itemName = itemsList.firstOrNull()?.name ?: "Sample Product",
                rate = (itemsList.firstOrNull()?.salesPrice ?: 500.0).toString(),
                unit = itemsList.firstOrNull()?.unit ?: "PCS",
                gstRate = itemsList.firstOrNull()?.gstRate ?: 18.0
            )
        )
    }

    // Party selection dropdown state
    var partyExpanded by remember { mutableStateOf(false) }
    var secondLedgerExpanded by remember { mutableStateOf(false) }
    var modeExpanded by remember { mutableStateOf(false) }

    // Relevant parties based on voucher type
    val filteredParties = when (voucherType) {
        "SALES", "SALES_RETURN" -> ledgers.filter { it.partyType in listOf("CUSTOMER", "CASH", "BANK") }
        "PURCHASE", "PURCHASE_RETURN" -> ledgers.filter { it.partyType in listOf("SUPPLIER", "CASH", "BANK") }
        "RECEIPT" -> ledgers.filter { it.partyType in listOf("CUSTOMER", "OTHER") }
        "PAYMENT" -> ledgers.filter { it.partyType in listOf("SUPPLIER", "EXPENSE", "OTHER") }
        "CONTRA" -> ledgers.filter { it.partyType in listOf("CASH", "BANK") }
        "EXPENSE" -> ledgers.filter { it.partyType == "EXPENSE" }
        else -> ledgers
    }

    val bankAndCashLedgers = ledgers.filter { it.partyType in listOf("CASH", "BANK") }

    // Default select first available party if none selected
    if (selectedParty == null && filteredParties.isNotEmpty()) {
        selectedParty = filteredParties.first()
    }
    if (selectedSecondLedger == null && bankAndCashLedgers.isNotEmpty()) {
        selectedSecondLedger = bankAndCashLedgers.first()
    }

    // Calculations for Sales/Purchase
    val isMultiItem = voucherType in listOf("SALES", "PURCHASE", "SALES_RETURN", "PURCHASE_RETURN")
    val isInterState = (selectedParty?.state ?: "Gujarat") != (company?.state ?: "Gujarat")

    var computedTaxable = 0.0
    var computedCgst = 0.0
    var computedSgst = 0.0
    var computedIgst = 0.0
    var computedGrandTotal = 0.0

    if (isMultiItem) {
        for (row in lineItems) {
            val qty = row.quantity.toDoubleOrNull() ?: 0.0
            val rate = row.rate.toDoubleOrNull() ?: 0.0
            val disc = row.discount.toDoubleOrNull() ?: 0.0
            val taxable = (qty * rate) - disc
            computedTaxable += taxable
            val gstAmt = taxable * (row.gstRate / 100.0)
            if (isInterState) {
                computedIgst += gstAmt
            } else {
                computedCgst += (gstAmt / 2.0)
                computedSgst += (gstAmt / 2.0)
            }
        }
        computedGrandTotal = computedTaxable + computedCgst + computedSgst + computedIgst
    } else {
        val amt = simpleAmount.toDoubleOrNull() ?: 0.0
        computedTaxable = amt
        computedGrandTotal = amt
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New $voucherType Entry", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .testTag("create_voucher_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Voucher Header Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = voucherNumber,
                                onValueChange = { voucherNumber = it },
                                label = { Text("Voucher / Bill No.") },
                                modifier = Modifier.weight(1f).testTag("input_voucher_number"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = dateString,
                                onValueChange = { dateString = it },
                                label = { Text("Date") },
                                modifier = Modifier.weight(1f).testTag("input_voucher_date"),
                                singleLine = true
                            )
                        }

                        // Party Selector
                        ExposedDropdownMenuBox(
                            expanded = partyExpanded,
                            onExpandedChange = { partyExpanded = !partyExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedParty?.name ?: "Select Party / Ledger",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(if (voucherType in listOf("SALES", "RECEIPT")) "Customer / Party" else "Supplier / Account") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().testTag("input_party_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = partyExpanded,
                                onDismissRequest = { partyExpanded = false }
                            ) {
                                filteredParties.forEach { party ->
                                    DropdownMenuItem(
                                        text = { Text("${party.name} (${party.groupName}) - Bal: ₹${String.format("%.2f", party.currentBalance)}") },
                                        onClick = {
                                            selectedParty = party
                                            partyExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Second Ledger for Receipt/Payment/Contra
                        if (!isMultiItem) {
                            ExposedDropdownMenuBox(
                                expanded = secondLedgerExpanded,
                                onExpandedChange = { secondLedgerExpanded = !secondLedgerExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedSecondLedger?.name ?: "Select Bank / Cash",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (voucherType == "RECEIPT") "Deposit Into (Bank/Cash)" else "Pay From (Bank/Cash)") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = secondLedgerExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = secondLedgerExpanded,
                                    onDismissRequest = { secondLedgerExpanded = false }
                                ) {
                                    bankAndCashLedgers.forEach { b ->
                                        DropdownMenuItem(
                                            text = { Text("${b.name} - Bal: ₹${String.format("%.2f", b.currentBalance)}") },
                                            onClick = {
                                                selectedSecondLedger = b
                                                secondLedgerExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = simpleAmount,
                                onValueChange = { simpleAmount = it },
                                label = { Text("Amount (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth().testTag("input_simple_amount"),
                                singleLine = true
                            )
                        }

                        // Payment Mode Selector
                        ExposedDropdownMenuBox(
                            expanded = modeExpanded,
                            onExpandedChange = { modeExpanded = !modeExpanded }
                        ) {
                            OutlinedTextField(
                                value = paymentMode,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Payment Mode") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = modeExpanded,
                                onDismissRequest = { modeExpanded = false }
                            ) {
                                listOf("CREDIT", "CASH", "BANK", "UPI", "CARD").forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode) },
                                        onClick = {
                                            paymentMode = mode
                                            modeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Line Items (for Sales / Purchase)
            if (isMultiItem) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Item Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                val item = itemsList.firstOrNull()
                                lineItems.add(
                                    DraftItemRow(
                                        itemId = item?.id ?: 0,
                                        itemName = item?.name ?: "Item ${lineItems.size + 1}",
                                        rate = (if (voucherType == "SALES") item?.salesPrice else item?.purchasePrice ?: 100.0).toString(),
                                        unit = item?.unit ?: "PCS",
                                        gstRate = item?.gstRate ?: 18.0
                                    )
                                )
                            },
                            modifier = Modifier.testTag("btn_add_line_item")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }
                }

                itemsIndexed(lineItems) { index, row ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Item #${index + 1}", fontWeight = FontWeight.Bold)
                                if (lineItems.size > 1) {
                                    IconButton(onClick = { lineItems.removeAt(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Item name selector
                            var itemMenuExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = itemMenuExpanded,
                                onExpandedChange = { itemMenuExpanded = !itemMenuExpanded }
                            ) {
                                OutlinedTextField(
                                    value = row.itemName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Product / Item") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = itemMenuExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = itemMenuExpanded,
                                    onDismissRequest = { itemMenuExpanded = false }
                                ) {
                                    itemsList.forEach { itm ->
                                        DropdownMenuItem(
                                            text = { Text("${itm.name} (Stock: ${itm.currentStock} ${itm.unit})") },
                                            onClick = {
                                                val rateVal = if (voucherType == "SALES") itm.salesPrice else itm.purchasePrice
                                                lineItems[index] = row.copy(
                                                    itemId = itm.id,
                                                    itemName = itm.name,
                                                    hsn = itm.hsnSac,
                                                    rate = rateVal.toString(),
                                                    unit = itm.unit,
                                                    gstRate = itm.gstRate
                                                )
                                                itemMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = row.quantity,
                                    onValueChange = { lineItems[index] = row.copy(quantity = it) },
                                    label = { Text("Qty (${row.unit})") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = row.rate,
                                    onValueChange = { lineItems[index] = row.copy(rate = it) },
                                    label = { Text("Rate (₹)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = "${row.gstRate.toInt()}%",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("GST") },
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Narration
            item {
                OutlinedTextField(
                    value = narration,
                    onValueChange = { narration = it },
                    label = { Text("Narration / Remarks / Delivery Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }

            // 4. Financial Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable Subtotal:", style = MaterialTheme.typography.bodyMedium)
                            Text("₹${String.format("%,.2f", computedTaxable)}", fontWeight = FontWeight.Bold)
                        }
                        if (computedCgst > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CGST:", style = MaterialTheme.typography.bodySmall)
                                Text("₹${String.format("%,.2f", computedCgst)}")
                            }
                        }
                        if (computedSgst > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SGST:", style = MaterialTheme.typography.bodySmall)
                                Text("₹${String.format("%,.2f", computedSgst)}")
                            }
                        }
                        if (computedIgst > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("IGST (Inter-state):", style = MaterialTheme.typography.bodySmall)
                                Text("₹${String.format("%,.2f", computedIgst)}")
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("₹${String.format("%,.2f", computedGrandTotal)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error display
            if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // 5. Submit Button
            item {
                Button(
                    onClick = {
                        val party = selectedParty
                        if (party == null) {
                            errorMessage = "Please select a party account."
                            return@Button
                        }
                        if (computedGrandTotal <= 0.0) {
                            errorMessage = "Total amount must be greater than zero."
                            return@Button
                        }

                        val compId = company?.id ?: 1L
                        val newVoucher = VoucherEntity(
                            companyId = compId,
                            voucherType = voucherType,
                            voucherNumber = voucherNumber,
                            dateMillis = System.currentTimeMillis(),
                            dateString = dateString,
                            partyLedgerId = party.id,
                            partyLedgerName = party.name,
                            secondLedgerId = selectedSecondLedger?.id ?: 0,
                            secondLedgerName = selectedSecondLedger?.name ?: "",
                            paymentMode = paymentMode,
                            subTotal = computedTaxable,
                            discountAmount = 0.0,
                            taxableAmount = computedTaxable,
                            cgstAmount = computedCgst,
                            sgstAmount = computedSgst,
                            igstAmount = computedIgst,
                            totalGst = computedCgst + computedSgst + computedIgst,
                            grandTotal = computedGrandTotal,
                            narration = narration
                        )

                        val voucherItems = if (isMultiItem) {
                            lineItems.map { row ->
                                val qty = row.quantity.toDoubleOrNull() ?: 1.0
                                val rate = row.rate.toDoubleOrNull() ?: 0.0
                                val disc = row.discount.toDoubleOrNull() ?: 0.0
                                val taxable = (qty * rate) - disc
                                val gst = taxable * (row.gstRate / 100.0)
                                VoucherItemEntity(
                                    voucherId = 0,
                                    itemId = row.itemId,
                                    itemName = row.itemName,
                                    hsnSac = row.hsn,
                                    quantity = qty,
                                    unit = row.unit,
                                    rate = rate,
                                    discount = disc,
                                    taxableAmount = taxable,
                                    gstRate = row.gstRate,
                                    cgst = if (isInterState) 0.0 else gst / 2.0,
                                    sgst = if (isInterState) 0.0 else gst / 2.0,
                                    igst = if (isInterState) gst else 0.0,
                                    totalAmount = taxable + gst
                                )
                            }
                        } else emptyList()

                        viewModel.createVoucher(newVoucher, voucherItems) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_save_voucher")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save & Post Voucher", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
