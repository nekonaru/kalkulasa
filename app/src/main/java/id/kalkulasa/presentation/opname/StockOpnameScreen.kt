@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.opname

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import id.kalkulasa.core.util.DateFormatter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StockOpnameScreen(
    onBack: () -> Unit,
    viewModel: StockOpnameViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val counts by viewModel.counts.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OpnameEvent.Message -> snackbar.showSnackbar(
                    when {
                        event.text == "NO_CHANGE" -> context.getString(R.string.opname_no_change)
                        event.text.startsWith("SAVED:") -> context.getString(
                            R.string.opname_saved,
                            event.text.removePrefix("SAVED:").toIntOrNull() ?: 0,
                        )
                        else -> context.getString(R.string.error_generic)
                    },
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.opname_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(context.getString(R.string.opname_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(products, key = { it.id }) { product ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(product.nama, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${context.getString(R.string.stock_current)} ${DateFormatter.formatQty(product.stok)} ${product.satuan}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            OutlinedTextField(
                                value = counts[product.id] ?: "",
                                onValueChange = { viewModel.onCount(product.id, it) },
                                placeholder = { Text(DateFormatter.formatQty(product.stok)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(0.35f),
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { viewModel.save(context.getString(R.string.reason_opname)) },
                enabled = !saving && products.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.opname_save)) }
        }
    }
}
