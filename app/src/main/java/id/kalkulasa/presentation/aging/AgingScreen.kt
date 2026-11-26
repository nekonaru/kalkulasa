@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.aging

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
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.usecase.PiutangAgingUseCase
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun AgingScreen(
    onBack: () -> Unit,
    viewModel: AgingViewModel = hiltViewModel(),
) {
    val totals by viewModel.totals.collectAsStateWithLifecycle()
    val outstanding by viewModel.outstanding.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.aging_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(context.getString(R.string.aging_total), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    Text(RupiahFormatter.format(outstanding), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            if (totals.isEmpty()) {
                Text(context.getString(R.string.aging_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                listOf(
                    PiutangAgingUseCase.Bucket.CURRENT to R.string.aging_current,
                    PiutangAgingUseCase.Bucket.D1_30 to R.string.aging_1_30,
                    PiutangAgingUseCase.Bucket.D31_60 to R.string.aging_31_60,
                    PiutangAgingUseCase.Bucket.OVER_60 to R.string.aging_over_60,
                ).forEach { (bucket, label) ->
                    val value = totals[bucket] ?: 0L
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(context.getString(label), style = MaterialTheme.typography.bodyLarge)
                            Text(RupiahFormatter.format(value), color = if (value > 0 && bucket != PiutangAgingUseCase.Bucket.CURRENT) Rugi else Sukses)
                        }
                    }
                }
            }
        }
    }
}
