@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package id.kalkulasa.presentation.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.CapitalEntryType
import id.kalkulasa.domain.model.Category
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FinanceScreen(
    onOpenPiutang: () -> Unit,
    onOpenCustomers: () -> Unit,
    onOpenSuppliers: () -> Unit,
    onOpenEmployees: () -> Unit,
    onOpenPayable: () -> Unit,
    onOpenStatements: () -> Unit,
    onOpenTarget: () -> Unit,
    onOpenActivity: () -> Unit,
    onOpenAging: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenPromotion: () -> Unit,
    onOpenFinanceMore: () -> Unit,
    onOpenCrm: () -> Unit,
    onOpenDataTools: () -> Unit,
    onOpenKasirPro: () -> Unit,
    onOpenGudangPro: () -> Unit,
    onOpenKeuanganPro: () -> Unit,
    onOpenKaryawanPro: () -> Unit,
    viewModel: FinanceViewModel = hiltViewModel(),
) {
    val balance by viewModel.cashBalance.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val categories by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var sheet by remember { mutableStateOf<SheetMode?>(null) }

    OnLifecycleResume { viewModel.refreshBalance() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is FinanceEvent.Message -> snackbar.showSnackbar(
                    if (event.text == "SAVED") context.getString(R.string.saved_generic) else event.text,
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { sheet = SheetMode.EXPENSE },
                icon = { Icon(Icons.Rounded.Payments, contentDescription = null) },
                text = { Text(context.getString(R.string.add_expense)) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Balance card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        context.getString(R.string.cash_balance),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    Text(
                        RupiahFormatter.format(balance),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { sheet = SheetMode.CAPITAL }) { Text(context.getString(R.string.add_capital)) }
                        TextButton(onClick = { sheet = SheetMode.PRIVATE_DRAW }) { Text(context.getString(R.string.add_private_draw)) }
                    }
                    TextButton(onClick = onOpenPiutang) { Text(context.getString(R.string.piutang_open)) }
                    TextButton(onClick = onOpenCustomers) { Text(context.getString(R.string.customers_title)) }
                    TextButton(onClick = onOpenSuppliers) { Text(context.getString(R.string.suppliers_entry)) }
                    TextButton(onClick = onOpenEmployees) { Text(context.getString(R.string.employees_title)) }
                    TextButton(onClick = onOpenPayable) { Text(context.getString(R.string.payable_open)) }
                    TextButton(onClick = onOpenStatements) { Text(context.getString(R.string.statements_open)) }
                    TextButton(onClick = onOpenTarget) { Text(context.getString(R.string.target_title)) }
                    TextButton(onClick = onOpenInsights) { Text(context.getString(R.string.insights_open)) }
                    TextButton(onClick = onOpenActivity) { Text(context.getString(R.string.activity_open)) }
                    TextButton(onClick = onOpenAging) { Text(context.getString(R.string.aging_open)) }
                    TextButton(onClick = onOpenPromotion) { Text(context.getString(R.string.promotion_open)) }
                    TextButton(onClick = onOpenFinanceMore) { Text(context.getString(R.string.finance_more_open)) }
                    TextButton(onClick = onOpenCrm) { Text(context.getString(R.string.crm_open)) }
                    TextButton(onClick = onOpenDataTools) { Text(context.getString(R.string.data_tools_open)) }
                    TextButton(onClick = onOpenKasirPro) { Text(context.getString(R.string.kasir_pro_open)) }
                    TextButton(onClick = onOpenGudangPro) { Text(context.getString(R.string.gudang_pro_open)) }
                    TextButton(onClick = onOpenKeuanganPro) { Text(context.getString(R.string.keuangan_pro_open)) }
                    TextButton(onClick = onOpenKaryawanPro) { Text(context.getString(R.string.karyawan_pro_open)) }
                }
            }

            Text(
                context.getString(R.string.finance_history),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (items.isEmpty()) {
                KalkulasaEmptyState(
                    title = context.getString(R.string.finance_empty),
                    body = "",
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(items, key = { when (it) { is FinanceItem.Capital -> "c-${it.entry.id}"; is FinanceItem.ExpenseItem -> "e-${it.expense.id}"; is FinanceItem.Purchase -> "p-${it.purchase.id}" } }) { item ->
                        FinanceRow(item)
                    }
                }
            }
        }
    }

    when (sheet) {
        SheetMode.CAPITAL -> AmountSheet(
            title = context.getString(R.string.add_capital),
            showCategory = false,
            categories = categories,
            onDismiss = { sheet = null },
            onSave = { nominal, catatan, category ->
                viewModel.addCapital(nominal, catatan, CapitalEntryType.TAMBAH_MODAL)
                sheet = null
            },
        )
        SheetMode.PRIVATE_DRAW -> AmountSheet(
            title = context.getString(R.string.add_private_draw),
            showCategory = false,
            categories = categories,
            onDismiss = { sheet = null },
            onSave = { nominal, catatan, category ->
                viewModel.addCapital(nominal, catatan, CapitalEntryType.PRIVE)
                sheet = null
            },
        )
        SheetMode.EXPENSE -> AmountSheet(
            title = context.getString(R.string.add_expense),
            showCategory = true,
            categories = categories,
            onDismiss = { sheet = null },
            onSave = { nominal, catatan, category ->
                viewModel.addExpense(category?.id, category?.nama ?: context.getString(R.string.expense_lainnya), nominal, catatan)
                sheet = null
            },
        )
        null -> Unit
    }
}

