@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.kasirpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun KasirProScreen(
    onBack: () -> Unit,
    viewModel: KasirProViewModel = hiltViewModel(),
) {
    val preorders by viewModel.preorders.collectAsStateWithLifecycle()
    val bundles by viewModel.bundles.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var tab by remember { mutableStateOf(0) }
    var showPreorder by remember { mutableStateOf(false) }
    var showBundle by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest {
            snackbar.showSnackbar(context.getString(R.string.saved_generic))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.kasir_pro_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(context.getString(R.string.preorder_title)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(context.getString(R.string.bundle_title)) })
            }
            if (tab == 0) {
                Button(onClick = { showPreorder = true }, modifier = Modifier.padding(16.dp)) {
                    Text(context.getString(R.string.preorder_add))
                }
                if (preorders.isEmpty()) {
                    KalkulasaEmptyState(title = context.getString(R.string.preorder_empty), body = "")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(preorders, key = { it.id }) { p ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(p.namaPelanggan ?: context.getString(R.string.customer_name_hint))
                                        Text(DateFormatter.formatDateTime(p.waktu), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                                        Text(RupiahFormatter.format(p.total))
                                        Text(p.status, style = MaterialTheme.typography.bodySmall)
                                        Row {
                                            TextButton(onClick = { viewModel.changeStatus(p.id, "DIJUAL") }) { Text(context.getString(R.string.preorder_sell)) }
                                            TextButton(onClick = { viewModel.changeStatus(p.id, "DIBATALKAN") }) { Text(context.getString(R.string.button_cancel)) }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Button(onClick = { showBundle = true }, modifier = Modifier.padding(16.dp)) {
                    Text(context.getString(R.string.bundle_add))
                }
                if (bundles.isEmpty()) {
                    KalkulasaEmptyState(title = context.getString(R.string.bundle_empty), body = "")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(bundles, key = { it.id }) { b ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(b.nama)
                                    Text(RupiahFormatter.format(b.hargaPaket))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPreorder) {
        var nama by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showPreorder = false },
            title = { Text(context.getString(R.string.preorder_add)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.customer_name)) }, singleLine = true)
                    OutlinedTextField(qty, { qty = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.redeem_points_hint)) }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createQuickPreorder(nama)
                    showPreorder = false
                }, enabled = nama.isNotBlank()) { Text(context.getString(R.string.button_save)) }
            },
            dismissButton = { TextButton(onClick = { showPreorder = false }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
    if (showBundle) {
        var nama by remember { mutableStateOf("") }
        var harga by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBundle = false },
            title = { Text(context.getString(R.string.bundle_add)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.bundle_name)) }, singleLine = true)
                    OutlinedTextField(harga, { harga = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.bundle_price)) }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createBundle(nama, harga, emptyList())
                    showBundle = false
                }, enabled = nama.isNotBlank() && harga.isNotBlank()) { Text(context.getString(R.string.button_save)) }
            },
            dismissButton = { TextButton(onClick = { showBundle = false }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
}
