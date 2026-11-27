@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.crmpro

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
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@Composable
fun CrmProScreen(
    onBack: () -> Unit,
    viewModel: CrmProViewModel = hiltViewModel(),
) {
    val rows by viewModel.rows.collectAsStateWithLifecycle()
    val context = LocalContext.current
    id.kalkulasa.presentation.component.OnLifecycleResume { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.crm_pro_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        if (rows.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.crm_pro_empty), body = "", modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(rows, key = { it.nama }) { row ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(row.nama, style = MaterialTheme.typography.titleMedium)
                                Text(context.getString(R.string.crm_rfm) + ": " + row.rfm)
                            }
                            row.hariUlangTahun?.let {
                                Text(context.getString(R.string.crm_birthday) + ": " + it)
                            }
                        }
                    }
                }
            }
        }
    }
}
