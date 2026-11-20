package id.kalkulasa.presentation.supplier

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SupplierPurchasesScreen(
    onBack: () -> Unit,
    viewModel: SupplierPurchasesViewModel = hiltViewModel(),
) {
    val purchases by viewModel.purchases.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var days by remember { mutableStateOf(30) }

    LaunchedEffect(days) { viewModel.load(days) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.supplier.ifBlank { context.getString(R.string.supplier_unknown) }) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    30 to R.string.sales_history_period_30,
                    90 to R.string.sales_history_period_90,
                    365 to R.string.supplier_period_year,
                ).forEach { (period, label) ->
                    FilterChip(selected = days == period, onClick = { days = period }, label = { Text(context.getString(label)) })
                }
            }
            Text(
                "${context.getString(R.string.suppliers_total_spent)}: ${RupiahFormatter.format(purchases.sumOf { it.total })}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            if (purchases.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.suppliers_empty), body = "")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(purchases, key = { it.id }) { purchase ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(DateFormatter.formatDateTime(purchase.waktu), style = MaterialTheme.typography.bodyLarge)
                                    purchase.catatan?.let {
                                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(RupiahFormatter.format(purchase.total), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
