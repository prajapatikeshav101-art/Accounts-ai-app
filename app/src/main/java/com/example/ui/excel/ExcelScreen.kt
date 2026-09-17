package com.example.ui.excel

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.importer.ExcelCsvManager
import com.example.data.importer.ImportResult
import com.example.ui.AccountsViewModel
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen

@Composable
fun ExcelScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val ledgers by viewModel.ledgers.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Import, 1: Export

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("excel_screen")
    ) {
        TabRow(selectedTabIndex = activeTab) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Import Data (Excel/CSV)", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Export Reports", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
            )
        }

        if (activeTab == 0) {
            ExcelImportContent(viewModel)
        } else {
            ExcelExportContent(
                onExportLedgers = { ExcelCsvManager.exportLedgersToCsv(ledgers) },
                onExportItems = { ExcelCsvManager.exportItemsToCsv(items) },
                onExportVouchers = { ExcelCsvManager.exportVouchersToCsv(vouchers) }
            )
        }
    }
}

@Composable
fun ExcelImportContent(viewModel: AccountsViewModel) {
    var selectedDataType by remember { mutableStateOf("LEDGERS") } // "LEDGERS", "ITEMS"
    var csvInputText by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1: Select Type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Step 1: Select Import Dataset", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedDataType == "LEDGERS",
                            onClick = {
                                selectedDataType = "LEDGERS"
                                csvInputText = ExcelCsvManager.LEDGER_TEMPLATE
                                importResult = null
                            },
                            label = { Text("Ledgers & Parties") }
                        )
                        FilterChip(
                            selected = selectedDataType == "ITEMS",
                            onClick = {
                                selectedDataType = "ITEMS"
                                csvInputText = ExcelCsvManager.ITEM_TEMPLATE
                                importResult = null
                            },
                            label = { Text("Inventory Products") }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = {
                            csvInputText = if (selectedDataType == "LEDGERS") ExcelCsvManager.LEDGER_TEMPLATE else ExcelCsvManager.ITEM_TEMPLATE
                        }) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load Sample Template Data")
                        }
                    }
                }
            }
        }

        // Step 2: Input / Paste CSV content
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Step 2: CSV / Excel Data Input", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Columns are automatically mapped. You can paste spreadsheet rows or edit below:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    OutlinedTextField(
                        value = csvInputText,
                        onValueChange = {
                            csvInputText = it
                            importResult = null
                        },
                        placeholder = { Text("Paste CSV data here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("csv_input_field"),
                        maxLines = 10
                    )

                    Button(
                        onClick = {
                            importResult = if (selectedDataType == "LEDGERS") {
                                ExcelCsvManager.analyzeAndValidateLedgers(csvInputText, 1L)
                            } else {
                                ExcelCsvManager.analyzeAndValidateItems(csvInputText, 1L)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_validate_csv")
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyze & Validate Columns")
                    }
                }
            }
        }

        // Step 3: Preview & Validation Results
        importResult?.let { result ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Validation Results", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("${result.validRows} Valid / ${result.totalRows} Total", fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }

                        Divider()

                        result.previewRows.forEach { row ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (row.isValid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Row #${row.rowNumber}: ${row.mappedValues.values.take(3).joinToString(" • ")}",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = row.validationMessage,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (row.isValid) Color(0xFF2E7D32) else Color(0xFFC62828)
                                        )
                                    }
                                    Icon(
                                        imageVector = if (row.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (row.isValid) SuccessGreen else Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                if (selectedDataType == "LEDGERS") {
                                    viewModel.importLedgersFromCsv(csvInputText) { count ->
                                        successMessage = "Successfully imported $count Ledgers into the company database!"
                                        importResult = null
                                    }
                                } else {
                                    viewModel.importItemsFromCsv(csvInputText) { count ->
                                        successMessage = "Successfully imported $count Products into Inventory!"
                                        importResult = null
                                    }
                                }
                            },
                            enabled = result.validRows > 0,
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_confirm_import")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirm & Import ${result.validRows} Records")
                        }
                    }
                }
            }
        }

        // Success banner
        successMessage?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ExcelExportContent(
    onExportLedgers: () -> String,
    onExportItems: () -> String,
    onExportVouchers: () -> String
) {
    var previewExportText by remember { mutableStateOf<String?>(null) }
    var previewTitle by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Export Company Accounting Records", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Exports formatted CSV spreadsheets compatible with Microsoft Excel, Google Sheets, and Tally.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    ExportActionTile(
                        title = "Export Chart of Accounts & Ledgers",
                        subtitle = "Debtors, Creditors, Balances, GSTIN, Contact info",
                        onClick = {
                            previewTitle = "Ledgers & Parties Export"
                            previewExportText = onExportLedgers()
                        }
                    )

                    ExportActionTile(
                        title = "Export Inventory & Stock Master",
                        subtitle = "Item names, SKU, Barcode, HSN, Tax rates, Stock, Cost",
                        onClick = {
                            previewTitle = "Inventory Products Export"
                            previewExportText = onExportItems()
                        }
                    )

                    ExportActionTile(
                        title = "Export Invoices & Day Book Register",
                        subtitle = "Sales, Purchases, Receipts, Payments, Tax breakdowns",
                        onClick = {
                            previewTitle = "Vouchers & Transactions Export"
                            previewExportText = onExportVouchers()
                        }
                    )
                }
            }
        }
    }

    previewExportText?.let { content ->
        AlertDialog(
            onDismissRequest = { previewExportText = null },
            title = { Text(previewTitle, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exported CSV Data Content:", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = content,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(260.dp),
                        maxLines = 12
                    )
                }
            },
            confirmButton = {
                Button(onClick = { previewExportText = null }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { previewExportText = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ExportActionTile(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Icon(Icons.Default.Download, contentDescription = null, tint = PrimaryNavy)
        }
    }
}
