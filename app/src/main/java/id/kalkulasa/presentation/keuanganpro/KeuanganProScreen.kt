@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.keuanganpro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import id.kalkulasa.core.util.RupiahFormatter
import kotlinx.coroutines.flow.collectLatest

@Composable
fun KeuanganProScreen(
    onBack: () -> Unit,
    viewModel: KeuanganProViewModel = hiltViewModel(),
) {
    val recurring by viewModel.recurring.collectAsStateWithLifecycle()
    val shares by viewModel.shares.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var kategori by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var hari by remember { mutableStateOf("1") }
    var nama by remember { mutableStateOf("") }
    var persen by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { snackbar.showSnackbar(context.getString(R.string.saved_generic)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.keuangan_pro_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(context.getString(R.string.recurring_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(kategori, { kategori = it }, label = { Text(context.getString(R.string.expense_category)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nominal, { nominal = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.amount)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(hari, { hari = it.filter(Char::isDigit).take(2) }, label = { Text(context.getString(R.string.recurring_day)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.addRecurring(kategori, nominal, hari); kategori = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.recurring_save))
                }
                recurring.forEach { r ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${r.kategori} · ${context.getString(R.string.recurring_day)} ${r.hariPerBulan}")
                            Text(RupiahFormatter.format(r.nominal))
                        }
                    }
                }
            }
            item {
                Text(context.getString(R.string.profit_share_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.profit_share_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(persen, { persen = it.filter(Char::isDigit).take(3) }, label = { Text(context.getString(R.string.profit_share_percent)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.addShare(nama, persen); nama = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.profit_share_save))
                }
                shares.forEach { s ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(s.nama)
                            Text("${s.persentase}%")
                        }
                    }
                }
            }
        }
    }
}
