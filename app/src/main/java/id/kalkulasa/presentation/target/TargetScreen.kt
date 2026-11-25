@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.target

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.SalesTarget
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TargetScreen(
    onBack: () -> Unit,
    viewModel: TargetViewModel = hiltViewModel(),
) {
    val targets by viewModel.targets.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val monthly by viewModel.monthly.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is TargetEvent.Message -> snackbar.showSnackbar(
                    when (event.text) {
                        "SAVED" -> context.getString(R.string.saved_generic)
                        else -> context.getString(R.string.error_generic)
                    },
                )
            }
        }
    }

    val dailyTarget = targets.find { it.jenis == SalesTarget.HARIAN }?.nominal ?: 0L
    val monthlyTarget = targets.find { it.jenis == SalesTarget.BULANAN }?.nominal ?: 0L

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.target_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(context.getString(R.string.target_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${context.getString(R.string.target_daily)}: ${RupiahFormatter.format(dailyTarget)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        "${context.getString(R.string.target_monthly)}: ${RupiahFormatter.format(monthlyTarget)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            OutlinedTextField(
                value = daily,
                onValueChange = viewModel::onDailyChange,
                label = { Text(context.getString(R.string.target_daily)) },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = monthly,
                onValueChange = viewModel::onMonthlyChange,
                label = { Text(context.getString(R.string.target_monthly)) },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = viewModel::save, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(context.getString(R.string.button_save))
            }
        }
    }
}
