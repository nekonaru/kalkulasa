@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun InsightsScreen(
    onBack: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.insights_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!state.hasData && !state.loading) {
                KalkulasaEmptyState(title = context.getString(R.string.insights_empty), body = "")
            } else {
                InsightCard(context.getString(R.string.insights_avg_sale), RupiahFormatter.format(state.averageSale))
                state.bestHour?.let { InsightCard(context.getString(R.string.insights_best_hour), String.format(java.util.Locale.getDefault(), "%02d:00", it)) }
                state.bestDay?.let { InsightCard(context.getString(R.string.insights_best_day), dayName(it)) }
                state.topCustomer?.let { InsightCard(context.getString(R.string.insights_top_customer), "${it.nama} (${it.kunjungan})") }
                state.topProduct?.let { InsightCard(context.getString(R.string.insights_top_margin), "${it.namaProduk} (${RupiahFormatter.format(it.profit)})") }
            }
        }
    }
}

private fun dayName(day: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.set(java.util.Calendar.DAY_OF_WEEK, day)
    return java.text.SimpleDateFormat("EEEE", java.util.Locale.getDefault()).format(calendar.time)
}

@Composable
private fun InsightCard(label: String, value: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, color = Sukses, style = MaterialTheme.typography.titleMedium)
        }
    }
}
