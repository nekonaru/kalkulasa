@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.crm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@Composable
fun CrmScreen(
    onBack: () -> Unit,
    viewModel: CrmViewModel = hiltViewModel(),
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var editing by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.crm_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        if (customers.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.crm_empty), body = "", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(customers, key = { it.id }) { customer ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { editing = customer },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(customer.nama, style = MaterialTheme.typography.titleMedium)
                                customer.tag?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                                customer.catatan?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            }
                            TextButton(onClick = { viewModel.shareMemberCard(customer) }) {
                                Text(context.getString(R.string.crm_share_card))
                            }
                        }
                    }
                }
            }
        }
    }

    editing?.let { customer ->
        var catatan by remember(customer.id) { mutableStateOf(customer.catatan ?: "") }
        var tag by remember(customer.id) { mutableStateOf(customer.tag ?: "") }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text(customer.nama) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(tag, { tag = it }, label = { Text(context.getString(R.string.crm_tag)) }, singleLine = true)
                    OutlinedTextField(catatan, { catatan = it }, label = { Text(context.getString(R.string.crm_note)) }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateNotes(customer.id, catatan, tag)
                    editing = null
                }) { Text(context.getString(R.string.button_save)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
}
