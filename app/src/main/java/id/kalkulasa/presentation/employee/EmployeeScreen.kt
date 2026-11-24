package id.kalkulasa.presentation.employee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.GajiKaryawan
import id.kalkulasa.domain.usecase.HitungGajiUseCase
import id.kalkulasa.presentation.component.KalkulasaEmptyState
import id.kalkulasa.presentation.component.OnLifecycleResume
import id.kalkulasa.presentation.theme.Rugi
import id.kalkulasa.presentation.theme.Sukses
import kotlinx.coroutines.flow.collectLatest

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun EmployeeScreen(
    onBack: () -> Unit,
    viewModel: EmployeeViewModel = hiltViewModel(),
) {
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val attendance by viewModel.attendance.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val payroll by viewModel.payroll.collectAsStateWithLifecycle()
    val payrollPeriod by viewModel.payrollPeriod.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }

    var tab by remember { mutableStateOf(0) }
    var employeeForm by remember { mutableStateOf<Employee?>(null) }
    var showEmployeeForm by remember { mutableStateOf(false) }
    var kasbonTarget by remember { mutableStateOf<Employee?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EmployeeEvent.Message -> snackbar.showSnackbar(
                    when (event.text) {
                        "SAVED" -> context.getString(R.string.employee_saved)
                        "ATTENDANCE_SAVED" -> context.getString(R.string.attendance_saved)
                        "SALARY_PAID" -> context.getString(R.string.salary_paid)
                        else -> event.text
                    },
                )
            }
        }
    }
    LaunchedEffect(tab) { if (tab == 2) viewModel.loadPayroll() }
    OnLifecycleResume { if (tab == 2) viewModel.loadPayroll() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text(context.getString(R.string.employees_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text(context.getString(R.string.button_back)) }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(context.getString(R.string.tab_employees)) })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(context.getString(R.string.tab_attendance)) })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(context.getString(R.string.tab_payroll)) })
            }
            when (tab) {
                0 -> EmployeeListTab(
                    employees = employees,
                    onAdd = { employeeForm = null; showEmployeeForm = true },
                    onEdit = { employeeForm = it; showEmployeeForm = true },
                    onToggleActive = viewModel::setActive,
                    onKasbon = { kasbonTarget = it },
                )
                1 -> AttendanceTab(
                    employees = employees,
                    attendance = attendance,
                    selectedDate = selectedDate,
                    onDate = viewModel::selectDate,
                    onMark = viewModel::markAttendance,
                )
                2 -> PayrollTab(
                    payroll = payroll,
                    period = payrollPeriod,
                    onPeriod = viewModel::selectPayrollPeriod,
                    onPay = viewModel::paySalary,
                )
            }
        }
    }

    if (showEmployeeForm) {
        EmployeeFormDialog(
            initial = employeeForm,
            onDismiss = { showEmployeeForm = false },
            onSave = { employee ->
                viewModel.saveEmployee(employee)
                showEmployeeForm = false
            },
        )
    }
    kasbonTarget?.let { employee ->
        KasbonDialog(
            employee = employee,
            onDismiss = { kasbonTarget = null },
            onSave = { nominal, catatan ->
                viewModel.addKasbon(employee.id, nominal, catatan)
                kasbonTarget = null
            },
        )
    }
}

