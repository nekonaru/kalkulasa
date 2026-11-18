package id.kalkulasa.presentation.categories

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import id.kalkulasa.domain.model.Category
import id.kalkulasa.presentation.component.KalkulasaEmptyState

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var newName by remember { mutableStateOf("") }
    var editTarget by remember { mutableStateOf<Category?>(null) }
    var deleteTarget by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.categories_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text(context.getString(R.string.product_category)) },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.add(newName); newName = "" },
                        enabled = newName.isNotBlank(),
                    ) { Icon(Icons.Rounded.Add, contentDescription = context.getString(R.string.button_add)) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (categories.isEmpty()) {
                KalkulasaEmptyState(title = context.getString(R.string.categories_empty), body = "")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { category ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(category.nama, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Row {
                                    IconButton(onClick = { editTarget = category }) {
                                        Icon(Icons.Rounded.Edit, contentDescription = null)
                                    }
                                    IconButton(onClick = { deleteTarget = category }) {
                                        Icon(Icons.Rounded.Delete, contentDescription = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    editTarget?.let { category ->
        var name by remember(category) { mutableStateOf(category.nama) }
        AlertDialog(
            onDismissRequest = { editTarget = null },
            title = { Text(category.nama) },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(category.id, name)
                    editTarget = null
                }) { Text(context.getString(R.string.button_save)) }
            },
            dismissButton = { TextButton(onClick = { editTarget = null }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
    deleteTarget?.let { category ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(context.getString(R.string.categories_delete_title)) },
            text = { Text("${category.nama}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(category.id)
                    deleteTarget = null
                }) { Text(context.getString(R.string.button_delete)) }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(context.getString(R.string.button_cancel)) } },
        )
    }
}
