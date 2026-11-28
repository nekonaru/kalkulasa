@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.keuanganakhir

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
import id.kalkulasa.core.util.RupiahFormatter

@Composable
fun KeuanganAkhirScreen(
    onBack: () -> Unit,
    viewModel: KeuanganAkhirViewModel = hiltViewModel(),
) {
    val payables by viewModel.payables.collectAsStateWithLifecycle()
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    val ppn by viewModel.ppn.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var totalText by remember { mutableStateOf("") }
    var aktif by remember { mutableStateOf(false) }
    var nominalPer by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.keuangan_akhir_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(context.getString(R.string.batch_pay_title), style = MaterialTheme.typography.titleMedium)
                payables.filter { !it.isLunas }.forEach { p ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = p.id in selected, onCheckedChange = { viewModel.toggle(p.id) })
                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.pemasok)
                                Text("Sisa ${RupiahFormatter.format(p.sisa)}", style = MaterialTheme.typography.bodySmall)
                            }
                            OutlinedTextField(
                                value = nominalPer[p.id]?.toString() ?: "",
                                onValueChange = { nominalPer = nominalPer + (p.id to (it.filter(Char::isDigit).toLongOrNull() ?: 0L)) },
                                modifier = Modifier.fillMaxWidth(0.35f),
                                singleLine = true,
                            )
                        }
                    }
                }
                Button(onClick = { viewModel.bayarBatch(nominalPer) }, enabled = selected.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.batch_pay))
                }
            }
            item {
                Text(context.getString(R.string.ppn_report_title), style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = aktif, onCheckedChange = { aktif = it; viewModel.hitungPpn(totalText, it) })
                    Text(context.getString(R.string.ppn_enable))
                }
                OutlinedTextField(totalText, { totalText = it; viewModel.hitungPpn(it, aktif) }, label = { Text(context.getString(R.string.amount)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("${context.getString(R.string.ppn)}: ${RupiahFormatter.format(ppn)}")
            }
        }
    }
}