@Composable
private fun EmployeeListTab(
    employees: List<Employee>,
    onAdd: () -> Unit,
    onEdit: (Employee) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onKasbon: (Employee) -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = onAdd, modifier = Modifier.fillMaxWidth()) { Text(context.getString(R.string.add_employee)) }
        if (employees.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.employees_empty), body = "")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(employees, key = { it.id }) { employee ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEdit(employee) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(employee.nama, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${employee.jabatan ?: context.getString(R.string.employee_role_default)} · ${RupiahFormatter.format(employee.upahHarian)}/hari",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    if (employee.aktif) context.getString(R.string.employee_active) else context.getString(R.string.employee_inactive),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (employee.aktif) Sukses else Rugi,
                                )
                            }
                            Checkbox(checked = employee.aktif, onCheckedChange = { onToggleActive(employee.id, it) })
                            IconButton(onClick = { onKasbon(employee) }) {
                                Text("+", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceTab(
    employees: List<Employee>,
    attendance: List<Attendance>,
    selectedDate: Long,
    onDate: (Long) -> Unit,
    onMark: (String, String, String?) -> Unit,
) {
    val context = LocalContext.current
    val dayMillis = 24L * 60 * 60 * 1000
    val active = employees.filter { it.aktif }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = { onDate(selectedDate - dayMillis) }) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
            Text(DateFormatter.formatDayMonthYear(selectedDate), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onDate(selectedDate + dayMillis) }) { Text("›", style = MaterialTheme.typography.headlineMedium) }
        }
        if (active.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.employees_empty), body = "")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(active, key = { it.id }) { employee ->
                    val current = attendance.find { it.karyawanId == employee.id }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(employee.nama, fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                HitungGajiUseCase.STATUSES.forEach { status ->
                                    FilterChip(
                                        selected = current?.status == status,
                                        onClick = { onMark(employee.id, status, null) },
                                        label = { Text(statusLabel(context, status)) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayrollTab(
    payroll: List<GajiKaryawan>,
    period: Int,
    onPeriod: (Int) -> Unit,
    onPay: (Employee) -> Unit,
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(0, 1, 2).forEach { months ->
                FilterChip(
                    selected = period == months,
                    onClick = { onPeriod(months) },
                    label = { Text(if (months == 0) context.getString(R.string.payroll_this_month) else String.format(context.getString(R.string.payroll_months_ago), months)) },
                )
            }
        }
        if (payroll.isEmpty()) {
            KalkulasaEmptyState(title = context.getString(R.string.payroll_empty), body = "")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(payroll, key = { it.karyawan.id }) { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.karyawan.nama, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${context.getString(R.string.attendance_present)}: ${item.hadir} · ${context.getString(R.string.payroll_kasbon)}: ${RupiahFormatter.format(item.totalKasbon)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(RupiahFormatter.format(item.dibayar), style = MaterialTheme.typography.titleMedium, color = Sukses)
                                Button(onClick = { onPay(item.karyawan) }, enabled = item.dibayar > 0 && !item.sudahDibayar) {
                                    Text(if (item.sudahDibayar) context.getString(R.string.payroll_paid) else context.getString(R.string.payroll_pay))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeFormDialog(
    initial: Employee?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit,
) {
    val context = LocalContext.current
    var nama by remember(initial) { mutableStateOf(initial?.nama ?: "") }
    var jabatan by remember(initial) { mutableStateOf(initial?.jabatan ?: "") }
    var telepon by remember(initial) { mutableStateOf(initial?.telepon ?: "") }
    var upah by remember(initial) { mutableStateOf(initial?.upahHarian?.toString() ?: "") }
    var aktif by remember(initial) { mutableStateOf(initial?.aktif ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(context.getString(if (initial == null) R.string.add_employee else R.string.edit_employee)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nama, { nama = it }, label = { Text(context.getString(R.string.employee_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(jabatan, { jabatan = it }, label = { Text(context.getString(R.string.employee_role)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(telepon, { telepon = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.employee_phone)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(upah, { upah = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.employee_daily_wage)) }, prefix = { Text("Rp ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = aktif, onCheckedChange = { aktif = it })
                    Text(context.getString(R.string.employee_active))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    Employee(
                        id = initial?.id ?: java.util.UUID.randomUUID().toString(),
                        nama = nama, jabatan = jabatan.ifBlank { null }, telepon = telepon.ifBlank { null },
                        upahHarian = upah.toLongOrNull() ?: 0L, aktif = aktif,
                    ),
                )
            }) { Text(context.getString(R.string.button_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(context.getString(R.string.button_cancel)) } },
    )
}

@Composable
private fun KasbonDialog(
    employee: Employee,
    onDismiss: () -> Unit,
    onSave: (Long, String?) -> Unit,
) {
    val context = LocalContext.current
    var nominal by remember { mutableStateOf("") }
    var catatan by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${context.getString(R.string.payroll_kasbon)}: ${employee.nama}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(nominal, { nominal = it.filter(Char::isDigit) }, label = { Text(context.getString(R.string.amount)) }, prefix = { Text("Rp ") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(catatan, { catatan = it }, label = { Text(context.getString(R.string.note)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = { onSave(nominal.toLongOrNull() ?: 0L, catatan.ifBlank { null }) }) { Text(context.getString(R.string.button_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(context.getString(R.string.button_cancel)) } },
    )
}

@Composable
private fun statusLabel(context: android.content.Context, status: String): String = when (status) {
    HitungGajiUseCase.HADIR -> context.getString(R.string.attendance_present)
    HitungGajiUseCase.IZIN -> context.getString(R.string.attendance_permit)
    HitungGajiUseCase.SAKIT -> context.getString(R.string.attendance_sick)
    HitungGajiUseCase.ALPHA -> context.getString(R.string.attendance_absent)
    else -> status
}
