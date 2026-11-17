package id.kalkulasa.presentation.stock

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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Product
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun RestockScreen(
    onBack: () -> Unit,
    viewModel: RestockViewModel = hiltViewModel(),
) {
    val products by viewModel.lowStock.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.restock_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (products.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.restock_empty), body = "")
            } else {
                Button(
                    onClick = { viewModel.share(context) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Rounded.Share, contentDescription = null)
                    Text(context.getString(R.string.restock_share))
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(products, key = { it.id }) { product ->
                        RestockRow(product)
                    }
                }
            }
        }
    }
}

@Composable
private fun RestockRow(product: Product) {
    val context = LocalContext.current
    val suggested = suggestedRestock(product)
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.nama, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${DateFormatter.formatQty(product.stok)} ${product.satuan} · ${RupiahFormatter.format(product.hargaBeliRata2)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                String.format(context.getString(R.string.restock_suggested), DateFormatter.formatQty(suggested), product.satuan),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun suggestedRestock(product: Product): Double {
    // Target = double the minimum stock, never below 1
    val target = (product.stokMinimum * 2.0) - product.stok
    return if (target >= 1.0) target else 1.0
}
