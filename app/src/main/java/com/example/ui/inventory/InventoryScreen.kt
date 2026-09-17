package com.example.ui.inventory

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ItemEntity
import com.example.ui.AccountsViewModel
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun InventoryScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val itemsList by viewModel.items.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.dashboardSummary.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }
    var itemToAdjust by remember { mutableStateOf<ItemEntity?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL") + itemsList.map { it.category }.distinct().filter { it.isNotBlank() }

    val filteredItems = itemsList.filter { item ->
        val matchesCategory = selectedCategory == "ALL" || item.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                item.name.contains(searchQuery, ignoreCase = true) ||
                item.itemCode.contains(searchQuery, ignoreCase = true) ||
                item.barcode.contains(searchQuery, ignoreCase = true) ||
                item.brand.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_item")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("inventory_screen")
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search item name, code, or barcode...") },
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
                    .testTag("inventory_search_field"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) }
                    )
                }
            }

            // Low stock count alert chip if any
            if (lowStockItems.lowStockCount > 0) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${lowStockItems.lowStockCount} items at or below reorder level.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Items List
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_card_${item.itemCode}"),
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
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Code: ${item.itemCode} • HSN: ${item.hsnSac} • GST: ${item.gstRate.toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                IconButton(onClick = { itemToEdit = item }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = PrimaryNavy)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Sales: ₹${item.salesPrice} / ${item.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SuccessGreen
                                    )
                                    Text(
                                        text = "Cost: ₹${item.purchasePrice} / ${item.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    val isLow = item.currentStock <= item.minStockLevel
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isLow) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = "Stock: ${item.currentStock} ${item.unit}",
                                            color = if (isLow) Color(0xFFC62828) else Color(0xFF2E7D32),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Val: ₹${String.format("%,.2f", item.currentStock * item.purchasePrice)}",
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
                                OutlinedButton(
                                    onClick = { itemToAdjust = item },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Adjust Stock", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Item Dialog
    if (showCreateDialog || itemToEdit != null) {
        val target = itemToEdit
        ItemFormDialog(
            initialItem = target,
            onDismiss = {
                showCreateDialog = false
                itemToEdit = null
            },
            onSave = { savedItem ->
                viewModel.saveItem(savedItem)
                showCreateDialog = false
                itemToEdit = null
            }
        )
    }

    // Stock Adjustment Dialog
    itemToAdjust?.let { item ->
        StockAdjustmentDialog(
            item = item,
            onDismiss = { itemToAdjust = null },
            onConfirm = { qty, reason ->
                viewModel.adjustStock(item.id, qty, reason)
                itemToAdjust = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemFormDialog(
    initialItem: ItemEntity?,
    onDismiss: () -> Unit,
    onSave: (ItemEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var code by remember { mutableStateOf(initialItem?.itemCode ?: "") }
    var barcode by remember { mutableStateOf(initialItem?.barcode ?: "") }
    var category by remember { mutableStateOf(initialItem?.category ?: "Electronics") }
    var unit by remember { mutableStateOf(initialItem?.unit ?: "PCS") }
    var hsn by remember { mutableStateOf(initialItem?.hsnSac ?: "") }
    var gstRateStr by remember { mutableStateOf(initialItem?.gstRate?.toInt()?.toString() ?: "18") }
    var purchasePrice by remember { mutableStateOf(initialItem?.purchasePrice?.toString() ?: "0.0") }
    var salesPrice by remember { mutableStateOf(initialItem?.salesPrice?.toString() ?: "0.0") }
    var openingStock by remember { mutableStateOf(initialItem?.openingStock?.toString() ?: "10.0") }
    var minLevel by remember { mutableStateOf(initialItem?.minStockLevel?.toString() ?: "5.0") }
    var warehouse by remember { mutableStateOf(initialItem?.warehouse ?: "Main Warehouse") }
    var rack by remember { mutableStateOf(initialItem?.rack ?: "A-1") }

    var unitExpanded by remember { mutableStateOf(false) }
    var gstExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "Add Inventory Product" else "Edit Product", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().testTag("item_form_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_item_name"),
                        singleLine = true
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = code,
                            onValueChange = { code = it },
                            label = { Text("Item Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = barcode,
                            onValueChange = { barcode = it },
                            label = { Text("Barcode") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = hsn,
                            onValueChange = { hsn = it },
                            label = { Text("HSN/SAC") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = !unitExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                listOf("PCS", "KGS", "BOX", "MTR", "LTR", "BAG", "NOS").forEach { u ->
                                    DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = gstExpanded,
                            onExpandedChange = { gstExpanded = !gstExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = "$gstRateStr%",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("GST Rate") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = gstExpanded,
                                onDismissRequest = { gstExpanded = false }
                            ) {
                                listOf("0", "5", "12", "18", "28").forEach { g ->
                                    DropdownMenuItem(text = { Text("$g%") }, onClick = { gstRateStr = g; gstExpanded = false })
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("Purchase Rate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = salesPrice,
                            onValueChange = { salesPrice = it },
                            label = { Text("Sales Rate") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = openingStock,
                            onValueChange = { openingStock = it },
                            label = { Text("Opening Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = minLevel,
                            onValueChange = { minLevel = it },
                            label = { Text("Min Level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = warehouse,
                            onValueChange = { warehouse = it },
                            label = { Text("Warehouse") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = rack,
                            onValueChange = { rack = it },
                            label = { Text("Rack") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val pRate = purchasePrice.toDoubleOrNull() ?: 0.0
                        val sRate = salesPrice.toDoubleOrNull() ?: 0.0
                        val opStock = openingStock.toDoubleOrNull() ?: 0.0
                        val minStock = minLevel.toDoubleOrNull() ?: 5.0
                        val gst = gstRateStr.toDoubleOrNull() ?: 18.0

                        onSave(
                            ItemEntity(
                                id = initialItem?.id ?: 0,
                                companyId = initialItem?.companyId ?: 1L,
                                name = name,
                                itemCode = if (code.isNotBlank()) code else "ITM-${(100..999).random()}",
                                barcode = barcode,
                                category = category,
                                unit = unit,
                                hsnSac = hsn,
                                gstRate = gst,
                                purchasePrice = pRate,
                                salesPrice = sRate,
                                openingStock = opStock,
                                currentStock = if (initialItem != null) initialItem.currentStock else opStock,
                                minStockLevel = minStock,
                                warehouse = warehouse,
                                rack = rack
                            )
                        )
                    }
                },
                modifier = Modifier.testTag("btn_save_item")
            ) {
                Text("Save Product")
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
fun StockAdjustmentDialog(
    item: ItemEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var adjustType by remember { mutableStateOf("IN") } // "IN" or "OUT"
    var quantityStr by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("Physical verification adjustment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Stock Adjustment: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Current Stock: ${item.currentStock} ${item.unit}", style = MaterialTheme.typography.bodyMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = adjustType == "IN",
                        onClick = { adjustType = "IN" },
                        label = { Text("+ Stock In (Add)") }
                    )
                    FilterChip(
                        selected = adjustType == "OUT",
                        onClick = { adjustType = "OUT" },
                        label = { Text("- Stock Out (Reduce)") }
                    )
                }

                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { quantityStr = it },
                    label = { Text("Adjustment Quantity (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Reason / Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull() ?: 0.0
                    if (qty > 0) {
                        val finalQty = if (adjustType == "IN") qty else -qty
                        onConfirm(finalQty, remarks)
                    }
                }
            ) {
                Text("Apply Adjustment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
