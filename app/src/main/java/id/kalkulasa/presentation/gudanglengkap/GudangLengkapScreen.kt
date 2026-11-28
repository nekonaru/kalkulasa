@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.gudanglengkap

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
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.usecase.KonversiSatuanUseCase
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@Composable
fun GudangLengkapScreen(
    onBack: () -> Unit,
    viewModel: GudangLengkapViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val saran by viewModel.saran.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var harian by remember { mutableStateOf("") }
    var dus by remember { mutableStateOf("") }
    var isi by remember { mutableStateOf("12") }
    var dari by remember { mutableStateOf("") }
    var ke by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.gudang_lengkap_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(context.getString(R.string.restock_lead_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(harian, { harian = it.filter { c -> c.isDigit() || c == ',' || c == '.' } }, label = { Text(context.getString(R.string.restock_daily)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.hitungSaran(harian) }, modifier = Modifier.fillMaxWidth()) { Text(context.getString(R.string.restock_calc)) }
                saran.forEach { row ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(row.nama)
                            Text(DateFormatter.formatQty(row.saran))
                        }
                    }
                }
            }
            item {
                Text(context.getString(R.string.converter_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(dus, { dus = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(context.getString(R.string.converter_dus)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(isi, { isi = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.converter_isi)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                val pcs = KonversiSatuanUseCase.keSatuanKecil(dus.replace(',', '.').toDoubleOrNull() ?: 0.0, isi.toDoubleOrNull() ?: 0.0)
                Text("${DateFormatter.formatQty(pcs)} pcs · ${KonversiSatuanUseCase.tampil(pcs, isi.toDoubleOrNull() ?: 0.0)}")
            }
            item {
                Text(context.getString(R.string.transfer_full_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(dari, { dari = it }, label = { Text(context.getString(R.string.transfer_from)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(ke, { ke = it }, label = { Text(context.getString(R.string.transfer_to)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                var qty by remember { mutableStateOf("") }
                var productId by remember { mutableStateOf("") }
                OutlinedTextField(productId, { productId = it }, label = { Text(context.getString(R.string.employee_id)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(qty, { qty = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(context.getString(R.string.amount)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        val q = qty.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (productId.isNotBlank() && q > 0) viewModel.transfer(dari, ke, listOf(productId to q))
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(context.getString(R.string.transfer_save)) }
                if (products.isEmpty()) KalkulasaEmptyState(title = context.getString(R.string.product_empty_title), body = "")
                else products.take(20).forEach { p -> Text("${p.id.take(8)} · ${p.nama}") }
            }
        }
    }
}
