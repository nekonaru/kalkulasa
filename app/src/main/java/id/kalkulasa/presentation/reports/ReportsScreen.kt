package id.kalkulasa.presentation.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.LabeledValueRow
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mostProfitable by viewModel.mostProfitable.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    OnLifecycleResume { viewModel.load() }
    LaunchedEffect(error) {
        error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                context.getString(R.string.reports_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            if (state.error) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(context.getString(R.string.error_generic), color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::load) { Text(context.getString(R.string.button_retry)) }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ReportPeriod.TODAY to R.string.period_today,
                    ReportPeriod.DAYS_7 to R.string.period_7_days,
                    ReportPeriod.DAYS_30 to R.string.period_30_days,
                    ReportPeriod.THIS_MONTH to R.string.period_this_month,
                ).forEach { (period, label) ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(context.getString(label)) },
                    )
                }
            }

            val report = state.report
            if (!state.loading && report.jumlahTransaksi == 0 && report.omzet == 0L) {
                KalkulasaEmptyState(title = context.getString(R.string.reports_empty), body = "")
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(context.getString(R.string.profit_loss), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    LabeledValueRow(context.getString(R.string.revenue), RupiahFormatter.format(report.omzet))
                    LabeledValueRow(context.getString(R.string.cogs), "- ${RupiahFormatter.format(report.hpp)}")
                    LabeledValueRow(context.getString(R.string.gross_profit), RupiahFormatter.format(report.labaKotor))
                    LabeledValueRow(context.getString(R.string.operating_expense), "- ${RupiahFormatter.format(report.pengeluaran)}")
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(context.getString(R.string.net_profit_full), style = MaterialTheme.typography.titleMedium)
                        Text(
                            RupiahFormatter.format(report.labaBersih),
                            style = MaterialTheme.typography.titleLarge,
                            color = if (report.labaBersih < 0) Rugi else Sukses,
                        )
                    }
                    LabeledValueRow(context.getString(R.string.margin), String.format(java.util.Locale.getDefault(), "%.1f%%", report.marginPersen))
                    LabeledValueRow(context.getString(R.string.inventory_value), RupiahFormatter.format(report.nilaiPersediaan))
                }
            }

            if (mostProfitable.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            context.getString(R.string.reports_most_profitable),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        mostProfitable.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.namaProduk, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                    Text(
                                        context.getString(R.string.units_sold, id.kalkulasa.core.util.DateFormatter.formatQty(item.qty)),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    RupiahFormatter.format(item.profit),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (item.profit >= 0) Sukses else Rugi,
                                )
                            }
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::exportPdf, enabled = !state.exporting) {
                    Text(context.getString(R.string.export_pdf))
                }
                OutlinedButton(onClick = viewModel::exportCsv, enabled = !state.exporting) {
                    Text(context.getString(R.string.export_csv))
                }
            }
            OutlinedButton(onClick = viewModel::shareWhatsapp) {
                Text(context.getString(R.string.share_whatsapp))
            }
        }
    }
}
