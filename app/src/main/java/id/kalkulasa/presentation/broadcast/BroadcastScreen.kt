@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.broadcast

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import id.kalkulasa.presentation.component.OnLifecycleResume

@Composable
fun BroadcastScreen(
    onBack: () -> Unit,
    viewModel: BroadcastViewModel = hiltViewModel(),
) {
    val all by viewModel.all.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tag by remember { mutableStateOf("") }
    var minimal by remember { mutableStateOf("") }
    var pesan by remember { mutableStateOf("") }
    OnLifecycleResume { viewModel.load() }

    val minValue = minimal.filter(Char::isDigit).toLongOrNull() ?: 0L
    val rows = viewModel.filtered(tag.trim(), minValue)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.broadcast_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(tag, { tag = it }, label = { Text(context.getString(R.string.segment_tag)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(minimal, { minimal = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.segment_min)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(pesan, { pesan = it }, label = { Text(context.getString(R.string.broadcast_message)) }, modifier = Modifier.fillMaxWidth())
            Button(onClick = { viewModel.broadcast(pesan) }, enabled = pesan.isNotBlank() && selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                Text(context.getString(R.string.broadcast_send))
            }
            if (rows.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.segment_empty), body = "")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(rows, key = { it.id }) { customer ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Checkbox(checked = customer.id in selected, onCheckedChange = { viewModel.toggle(customer.id) })
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.nama, style = MaterialTheme.typography.titleMedium)
                                    Text("${viewModel.tier(customer)} · ${viewModel.memberCode(customer)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
