@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.payable

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
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Payable
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.theme.Rugi
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PayableScreen(
    onBack: () -> Unit,
    viewModel: PayableViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val outstanding by viewModel.outstanding.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var tab by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    var payTarget by remember { mutableStateOf<Payable?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is PayableEvent.Message -> snackbar.showSnackbar(
                    when (event.text) {
                        "SAVED" -> context.getString(R.string.payable_saved)
                        "PAYMENT_SAVED" -> context.getString(R.string.payable_payment_saved)
                        "ERROR" -> context.getString(R.string.error_generic)
                        else -> event.text
                    },
                )
            }
        }
    }

    val filtered = items.filter { it.pemasok.contains(query, ignoreCase = true) }
    val unpaid = filtered.filter { !it.isLunas }
    val paid = filtered.filter { it.isLunas }
    val shown = if (tab == 0) unpaid else paid

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.payable_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAdd = true },
                icon = { Icon(Icons.Rounded.Payments, contentDescription = null) },
                text = { Text(context.getString(R.string.payable_add)) },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        context.getString(R.string.payable_outstanding),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    Text(
                        RupiahFormatter.format(outstanding),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(context.getString(R.string.payable_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("${context.getString(R.string.payable_unpaid)} (${unpaid.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("${context.getString(R.string.payable_paid)} (${paid.size})") })
            }
            if (shown.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.payable_empty), body = "")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(shown, key = { it.id }) { item ->
                        PayableRow(
                            item = item,
                            onPay = { payTarget = it },
                            onRemind = { viewModel.shareReminder(it) },
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddPayableSheet(
            onDismiss = { showAdd = false },
            onSave = { pemasok, nominal, dueDays, catatan ->
                viewModel.add(pemasok, nominal, dueDays, catatan)
                showAdd = false
            },
        )
    }
    payTarget?.let { target ->
        PayPayableSheet(
            item = target,
            onDismiss = { payTarget = null },
            onPay = { amount ->
                viewModel.recordPayment(target.id, amount)
                payTarget = null
            },
        )
    }
}

@Composable
private fun PayableRow(
    item: Payable,
    onPay: (Payable) -> Unit,
    onRemind: (Payable) -> Unit,
) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(item.pemasok, style = MaterialTheme.typography.titleMedium)
                Text(
                    RupiahFormatter.format(item.sisa),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (item.isLunas) MaterialTheme.colorScheme.primary else Rugi,
                )
            }
            Text(
                buildString {
                    append("${context.getString(R.string.payable_remaining)} ${RupiahFormatter.format(item.sisa)}")
                    append(" · ${context.getString(R.string.payable_recorded)} ${DateFormatter.formatDateTime(item.waktu)}")
                    item.jatuhTempo?.let { append(" · ${context.getString(R.string.payable_due_date)}: ${DateFormatter.formatDayMonthYear(it)}") }
                    if (item.isOverdue) append(" · ${context.getString(R.string.payable_overdue)}")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!item.isLunas) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onRemind(item) }) {
                        Icon(Icons.AutoMirrored.Rounded.Chat, contentDescription = context.getString(R.string.payable_reminder), tint = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { onPay(item) }) {
                        Text(context.getString(R.string.payable_pay))
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPayableSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var pemasok by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var dueDays by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(context.getString(R.string.payable_add), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(pemasok, { pemasok = it }, label = { Text(context.getString(R.string.payable_supplier)) }, placeholder = { Text(context.getString(R.string.payable_supplier_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(nominal, { nominal = it.filter(Char::isDigit).take(15) }, label = { Text(context.getString(R.string.amount)) }, prefix = { Text("Rp ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(dueDays, { dueDays = it.filter(Char::isDigit).take(3) }, label = { Text(context.getString(R.string.payable_due_days)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(catatan, { catatan = it }, label = { Text(context.getString(R.string.payable_note_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = { onSave(pemasok, nominal, dueDays, catatan) },
                enabled = pemasok.isNotBlank() && nominal.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.button_save)) }
        }
    }
}

@Composable
private fun PayPayableSheet(
    item: Payable,
    onDismiss: () -> Unit,
    onPay: (String) -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var amount by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(context.getString(R.string.payable_pay_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "${item.pemasok} · ${context.getString(R.string.payable_remaining)} ${RupiahFormatter.format(item.sisa)}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit).take(15) },
                label = { Text(context.getString(R.string.amount)) },
                prefix = { Text("Rp ") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = { amount = item.sisa.toString() }) {
                Text("${context.getString(R.string.payable_pay)} ${RupiahFormatter.format(item.sisa)}")
            }
            Button(
                onClick = { onPay(amount) },
                enabled = amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(context.getString(R.string.button_save)) }
        }
    }
}
