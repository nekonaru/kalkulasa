@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.datatools

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
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@Composable
fun DataToolsScreen(
    onBack: () -> Unit,
    viewModel: DataToolsViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var results by remember { mutableStateOf(SearchResults(emptyList(), emptyList())) }

    LaunchedEffect(query) { results = viewModel.search(query) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.data_tools_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(context.getString(R.string.data_tools_export), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = viewModel::exportProducts, modifier = Modifier.weight(1f)) { Text(context.getString(R.string.export_products)) }
                    Button(onClick = viewModel::exportCustomers, modifier = Modifier.weight(1f)) { Text(context.getString(R.string.export_customers)) }
                }
                Button(onClick = viewModel::exportPayables, modifier = Modifier.fillMaxWidth()) { Text(context.getString(R.string.export_payables)) }
            }
            item {
                Text(context.getString(R.string.global_search), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(query, viewModel::onQueryChange, label = { Text(context.getString(R.string.global_search_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
            if (query.isNotBlank() && results.products.isEmpty() && results.sales.isEmpty()) {
                item { KalkulasaEmptyState(title = context.getString(R.string.search_empty), body = "") }
            } else {
                items(results.products, key = { "p-$it" }) { name ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(name, modifier = Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
                items(results.sales, key = { "s-$it" }) { sale ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(sale, modifier = Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
            }
        }
    }
}
