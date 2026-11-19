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
import id.kalkulasa.presentation.cashier.CashierScreen
import id.kalkulasa.presentation.customer.CustomersScreen
import id.kalkulasa.presentation.finance.FinanceScreen
import id.kalkulasa.presentation.home.HomeScreen
import id.kalkulasa.presentation.piutang.PiutangScreen
import id.kalkulasa.presentation.products.ImportProductsScreen
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
                )
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
                )
            }
            composable(Routes.SUPPLIERS) {
                SupplierScreen(
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
