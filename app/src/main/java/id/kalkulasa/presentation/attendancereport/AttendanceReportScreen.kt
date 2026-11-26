@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.attendancereport

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
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.reports.ReportPeriod

@Composable
fun AttendanceReportScreen(
    onBack: () -> Unit,
    viewModel: AttendanceReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.attendance_report_title)) },
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
                ).forEach { (period, label) ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(context.getString(label)) },
                    )
                }
            }
            if (state.rows.isEmpty() && !state.loading) {
                KalkulasaEmptyState(title = context.getString(R.string.attendance_report_empty), body = "")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.rows, key = { it.nama }) { row ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(row.nama, style = MaterialTheme.typography.titleMedium)
                                Text("${context.getString(R.string.attendance_report_days)}: ${row.hadir}")
                                Text("${context.getString(R.string.attendance_report_wage)}: ${RupiahFormatter.format(row.totalUpah)}")
                            }
                        }
                    }
                }
            }
        }
    }
}
