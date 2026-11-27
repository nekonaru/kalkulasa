package id.kalkulasa.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.Settings
import id.kalkulasa.R
import id.kalkulasa.presentation.categories.CategoriesScreen
import id.kalkulasa.presentation.employee.EmployeeScreen
import id.kalkulasa.presentation.payable.PayableScreen
import id.kalkulasa.presentation.statements.StatementsScreen
import id.kalkulasa.presentation.target.TargetScreen
import id.kalkulasa.presentation.opname.StockOpnameScreen
import id.kalkulasa.presentation.activity.ActivityScreen
import id.kalkulasa.presentation.aging.AgingScreen
import id.kalkulasa.presentation.insights.InsightsScreen
import id.kalkulasa.presentation.paymenthistory.PaymentHistoryScreen
import id.kalkulasa.presentation.attendancereport.AttendanceReportScreen
import id.kalkulasa.presentation.promotion.PromotionScreen
import id.kalkulasa.presentation.financemore.FinanceMoreScreen
import id.kalkulasa.presentation.crm.CrmScreen
import id.kalkulasa.presentation.datatools.DataToolsScreen
import id.kalkulasa.presentation.kasirpro.KasirProScreen
import id.kalkulasa.presentation.cashier.CashierScreen
import id.kalkulasa.presentation.customer.CustomersScreen
import id.kalkulasa.presentation.finance.FinanceScreen
import id.kalkulasa.presentation.home.HomeScreen
import id.kalkulasa.presentation.piutang.PiutangScreen
import id.kalkulasa.presentation.products.ImportProductsScreen
import id.kalkulasa.presentation.supplier.SupplierPurchasesScreen
import id.kalkulasa.presentation.supplier.SupplierScreen
import id.kalkulasa.presentation.sales.SalesHistoryScreen
import id.kalkulasa.presentation.stock.RestockScreen
import id.kalkulasa.presentation.onboarding.OnboardingScreen
import id.kalkulasa.presentation.reports.ReportsScreen
import id.kalkulasa.presentation.settings.SettingsScreen
import id.kalkulasa.presentation.stock.StockScreen

private data class TabItem(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, R.string.nav_home, Icons.Rounded.Home),
    TabItem(Routes.CASHIER, R.string.nav_cashier, Icons.Rounded.PointOfSale),
    TabItem(Routes.STOCK, R.string.nav_stock, Icons.Rounded.Inventory2),
    TabItem(Routes.FINANCE, R.string.nav_finance, Icons.Rounded.AccountBalanceWallet),
    TabItem(Routes.REPORTS, R.string.nav_reports, Icons.Rounded.Assessment),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KalkulasaNavHost(startDestination: String) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val showChrome = currentRoute in tabs.map { it.route }

    Scaffold(
        topBar = {
            if (showChrome) {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                            Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.nav_settings))
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showChrome) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.labelRes)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onDone = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onOpenCashier = { navController.navigateSingleTopTo(Routes.CASHIER) },
                    onOpenPiutang = { navController.navigate(Routes.PIUTANG) },
                )
            }
            composable(Routes.CASHIER) {
                CashierScreen(
                    onOpenSalesHistory = { navController.navigate(Routes.SALES_HISTORY) },
                )
            }
            composable(Routes.STOCK) {
                StockScreen(
                    onOpenRestock = { navController.navigate(Routes.RESTOCK) },
                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                    onOpenImport = { navController.navigate(Routes.IMPORT_PRODUCTS) },
                    onOpenOpname = { navController.navigate(Routes.OPNAME) },
                )
            }
            composable(Routes.OPNAME) {
                StockOpnameScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CATEGORIES) {
                CategoriesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.SALES_HISTORY) {
                SalesHistoryScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.IMPORT_PRODUCTS) {
                ImportProductsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.FINANCE) {
                FinanceScreen(
                    onOpenPiutang = { navController.navigate(Routes.PIUTANG) },
                    onOpenCustomers = { navController.navigate(Routes.CUSTOMERS) },
                    onOpenSuppliers = { navController.navigate(Routes.SUPPLIERS) },
                    onOpenEmployees = { navController.navigate(Routes.EMPLOYEES) },
                    onOpenPayable = { navController.navigate(Routes.PAYABLE) },
                    onOpenStatements = { navController.navigate(Routes.STATEMENTS) },
                    onOpenTarget = { navController.navigate(Routes.TARGET) },
                    onOpenActivity = { navController.navigate(Routes.ACTIVITY) },
                    onOpenAging = { navController.navigate(Routes.AGING) },
                    onOpenInsights = { navController.navigate(Routes.INSIGHTS) },
                    onOpenPromotion = { navController.navigate(Routes.PROMOTION) },
                    onOpenFinanceMore = { navController.navigate(Routes.FINANCE_MORE) },
                    onOpenCrm = { navController.navigate(Routes.CRM) },
                    onOpenDataTools = { navController.navigate(Routes.DATA_TOOLS) },
                    onOpenKasirPro = { navController.navigate(Routes.KASIR_PRO) },
                )
            }
            composable(Routes.ACTIVITY) { ActivityScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.AGING) { AgingScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.INSIGHTS) { InsightsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PAYMENT_HISTORY) { PaymentHistoryScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ATTENDANCE_REPORT) { AttendanceReportScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.PROMOTION) { PromotionScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.FINANCE_MORE) { FinanceMoreScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.CRM) { CrmScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.DATA_TOOLS) { DataToolsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.KASIR_PRO) { KasirProScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.TARGET) {
                TargetScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.STATEMENTS) {
                StatementsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PAYABLE) {
                PayableScreen(
                    onBack = { navController.popBackStack() },
                    onOpenHistory = { navController.navigate(Routes.PAYMENT_HISTORY) },
                )
            }
            composable(Routes.EMPLOYEES) {
                EmployeeScreen(
                    onBack = { navController.popBackStack() },
                    onOpenReport = { navController.navigate(Routes.ATTENDANCE_REPORT) },
                )
            }
            composable(Routes.SUPPLIERS) {
                SupplierScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSupplier = { name ->
                        val key = name.ifBlank { Routes.NO_SUPPLIER }
                        navController.navigate("${Routes.SUPPLIER_DETAIL}/${java.net.URLEncoder.encode(key, "UTF-8")}")
                    },
                )
            }
            composable("${Routes.SUPPLIER_DETAIL}/{supplier}") {
                SupplierPurchasesScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.PIUTANG) {
                PiutangScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.CUSTOMERS) {
                CustomersScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.RESTOCK) {
                RestockScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.REPORTS) { ReportsScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

private fun NavHostController.navigateSingleTopTo(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
