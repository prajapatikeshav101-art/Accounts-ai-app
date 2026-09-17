package com.example.ui.settings

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.model.UserEntity
import com.example.ui.AccountsViewModel
import com.example.ui.theme.PrimaryNavy
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: AccountsViewModel,
    modifier: Modifier = Modifier
) {
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val currentCompany by viewModel.currentCompany.collectAsStateWithLifecycle()
    val users by viewModel.users.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var showCreateCompanyDialog by remember { mutableStateOf(false) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var showAuditTrailDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Language Preference
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = PrimaryNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Application Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                    Text("Select your primary regional accounting language:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("English", "हिन्दी (Hindi)", "ગુજરાતી (Gujarati)").forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Multi-Company Switcher & Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = PrimaryNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Companies / Businesses (${companies.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showCreateCompanyDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Company", tint = PrimaryNavy)
                        }
                    }

                    companies.forEach { comp ->
                        val isSelected = comp.id == (currentCompany?.id ?: 1L)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectCompany(comp.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(comp.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("${comp.businessType} • GSTIN: ${comp.gstin}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. User & Role Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PrimaryNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("User Roles & Permissions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        IconButton(onClick = { showCreateUserDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add User", tint = PrimaryNavy)
                        }
                    }

                    users.forEach { u ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(u.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Role: ${u.role} • ${u.email}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(
                                    checked = u.isActive,
                                    onCheckedChange = { viewModel.toggleUserStatus(u) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Audit Trail
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAuditTrailDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = PrimaryNavy)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Audit Trail & Security Logs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("${auditLogs.size} recorded system actions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    TextButton(onClick = { showAuditTrailDialog = true }) {
                        Text("View Logs")
                    }
                }
            }
        }

        // 5. System Info & Security
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Prajapati Accounts AI • Enterprise Edition", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text("Version 1.0.0 • Offline-First Double-Entry Core Engine", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("Local SQLite Room Encryption & Instant GST GSTR-3B Reconciler", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }

    // Create Company Dialog
    if (showCreateCompanyDialog) {
        CompanyFormDialog(
            onDismiss = { showCreateCompanyDialog = false },
            onSave = { newComp ->
                viewModel.createCompany(newComp)
                showCreateCompanyDialog = false
            }
        )
    }

    // Create User Dialog
    if (showCreateUserDialog) {
        UserFormDialog(
            onDismiss = { showCreateUserDialog = false },
            onSave = { newUser ->
                viewModel.createUser(newUser)
                showCreateUserDialog = false
            }
        )
    }

    // Audit Trail Modal
    if (showAuditTrailDialog) {
        AlertDialog(
            onDismissRequest = { showAuditTrailDialog = false },
            title = { Text("System Audit Trail (${auditLogs.size})", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(auditLogs) { log ->
                        val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(log.action, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = PrimaryNavy)
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                }
                                Text(log.details, style = MaterialTheme.typography.bodySmall)
                                Text("User: ${log.userName}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAuditTrailDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CompanyFormDialog(
    onDismiss: () -> Unit,
    onSave: (CompanyEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var businessType by remember { mutableStateOf("Wholesale & Retail") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Ahmedabad") }
    var state by remember { mutableStateOf("Gujarat") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var pan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Business / Company", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    OutlinedTextField(value = businessType, onValueChange = { businessType = it }, label = { Text("Business Type") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = gstin, onValueChange = { gstin = it }, label = { Text("GSTIN") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = pan, onValueChange = { pan = it }, label = { Text("PAN") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                }
                item {
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Office Address") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            CompanyEntity(
                                name = name,
                                businessType = businessType,
                                address = address,
                                city = city,
                                state = state,
                                mobileNumber = mobile,
                                email = email,
                                gstin = gstin,
                                pan = pan
                            )
                        )
                    }
                }
            ) {
                Text("Create Company")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormDialog(
    onDismiss: () -> Unit,
    onSave: (UserEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Accountant") }
    var roleExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add User & Assign Role", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = mobile, onValueChange = { mobile = it }, label = { Text("Mobile Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        listOf("Admin", "Accountant", "Sales User", "Purchase User", "Viewer").forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = { role = r; roleExpanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            UserEntity(
                                name = name,
                                email = email,
                                mobile = mobile,
                                role = role
                            )
                        )
                    }
                }
            ) {
                Text("Add User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
