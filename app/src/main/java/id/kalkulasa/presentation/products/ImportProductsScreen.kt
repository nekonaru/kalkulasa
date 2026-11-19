package id.kalkulasa.presentation.products

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ImportProductsScreen(
    onBack: () -> Unit,
    viewModel: ImportProductsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onFilePicked(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ImportProductsEvent.Message -> {
                    if (event.text.startsWith("DONE:")) {
                        val count = event.text.removePrefix("DONE:").toIntOrNull() ?: 0
                        snackbar.showSnackbar(String.format(context.getString(R.string.import_done), count))
                    } else {
                        snackbar.showSnackbar(context.getString(R.string.error_generic))
                    }
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.import_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(context.getString(R.string.import_explanation), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickFile.launch(arrayOf("text/*", "application/octet-stream")) }) {
                    Icon(Icons.Rounded.Upload, contentDescription = null)
                    Text(context.getString(R.string.import_pick_file))
                }
                OutlinedButton(onClick = viewModel::shareTemplate) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Text(context.getString(R.string.import_share_template))
                }
            }

            state.fileName?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }

            if (state.products.isNotEmpty()) {
                Text(String.format(context.getString(R.string.import_preview), state.products.size), style = MaterialTheme.typography.titleMedium)
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.products.take(20)) { product ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(product.nama, style = MaterialTheme.typography.bodyMedium)
                                Text(id.kalkulasa.core.util.RupiahFormatter.format(product.hargaJual), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                if (state.errors.isNotEmpty()) {
                    Text(String.format(context.getString(R.string.import_errors), state.errors.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    onClick = viewModel::runImport,
                    enabled = !state.importing,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Text(context.getString(R.string.import_run))
                }
            } else if (state.importedCount != null) {
                Text(String.format(context.getString(R.string.import_done), state.importedCount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            } else {
                Text(context.getString(R.string.import_explanation), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
