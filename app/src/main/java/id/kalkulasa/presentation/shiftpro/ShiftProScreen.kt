@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package id.kalkulasa.presentation.shiftpro

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
fun ShiftProScreen(
    onBack: () -> Unit,
    viewModel: ShiftProViewModel = hiltViewModel(),
) {
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var nama by remember { mutableStateOf("") }
    var mulai by remember { mutableStateOf("") }
    var selesai by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    var menit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.shift_pro_title)) },
                navigationIcon = { TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(context.getString(R.string.shift_add), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.shift_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(mulai, { mulai = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.shift_start)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(selesai, { selesai = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.shift_end)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { viewModel.addShift(nama, mulai.toIntOrNull() ?: 0, selesai.toIntOrNull() ?: 0); nama = "" }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.shift_save))
                }
                shifts.forEach { s ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text("${s.nama} · ${s.mulai}-${s.selesai}", modifier = Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
            }
            item {
                Text(context.getString(R.string.employee_pin_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(employeeId, { employeeId = it }, label = { Text(context.getString(R.string.employee_id)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pin, { pin = it.filter(Char::isDigit).take(6) }, label = { Text(context.getString(R.string.pin_new)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { if (employeeId.isNotBlank() && pin.length == 6) viewModel.setPin(employeeId, pin) }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.pin_set))
                }
            }
            item {
                Text(context.getString(R.string.overtime_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(employeeId, { employeeId = it }, label = { Text(context.getString(R.string.employee_id)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(menit, { menit = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.overtime_minutes)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(onClick = { if (employeeId.isNotBlank()) viewModel.addOvertime(employeeId, menit.toIntOrNull() ?: 0) }, modifier = Modifier.fillMaxWidth()) {
                    Text(context.getString(R.string.overtime_add))
                }
                if (employees.isEmpty()) {
                    KalkulasaEmptyState(title = context.getString(R.string.employees_empty), body = "")
                } else {
                    employees.take(20).forEach { e -> Text(e.nama) }
                }
            }
        }
    }
}
