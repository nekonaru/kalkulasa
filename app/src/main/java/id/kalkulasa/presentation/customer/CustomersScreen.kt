package id.kalkulasa.presentation.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    onBack: () -> Unit,
    viewModel: CustomersViewModel = hiltViewModel(),
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selected by remember { mutableStateOf<id.kalkulasa.domain.model.Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.customers_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                label = { Text(context.getString(R.string.customers_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (customers.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.customers_empty), body = "")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(customers, key = { it.id }) { customer ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { selected = customer },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(Icons.Rounded.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(customer.nama, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        buildString {
                                            append(String.format(context.getString(R.string.customers_points_label), customer.poin))
                                            append(" · ")
                                            append(String.format(context.getString(R.string.customers_visits_label), customer.kunjungan))
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    RupiahFormatter.format(customer.totalBelanja),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selected?.let { customer ->
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(customer.nama) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(String.format(context.getString(R.string.customers_points_label), customer.poin), style = MaterialTheme.typography.titleLarge)
                    Text(String.format(context.getString(R.string.customers_visits_label), customer.kunjungan))
                    Text(String.format(context.getString(R.string.customers_spent_label), RupiahFormatter.format(customer.totalBelanja)))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPoints(customer.id)
                    selected = null
                }) { Text(context.getString(R.string.customers_reset_points)) }
            },
            dismissButton = {
                TextButton(onClick = { selected = null }) { Text(context.getString(R.string.button_close)) }
            },
        )
    }
}
