@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.statements

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.CashBreakdown
import id.kalkulasa.domain.model.CategorySpend
import id.kalkulasa.domain.model.Neraca
import id.kalkulasa.domain.usecase.LaporanKeuanganUseCase
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.reports.ReportPeriod
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun StatementsScreen(
    onBack: () -> Unit,
    viewModel: StatementsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    OnLifecycleResume { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.statements_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(context.getString(R.string.tab_balance)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(context.getString(R.string.tab_cash_detail)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(context.getString(R.string.tab_expense_categories)) })
            }
            if (state.loading) {
                Text(
                    context.getString(R.string.loading),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                when (tab) {
                    0 -> state.neraca?.let { BalanceTab(it, state.modalInfo, state.labaDitahanInfo) }
                    1 -> state.breakdown?.let { CashDetailTab(it) }
                    2 -> CategoriesTab(
                        categories = state.categories,
                        period = state.period,
                        onPeriod = viewModel::selectPeriod,
                    )
                }
            }
        }
    }
}

@Composable
private fun BalanceTab(neraca: Neraca, modal: Long, retained: Long) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SummaryCard(context.getString(R.string.balance_assets), RupiahFormatter.format(neraca.aset), Sukses)
            SummaryCard(context.getString(R.string.balance_liabilities), RupiahFormatter.format(neraca.liabilitas), Rugi)
            SummaryCard(context.getString(R.string.balance_equity), RupiahFormatter.format(neraca.ekuitas), MaterialTheme.colorScheme.primary)
        }
        item {
            DetailCard(
                title = context.getString(R.string.balance_assets),
                rows = listOf(
                    context.getString(R.string.balance_cash) to neraca.kas,
                    context.getString(R.string.balance_receivables) to neraca.piutang,
                    context.getString(R.string.balance_inventory) to neraca.persediaan,
                    context.getString(R.string.cash_kasbon) to neraca.kasbon,
                ),
            )
            DetailCard(
                title = context.getString(R.string.balance_liabilities),
                rows = listOf(context.getString(R.string.balance_payables) to neraca.hutangUsaha),
            )
            DetailCard(
                title = context.getString(R.string.balance_equity),
                rows = listOf(
                    context.getString(R.string.balance_capital) to modal,
                    context.getString(R.string.balance_retained) to retained,
                ),
            )
        }
    }
}

@Composable
private fun CashDetailTab(breakdown: CashBreakdown) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SummaryCard(context.getString(R.string.cash_net), RupiahFormatter.format(breakdown.saldo), MaterialTheme.colorScheme.primary)
            DetailCard(
                title = context.getString(R.string.cash_total_in),
                rows = listOf(
                    context.getString(R.string.cash_modal) to breakdown.modal,
                    context.getString(R.string.cash_sales) to breakdown.penjualanTunai,
                    context.getString(R.string.cash_receivables) to breakdown.penerimaanPiutang,
                ),
                total = breakdown.totalMasuk,
            )
            DetailCard(
                title = context.getString(R.string.cash_total_out),
                rows = listOf(
                    context.getString(R.string.cash_prive) to breakdown.prive,
                    context.getString(R.string.cash_purchases) to breakdown.pembelianStok,
                    context.getString(R.string.cash_payables) to breakdown.pembayaranHutang,
                    context.getString(R.string.cash_kasbon) to breakdown.kasbon,
                    context.getString(R.string.cash_expenses) to breakdown.pengeluaran,
                ),
                total = breakdown.totalKeluar,
            )
        }
    }
}

@Composable
private fun CategoriesTab(
    categories: List<CategorySpend>,
    period: ReportPeriod,
    onPeriod: (ReportPeriod) -> Unit,
) {
    val context = LocalContext.current
    val total = categories.sumOf { it.total }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                ReportPeriod.TODAY to R.string.period_today,
                ReportPeriod.DAYS_7 to R.string.period_7_days,
                ReportPeriod.DAYS_30 to R.string.period_30_days,
                ReportPeriod.THIS_MONTH to R.string.period_this_month,
            ).forEach { (value, label) ->
                FilterChip(
                    selected = period == value,
                    onClick = { onPeriod(value) },
                    label = { Text(context.getString(label)) },
                )
            }
        }
        if (categories.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.expense_categories_empty), body = "")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories, key = { it.nama }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${item.nama} · ${String.format(java.util.Locale.getDefault(), "%.1f%%", LaporanKeuanganUseCase.persentase(item.total, total))}",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(RupiahFormatter.format(item.total), fontWeight = FontWeight.SemiBold, color = Rugi)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DetailCard(title: String, rows: List<Pair<String, Long>>, total: Long? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            rows.forEach { (label, value) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                    Text(RupiahFormatter.format(value), style = MaterialTheme.typography.bodyMedium)
                }
            }
            if (total != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(RupiahFormatter.format(total), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
