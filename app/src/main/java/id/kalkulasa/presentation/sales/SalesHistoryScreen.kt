package id.kalkulasa.presentation.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.PaymentMethod
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.SaleStatus
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import kotlinx.coroutines.flow.collectLatest

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    onBack: () -> Unit,
    viewModel: SalesHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var saleToCancel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SalesHistoryEvent.Message -> snackbar.showSnackbar(
                    when (event.text) {
                        "SALE_CANCELLED" -> context.getString(R.string.sale_cancelled)
                        else -> context.getString(R.string.error_generic)
                    },
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.sales_history_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    SalesPeriod.DAYS_7 to R.string.sales_history_period_7,
                    SalesPeriod.DAYS_30 to R.string.sales_history_period_30,
                    SalesPeriod.DAYS_90 to R.string.sales_history_period_90,
                ).forEach { (period, label) ->
                    FilterChip(
                        selected = state.period == period,
                        onClick = { viewModel.selectPeriod(period) },
                        label = { Text(context.getString(label)) },
                    )
                }
            }
            if (state.sales.isEmpty() && !state.loading) {
                KalkulasaEmptyState(title = context.getString(R.string.sales_history_empty), body = "")
            } else {
                val totalPeriod = state.sales.filter { it.status == SaleStatus.SELESAI }.sumOf { it.total }
                Text(
                    "Omzet ${RupiahFormatter.format(totalPeriod)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.sales, key = { it.id }) { sale ->
                        SaleRow(
                            sale = sale,
                            onShare = { viewModel.shareReceipt(sale) },
                            onCancel = { saleToCancel = sale.id },
                        )
                    }
                }
            }
        }
    }

    saleToCancel?.let { saleId ->
        AlertDialog(
            onDismissRequest = { saleToCancel = null },
            title = { Text(context.getString(R.string.cancel_sale)) },
            text = { Text(context.getString(R.string.cancel_sale_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelSale(saleId)
                    saleToCancel = null
                }) { Text(context.getString(R.string.button_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { saleToCancel = null }) { Text(context.getString(R.string.button_cancel)) }
            },
        )
    }
}

@Composable
private fun SaleRow(
    sale: Sale,
    onShare: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(RupiahFormatter.format(sale.total), style = MaterialTheme.typography.titleMedium)
                Text(
                    DateFormatter.formatDateTime(sale.waktu) +
                        (if (sale.metodeBayar == PaymentMethod.HUTANG) " · ${context.getString(R.string.payment_hutang)}" else "") +
                        if (sale.status == SaleStatus.BATAL) " · BATAL" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (sale.status == SaleStatus.BATAL) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row {
                IconButton(onClick = onShare) {
                    Icon(Icons.Rounded.Share, contentDescription = context.getString(R.string.share_receipt))
                }
                TextButton(onClick = onCancel, enabled = sale.status != SaleStatus.BATAL) {
                    Text(context.getString(R.string.cancel_sale))
                }
            }
        }
    }
}
