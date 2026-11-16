package id.kalkulasa.presentation.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.component.SectionHeader
import id.kalkulasa.presentation.component.StatCard
import id.kalkulasa.presentation.theme.AmberCuan
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun HomeScreen(
    onOpenCashier: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = state.namaUsaha.ifEmpty { context.getString(R.string.app_name) },
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(context.getString(R.string.home_today), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = context.getString(R.string.omzet_today),
                    value = RupiahFormatter.format(state.omzetToday),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Payments,
                )
                StatCard(
                    label = context.getString(R.string.net_profit),
                    value = RupiahFormatter.format(state.netProfitToday),
                    modifier = Modifier.weight(1f),
                    valueColor = if (state.netProfitToday >= 0) Sukses else Rugi,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = context.getString(R.string.transactions_today),
                    value = state.transactionCount.toString(),
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.Receipt,
                )
                StatCard(
                    label = context.getString(R.string.low_stock_count),
                    value = state.lowStockCount.toString(),
                    modifier = Modifier.weight(1f),
                    valueColor = if (state.lowStockCount > 0) Rugi else Sukses,
                    icon = Icons.Rounded.Warning,
                )
            }
        }
        item {
            SectionHeader(context.getString(R.string.trend_7_days))
            TrendChart(points = state.trend, modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp))
        }
        item { SectionHeader(context.getString(R.string.best_sellers)) }
        if (state.bestSellers.isEmpty()) {
            item {
                KalkulasaEmptyState(
                    title = context.getString(R.string.home_empty),
                    body = "",
                    action = {
                        Button(onClick = onOpenCashier) {
                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(context.getString(R.string.home_cta_sell))
                        }
                    },
                )
            }
        } else {
            items(state.bestSellers) { product ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(product.namaProduk, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "${product.qty.toLong()}x",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendChart(points: List<id.kalkulasa.domain.model.DailyPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { maxOf(it.omzet, 1L) }.toFloat()
    val primary = MaterialTheme.colorScheme.primary
    val amber = AmberCuan
    Canvas(modifier = modifier) {
        val slot = size.width / points.size
        points.forEachIndexed { index, point ->
            val barWidth = slot * 0.6f
            val left = index * slot + (slot - barWidth) / 2
            val barHeight = (point.omzet / maxValue) * (size.height * 0.75f)
            drawRoundRect(
                color = primary,
                topLeft = Offset(left, size.height - barHeight - 24f),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            )
            val dotY = size.height - 24f - (point.laba.toFloat().coerceAtLeast(0f) / maxValue) * (size.height * 0.75f)
            drawCircle(color = amber, radius = 4f, center = Offset(left + barWidth / 2, dotY))
        }
        drawLine(
            color = primary.copy(alpha = 0.3f),
            start = Offset(0f, size.height - 24f),
            end = Offset(size.width, size.height - 24f),
            strokeWidth = 2f,
        )
    }
}