private enum class SheetMode { CAPITAL, PRIVATE_DRAW, EXPENSE }

@Composable
private fun FinanceRow(item: FinanceItem) {
    val context = LocalContext.current
    val (label, signedNominal, isIncome) = when (item) {
        is FinanceItem.Capital -> {
            val entry = item.entry
            when (entry.tipe) {
                CapitalEntryType.MODAL_AWAL -> Triple(context.getString(R.string.entry_capital_initial), entry.nominal, true)
                CapitalEntryType.TAMBAH_MODAL -> Triple(context.getString(R.string.entry_capital_add), entry.nominal, true)
                CapitalEntryType.PRIVE -> Triple(context.getString(R.string.entry_private_draw), entry.nominal, false)
                else -> Triple(entry.tipe, entry.nominal, true)
            }
        }
        is FinanceItem.ExpenseItem -> Triple(
            "${context.getString(R.string.entry_expense)}: ${item.expense.kategoriNama}",
            item.expense.nominal,
            false,
        )
        is FinanceItem.Purchase -> {
            val pemasok = item.purchase.pemasok?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.supplier_unknown)
            Triple("${context.getString(R.string.entry_purchase)}: $pemasok", item.purchase.total, false)
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (isIncome) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = if (isIncome) Sukses else Rugi,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    DateFormatter.formatDateTime(item.waktu) + (when (item) {
                        is FinanceItem.Capital -> item.entry.catatan
                        is FinanceItem.ExpenseItem -> item.expense.catatan
                        is FinanceItem.Purchase -> item.purchase.catatan
                    }?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                (if (isIncome) "+" else "-") + RupiahFormatter.format(signedNominal),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isIncome) Sukses else Rugi,
            )
        }
    }
}

@Composable
private fun AmountSheet(
    title: String,
    showCategory: Boolean,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (nominal: String, catatan: String, category: Category?) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nominal by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            if (showCategory) {
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(
                        value = selectedCategory?.nama ?: context.getString(R.string.expense_lainnya),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(context.getString(R.string.expense_category)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.nama) }, onClick = { selectedCategory = category; categoryExpanded = false })
                        }
                    }
                }
            }
            OutlinedTextField(
                value = nominal,
                onValueChange = { nominal = it.filter(Char::isDigit).take(12) },
                label = { Text(context.getString(R.string.amount)) },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text(context.getString(R.string.note)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { onSave(nominal, catatan, selectedCategory) },
                enabled = nominal.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.button_save)) }
        }
    }
}
