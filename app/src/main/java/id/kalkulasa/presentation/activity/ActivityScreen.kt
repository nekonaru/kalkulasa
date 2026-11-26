@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.activity

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.ActivityLog
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@Composable
fun ActivityScreen(
    onBack: () -> Unit,
    viewModel: ActivityViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.activity_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        if (items.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.activity_empty), body = "", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    ActivityRow(item)
                }
            }
        }
    }
}

@Composable
private fun ActivityRow(item: ActivityLog) {
    val context = LocalContext.current
    val entity = when (item.entitas) {
        "sale" -> context.getString(R.string.entity_sale)
        "product" -> context.getString(R.string.entity_product)
        "customer" -> context.getString(R.string.entity_customer)
        "receivable" -> context.getString(R.string.entity_receivable)
        "payable" -> context.getString(R.string.entity_payable)
        "employee" -> context.getString(R.string.entity_employee)
        "payroll" -> context.getString(R.string.entity_payroll)
        "expense" -> context.getString(R.string.entity_expense)
        "category" -> context.getString(R.string.entity_category)
        "stock_movement" -> context.getString(R.string.entity_stock_movement)
        "purchase" -> context.getString(R.string.entity_purchase)
        else -> context.getString(R.string.entity_other)
    }
    val verb = when (item.aksi) {
        "INSERT" -> context.getString(R.string.activity_insert, entity)
        "DELETE" -> context.getString(R.string.activity_delete, entity)
        else -> context.getString(R.string.activity_update, entity)
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(verb, style = MaterialTheme.typography.bodyLarge)
                Text(
                    item.entitasId.take(12),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                DateFormatter.formatDateTime(item.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
