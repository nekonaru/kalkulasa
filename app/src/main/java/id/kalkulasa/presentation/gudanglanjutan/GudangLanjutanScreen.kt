@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.gudanglanjutan

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume

@Composable
fun GudangLanjutanScreen(
    onBack: () -> Unit,
    viewModel: GudangLanjutanViewModel = hiltViewModel(),
) {
    val expiry by viewModel.expiry.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var products by remember { mutableStateOf<List<id.kalkulasa.domain.model.Product>>(emptyList()) }
    LaunchedEffect(Unit) { products = viewModel.products() }
    OnLifecycleResume { viewModel.loadExpiry() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.gudang_lanjutan_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(context.getString(R.string.expiry_title)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(context.getString(R.string.price_history_title)) })
            }
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tab == 0) {
                    if (expiry.isEmpty()) {
                        item { KalkulasaEmptyState(title = context.getString(R.string.expiry_empty), body = "") }
                    } else {
                        items(expiry, key = { it.nama }) { row ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(row.nama)
                                    Text("${row.sisaHari} hari")
                                }
                            }
                        }
                    }
                } else {
                    items(products.take(30), key = { it.id }) { product ->
                        TextButton(onClick = { viewModel.loadHistory(product.id) }) { Text(product.nama) }
                    }
                    items(history, key = { it.tanggal }) { row ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(DateFormatter.formatDateTime(row.tanggal))
                                Text(RupiahFormatter.format(row.harga))
                            }
                        }
                    }
                }
            }
        }
    }
}
