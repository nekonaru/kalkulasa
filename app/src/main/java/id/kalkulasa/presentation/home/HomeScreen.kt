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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.component.SectionHeader
import id.kalkulasa.presentation.component.StatCard
import id.kalkulasa.presentation.theme.AmberCuan
import id.kalkulasa.presentation.theme.AmberCuanDark
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses

@Composable
fun HomeScreen(
    onOpenCashier: () -> Unit,
    onOpenPiutang: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val warungMode by viewModel.warungMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    OnLifecycleResume { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.error) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(context.getString(R.string.error_generic), color = MaterialTheme.colorScheme.onErrorContainer)
                        TextButton(onClick = viewModel::refresh) { Text(context.getString(R.string.button_retry)) }
                    }
                }
            }
        }
        item {
            Column {
                val greeting = greetingForTimeOfDay(context)
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = state.namaUsaha.ifEmpty { context.getString(R.string.home_greeting_line) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
        if (state.targetHarian > 0 || state.targetBulanan > 0) {
            item { TargetCard(state) }
        }
        if (warungMode) {
            item {
                Button(
                    onClick = onOpenCashier,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(24.dp))
                    Text(context.getString(R.string.home_cta_sell), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
        if (!warungMode) {
            item {
                HealthScoreCard(health = state.health)
            }
            if (state.piutangOutstanding > 0) {
                item {
                    PiutangCard(outstanding = state.piutangOutstanding, onOpenPiutang = onOpenPiutang)
                }
            }
            item {
                SectionHeader(context.getString(R.string.trend_7_days))
                TrendChart(points = state.trend, modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp))
            }
        }
        if (!warungMode) {
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
                items(state.bestSellers, key = { it.namaProduk }) { product ->
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
}

private fun greetingForTimeOfDay(context: android.content.Context): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 4..10 -> context.getString(R.string.home_greeting_morning)
        in 11..14 -> context.getString(R.string.home_greeting_afternoon)
        in 15..18 -> context.getString(R.string.home_greeting_evening)
        else -> context.getString(R.string.home_greeting_night)
    }
}

@Composable
private fun TargetCard(state: HomeUiState) {
    val context = LocalContext.current
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(context.getString(R.string.target_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            if (state.targetHarian > 0) {
                val progress = (state.omzetToday.toFloat() / state.targetHarian.toFloat()).coerceIn(0f, 1f)
                Text(context.getString(R.string.target_daily_progress, RupiahFormatter.format(state.omzetToday), RupiahFormatter.format(state.targetHarian)), style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
            if (state.targetBulanan > 0) {
                val progress = (state.omzetMonth.toFloat() / state.targetBulanan.toFloat()).coerceIn(0f, 1f)
                Text(context.getString(R.string.target_monthly_progress, RupiahFormatter.format(state.omzetMonth), RupiahFormatter.format(state.targetBulanan)), style = MaterialTheme.typography.bodyMedium)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun HealthScoreCard(health: id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Result?) {
    if (health == null) return
    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }
    val verdictText = when (health.verdict) {
        id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Verdict.PRIMA -> context.getString(R.string.health_verdict_prima)
        id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Verdict.SEHAT -> context.getString(R.string.health_verdict_sehat)
        id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Verdict.CUKUP -> context.getString(R.string.health_verdict_cukup)
        id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Verdict.PERLU_PERHATIAN -> context.getString(R.string.health_verdict_attention)
    }
    val scoreColor = when {
        health.score >= 70 -> Sukses
        health.score >= 40 -> AmberCuanDark
        else -> Rugi
    }
    Card(
        onClick = { showDetails = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    health.score.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text("/100", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(context.getString(R.string.health_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Text(verdictText, style = MaterialTheme.typography.titleLarge, color = scoreColor)
                Text(
                    suggestionText(context, health.suggestions.firstOrNull()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                    maxLines = 2,
                )
            }
        }
    }
    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(context.getString(R.string.health_details)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${context.getString(R.string.health_title)}: ${health.score}/100 ($verdictText)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    health.suggestions.forEachIndexed { index, suggestion ->
                        Text("${index + 1}. ${suggestionText(context, suggestion)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) { Text(context.getString(R.string.button_close)) }
            },
        )
    }
}

@Composable
private fun PiutangCard(outstanding: Long, onOpenPiutang: () -> Unit) {
    val context = LocalContext.current
    Card(
        onClick = onOpenPiutang,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(context.getString(R.string.piutang_outstanding), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(RupiahFormatter.format(outstanding), style = MaterialTheme.typography.titleLarge, color = Rugi)
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun suggestionText(context: android.content.Context, suggestion: id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion?): String {
    if (suggestion == null) return context.getString(R.string.suggestion_keep)
    return context.getString(
        when (suggestion) {
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.PERBAIKI_MARGIN -> R.string.suggestion_margin
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.KURANGI_PENGELUARAN -> R.string.suggestion_expenses
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.PUTAR_STOK -> R.string.suggestion_stock
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.TAGIH_PIUTANG -> R.string.suggestion_piutang
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.ISI_KAS -> R.string.suggestion_cash
            id.kalkulasa.domain.usecase.KesehatanUsahaUseCase.Suggestion.BAGUS -> R.string.suggestion_keep
        },
    )
}

@Composable
private fun TrendChart(points: List<id.kalkulasa.domain.model.DailyPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { maxOf(it.omzet, 1L) }.toFloat()
    val primary = MaterialTheme.colorScheme.primary
    val amber = AmberCuan
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 10.sp,
        color = muted,
    )
    Canvas(modifier = modifier) {
        val slot = size.width / points.size
        points.forEachIndexed { index, point ->
            val barWidth = slot * 0.6f
            val left = index * slot + (slot - barWidth) / 2
            val barHeight = (point.omzet / maxValue) * (size.height * 0.72f)
            drawRoundRect(
                color = primary,
                topLeft = Offset(left, size.height - barHeight - 28f),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
            )
            val dotY = size.height - 28f - (point.laba.toFloat().coerceAtLeast(0f) / maxValue) * (size.height * 0.72f)
            drawCircle(color = amber, radius = 4f, center = Offset(left + barWidth / 2, dotY))
            val label = point.label
            val measured = textMeasurer.measure(label, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(left + (barWidth - measured.size.width) / 2, size.height - 16f),
                style = labelStyle,
            )
        }
        drawLine(
            color = primary.copy(alpha = 0.3f),
            start = Offset(0f, size.height - 28f),
            end = Offset(size.width, size.height - 28f),
            strokeWidth = 2f,
        )
    }
}
