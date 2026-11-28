@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.keuanganlanjutan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.usecase.PiutangAgingUseCase
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.reports.ReportPeriod
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun KeuanganLanjutanScreen(
    onBack: () -> Unit,
    viewModel: KeuanganLanjutanViewModel = hiltViewModel(),
) {
    val period by viewModel.period.collectAsStateWithLifecycle()
    val flow by viewModel.cashFlow.collectAsStateWithLifecycle()
    val aging by viewModel.aging.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.keuangan_lanjutan_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    ReportPeriod.TODAY to R.string.period_today,
                    ReportPeriod.DAYS_7 to R.string.period_7_days,
                    ReportPeriod.DAYS_30 to R.string.period_30_days,
                    ReportPeriod.THIS_MONTH to R.string.period_this_month,
                ).forEach { (p, label) ->
                    FilterChip(selected = period == p, onClick = { viewModel.selectPeriod(p) }, label = { Text(context.getString(label)) })
                }
            }
            flow?.let { f ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(context.getString(R.string.cash_total_in), style = MaterialTheme.typography.titleMedium, color = Sukses)
                        Text(RupiahFormatter.format(f.masuk))
                        Text(context.getString(R.string.cash_total_out), style = MaterialTheme.typography.titleMedium, color = Rugi)
                        Text(RupiahFormatter.format(f.keluar))
                        Text("${context.getString(R.string.cash_net)}: ${RupiahFormatter.format(f.bersih)}")
                    }
                }
            }
            Text(context.getString(R.string.payable_aging_title), style = MaterialTheme.typography.titleMedium)
            listOf(
                PiutangAgingUseCase.Bucket.CURRENT to R.string.aging_current,
                PiutangAgingUseCase.Bucket.D1_30 to R.string.aging_1_30,
                PiutangAgingUseCase.Bucket.D31_60 to R.string.aging_31_60,
                PiutangAgingUseCase.Bucket.OVER_60 to R.string.aging_over_60,
            ).forEach { (bucket, label) ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(context.getString(label))
                        Text(RupiahFormatter.format(aging[bucket] ?: 0L))
                    }
                }
            }
        }
    }
}
