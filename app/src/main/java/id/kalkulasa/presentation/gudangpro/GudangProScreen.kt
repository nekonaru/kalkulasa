@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.gudangpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun GudangProScreen(
    onBack: () -> Unit,
    viewModel: GudangProViewModel = hiltViewModel(),
) {
    val suppliers by viewModel.suppliers.collectAsStateWithLifecycle()
    val transfers by viewModel.transfers.collectAsStateWithLifecycle()
    val barcode by viewModel.barcode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var nama by remember { mutableStateOf("") }
    var leadTime by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest {
            snackbar.showSnackbar(context.getString(R.string.saved_generic))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.gudang_pro_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(context.getString(R.string.supplier_lead_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.supplier_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(leadTime, { leadTime = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.supplier_lead)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.addSupplier(nama, "", leadTime); nama = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.supplier_lead_save))
                }
                suppliers.forEach { s ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.nama)
                            Text("${s.leadTimeDays} hari")
                        }
                    }
                }
            }
            item {
                Text(context.getString(R.string.transfer_title), style = MaterialTheme.typography.titleMedium)
                if (transfers.isEmpty()) {
                    Text(context.getString(R.string.transfer_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    transfers.take(20).forEach { t ->
                        Text("${t.dariLokasi} → ${t.keLokasi}")
                    }
                }
            }
            item {
                Text(context.getString(R.string.barcode_title), style = MaterialTheme.typography.titleMedium)
                Button(onClick = viewModel::generateBarcode, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.barcode_generate))
                }
                if (barcode.isNotBlank()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(barcode, modifier = Modifier.fillMaxWidth().padding(16.dp), style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
