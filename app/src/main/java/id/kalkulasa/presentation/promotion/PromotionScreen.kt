@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.promotion

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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
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
fun PromotionScreen(
    onBack: () -> Unit,
    viewModel: PromotionViewModel = hiltViewModel(),
) {
    val vouchers by viewModel.vouchers.collectAsStateWithLifecycle()
    val deposits by viewModel.deposits.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var tab by remember { mutableStateOf(0) }
    var showVoucher by remember { mutableStateOf(false) }
    var showDeposit by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PromotionEvent.Message -> snackbar.showSnackbar(
                    if (event.text == "SAVED") context.getString(R.string.saved_generic)
                    else context.getString(R.string.error_generic),
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.promotion_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (tab == 0) showVoucher = true else showDeposit = true },
                text = { Text(context.getString(R.string.promotion_add)) },
                icon = { Text("+") },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(context.getString(R.string.tab_voucher)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(context.getString(R.string.tab_deposit)) })
            }
            if (tab == 0) {
                if (vouchers.isEmpty()) {
                    KalkulasaEmptyState(title = context.getString(R.string.voucher_empty), body = "")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(vouchers, key = { it.id }) { v ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text(v.kode, style = MaterialTheme.typography.titleMedium)
                                        Text("${if (v.tipe == "PERSEN") "${v.nilai}%" else RupiahFormatter.format(v.nilai)} · ${context.getString(R.string.voucher_quota)} ${v.sisaKuota}")
                                        Text(DateFormatter.formatDayMonthYear(v.berlakuSampai), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (deposits.isEmpty()) {
                    KalkulasaEmptyState(title = context.getString(R.string.deposit_empty), body = "")
                } else {
                    LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(deposits, key = { it.id }) { d ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(d.namaPelanggan, style = MaterialTheme.typography.titleMedium)
                                    Text(RupiahFormatter.format(d.sisa), color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showVoucher) {
        VoucherSheet(onDismiss = { showVoucher = false }, onSave = { kode, tipe, nilai, min, kuota, days ->
            viewModel.saveVoucher(kode, tipe, nilai, min, kuota, days); showVoucher = false
        })
    }
    if (showDeposit) {
        DepositSheet(onDismiss = { showDeposit = false }, onSave = { nama, nominal, catatan ->
            viewModel.addDeposit(nama, nominal, catatan); showDeposit = false
        })
    }
}

@Composable
private fun VoucherSheet(onDismiss: () -> Unit, onSave: (String, String, String, String, String, String) -> Unit) {
    val context = LocalContext.current
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var kode by remember { mutableStateOf("") }
    var tipe by remember { mutableStateOf("NOMINAL") }
    var nilai by remember { mutableStateOf("") }
    var min by remember { mutableStateOf("") }
    var kuota by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(context.getString(R.string.voucher_add), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(kode, { kode = it.uppercase() }, label = { Text(context.getString(R.string.voucher_code)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = tipe == "NOMINAL", onClick = { tipe = "NOMINAL" }, label = { Text(context.getString(R.string.voucher_nominal)) })
                FilterChip(selected = tipe == "PERSEN", onClick = { tipe = "PERSEN" }, label = { Text(context.getString(R.string.voucher_percent)) })
            }
            OutlinedTextField(nilai, { nilai = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.amount)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(min, { min = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.voucher_min)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(kuota, { kuota = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.voucher_quota)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(days, { days = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.voucher_days)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSave(kode, tipe, nilai, min, kuota, days) }, enabled = kode.isNotBlank() && nilai.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(context.getString(R.string.button_save))
            }
        }
    }
}

@Composable
private fun DepositSheet(onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
    val context = LocalContext.current
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var nama by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(context.getString(R.string.deposit_add), style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.customer_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(nominal, { nominal = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.amount)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(catatan, { catatan = it }, label = { Text(context.getString(R.string.note)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = { onSave(nama, nominal, catatan) }, enabled = nama.isNotBlank() && nominal.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(context.getString(R.string.button_save))
            }
        }
    }
}
