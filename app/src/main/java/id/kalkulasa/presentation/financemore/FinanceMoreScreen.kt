@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.financemore

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
import androidx.compose.material3.LinearProgressIndicator
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
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses
import kotlinx.coroutines.flow.collectLatest

@Composable
fun FinanceMoreScreen(
    onBack: () -> Unit,
    viewModel: FinanceMoreViewModel = hiltViewModel(),
) {
    val audits by viewModel.audits.collectAsStateWithLifecycle()
    val budgetInput by viewModel.budgetInput.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var kategori by remember { mutableStateOf("") }
    var fisik by remember { mutableStateOf("") }
    var budgets by remember { mutableStateOf<List<BudgetRow>>(emptyList()) }
    var refresh by remember { mutableStateOf(0) }

    LaunchedEffect(refresh) { budgets = viewModel.budgetRows() }
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { snackbar.showSnackbar(context.getString(R.string.saved_generic)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.finance_more_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(context.getString(R.string.budget_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(kategori, { kategori = it }, label = { Text(context.getString(R.string.expense_category)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(budgetInput, viewModel::onBudgetInput, label = { Text(context.getString(R.string.budget_nominal)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.saveBudget(kategori); refresh++; kategori = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.budget_save))
                }
                budgets.forEach { row ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${row.kategori}: ${RupiahFormatter.format(row.terpakai)} / ${RupiahFormatter.format(row.anggaran)}")
                            LinearProgressIndicator(progress = { ((row.persen / 100.0).coerceIn(0.0, 1.0)).toFloat() }, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                        }
                    }
                }
            }
            item {
                Text(context.getString(R.string.cash_audit_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(fisik, { fisik = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.cash_audit_physical)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.recordAudit(fisik, ""); fisik = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.cash_audit_save))
                }
            }
            if (audits.isEmpty()) {
                item { KalkulasaEmptyState(title = context.getString(R.string.cash_audit_empty), body = "") }
            } else {
                items(audits, key = { it.id }) { audit ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(DateFormatter.formatDateTime(audit.waktu))
                                Text("${context.getString(R.string.cash_audit_system)}: ${RupiahFormatter.format(audit.sistem)}")
                            }
                            Text(RupiahFormatter.format(audit.selisih), color = if (audit.selisih >= 0) Sukses else Rugi)
                        }
                    }
                }
            }
        }
    }
}
