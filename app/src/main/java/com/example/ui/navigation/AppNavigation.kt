package com.example.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.AccountsViewModel
import com.example.ui.ai.AiToolsScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.excel.ExcelScreen
import com.example.ui.inventory.InventoryScreen
import com.example.ui.ledgers.LedgersScreen
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.IndianRupeeGold
import com.example.ui.theme.PrimaryNavy
import com.example.ui.vouchers.CreateVoucherScreen
import com.example.ui.vouchers.VouchersScreen

sealed class Screen(val route: String, val title: String) {
    object Dashboard : Screen("dashboard", "Dashboard")
    object Transactions : Screen("transactions", "Transactions")
    object Inventory : Screen("inventory", "Inventory")
    object Ledgers : Screen("ledgers", "Parties")
    object Reports : Screen("reports", "Reports")
    object AiTools : Screen("ai_tools", "AI Hub")
    object Excel : Screen("excel", "Excel")
    object Settings : Screen("settings", "Settings")
    object CreateVoucher : Screen("create_voucher/{type}", "New Voucher") {
        fun createRoute(type: String) = "create_voucher/$type"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: AccountsViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val currentCompany by viewModel.currentCompany.collectAsStateWithLifecycle()
    val auditIssues by viewModel.auditIssues.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    var showCompanyDropdown by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        Screen.Dashboard to (Icons.Default.Dashboard to AppStrings.get("tab_dashboard", language)),
        Screen.Transactions to (Icons.Default.ReceiptLong to AppStrings.get("tab_transactions", language)),
        Screen.Inventory to (Icons.Default.Inventory2 to AppStrings.get("tab_inventory", language)),
        Screen.Ledgers to (Icons.Default.People to "Parties"),
        Screen.Reports to (Icons.Default.Analytics to AppStrings.get("tab_reports", language)),
        Screen.AiTools to (Icons.Default.AutoAwesome to "AI Hub"),
        Screen.Settings to (Icons.Default.Settings to "Settings")
    )

    Scaffold(
        topBar = {
            if (!currentRoute.startsWith("create_voucher")) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCompanyDropdown = true }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IndianRupeeGold.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("₹", fontWeight = FontWeight.Bold, color = IndianRupeeGold, fontSize = 18.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentCompany?.name ?: "Keshav Traders",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Switch Company",
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                                Text(
                                    text = "Prajapati Accounts AI • ${currentCompany?.city ?: "Ahmedabad"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }

                            DropdownMenu(
                                expanded = showCompanyDropdown,
                                onDismissRequest = { showCompanyDropdown = false }
                            ) {
                                companies.forEach { comp ->
                                    DropdownMenuItem(
                                        text = { Text(comp.name, fontWeight = if (comp.id == currentCompany?.id) FontWeight.Bold else FontWeight.Normal) },
                                        onClick = {
                                            viewModel.selectCompany(comp.id)
                                            showCompanyDropdown = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ Manage Companies in Settings", color = PrimaryNavy, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        showCompanyDropdown = false
                                        navController.navigate(Screen.Settings.route)
                                    }
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.AiTools.route) }) {
                            BadgedBox(
                                badge = {
                                    if (auditIssues.isNotEmpty()) {
                                        Badge { Text(auditIssues.size.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI Hub",
                                    tint = IndianRupeeGold
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (!currentRoute.startsWith("create_voucher")) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { (screen, iconAndLabel) ->
                        val (icon, label) = iconAndLabel
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                if (screen == Screen.AiTools && auditIssues.isNotEmpty()) {
                                    BadgedBox(badge = { Badge { Text(auditIssues.size.toString()) } }) {
                                        Icon(icon, contentDescription = label)
                                    }
                                } else {
                                    Icon(icon, contentDescription = label)
                                }
                            },
                            label = { Text(label, maxLines = 1, fontSize = 10.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCreateVoucher = { type -> navController.navigate(Screen.CreateVoucher.createRoute(type)) },
                    onNavigateToCreateLedger = { navController.navigate(Screen.Ledgers.route) },
                    onNavigateToCreateItem = { navController.navigate(Screen.Inventory.route) },
                    onNavigateToAiChecker = { navController.navigate(Screen.AiTools.route) },
                    onNavigateToAiAssistant = { navController.navigate(Screen.AiTools.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                    onNavigateToExcel = { navController.navigate(Screen.Excel.route) },
                    onVoucherClick = { navController.navigate(Screen.Transactions.route) }
                )
            }

            composable(Screen.Transactions.route) {
                VouchersScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { type -> navController.navigate(Screen.CreateVoucher.createRoute(type)) }
                )
            }

            composable(Screen.Inventory.route) {
                InventoryScreen(viewModel = viewModel)
            }

            composable(Screen.Ledgers.route) {
                LedgersScreen(viewModel = viewModel)
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = viewModel)
            }

            composable(Screen.AiTools.route) {
                AiToolsScreen(viewModel = viewModel)
            }

            composable(Screen.Excel.route) {
                ExcelScreen(viewModel = viewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }

            composable("create_voucher/{type}") { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type") ?: "SALES"
                CreateVoucherScreen(
                    initialType = type,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
