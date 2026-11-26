package id.kalkulasa.presentation.employee

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.Attendance
import id.kalkulasa.domain.model.Employee
import id.kalkulasa.domain.model.GajiKaryawan
import id.kalkulasa.domain.repository.AttendanceRepository
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.repository.KasbonRepository
import id.kalkulasa.domain.repository.PayrollRepository
import id.kalkulasa.domain.usecase.HitungGajiUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface EmployeeEvent {
    data class Message(val text: String) : EmployeeEvent
}

@HiltViewModel
class EmployeeViewModel @Inject constructor(
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val kasbonRepository: KasbonRepository,
    private val payrollRepository: PayrollRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val employees: StateFlow<List<Employee>> = employeeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedDate = MutableStateFlow(DateFormatter.startOfDay())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val attendance: StateFlow<List<Attendance>> = _selectedDate
        .flatMapLatest { date ->
            attendanceRepository.observeBetween(date, DateFormatter.endOfDay(date))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val payrollHistory: StateFlow<List<id.kalkulasa.domain.model.PayrollRecord>> = payrollRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _payroll = MutableStateFlow<List<GajiKaryawan>>(emptyList())
    val payroll: StateFlow<List<GajiKaryawan>> = _payroll.asStateFlow()

    private val _payrollPeriod = MutableStateFlow(0) // months back
    val payrollPeriod: StateFlow<Int> = _payrollPeriod.asStateFlow()

    private val _events = Channel<EmployeeEvent>(Channel.BUFFERED)
    val events: Flow<EmployeeEvent> = _events.receiveAsFlow()

    fun selectDate(date: Long) { _selectedDate.value = DateFormatter.startOfDay(date) }
    fun selectPayrollPeriod(monthsBack: Int) {
        _payrollPeriod.value = monthsBack
        loadPayroll()
    }

    fun saveEmployee(employee: Employee) {
        if (employee.nama.isBlank()) return
        viewModelScope.launch {
            employeeRepository.save(employee.copy(nama = employee.nama.trim()))
            _events.send(EmployeeEvent.Message("SAVED"))
        }
    }

    fun setActive(id: String, aktif: Boolean) {
        viewModelScope.launch { employeeRepository.setActive(id, aktif) }
    }

    fun markAttendance(karyawanId: String, status: String, catatan: String?) {
        viewModelScope.launch {
            val employee = employeeRepository.getById(karyawanId) ?: return@launch
            val upah = HitungGajiUseCase.upahUntukStatus(employee.upahHarian, status)
            attendanceRepository.mark(karyawanId, _selectedDate.value, status, upah, catatan)
                .onSuccess { _events.send(EmployeeEvent.Message("ATTENDANCE_SAVED")) }
                .onFailure { _events.send(EmployeeEvent.Message("ERROR")) }
        }
    }

    fun addKasbon(karyawanId: String, nominal: Long, catatan: String?) {
        if (nominal <= 0) return
        viewModelScope.launch {
            kasbonRepository.add(karyawanId, nominal, catatan)
            loadPayroll()
            _events.send(EmployeeEvent.Message("SAVED"))
        }
    }

    fun loadPayroll() {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = now
        calendar.add(java.util.Calendar.MONTH, -_payrollPeriod.value)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val from = DateFormatter.startOfDay(calendar.timeInMillis)
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
        val to = DateFormatter.endOfDay(calendar.timeInMillis)

        viewModelScope.launch {
            val data = employees.first()
            val absen = attendanceRepository.getBetween(from, to)
            val history = payrollRepository.observeAll().first()
            val result = data.filter { it.aktif }.map { employee ->
                val paidBefore = history.filter { it.karyawanId == employee.id && it.periodeAwal == from }
                    .sumOf { it.dibayar }
                val gaji = HitungGajiUseCase.hitung(
                    karyawan = employee,
                    absensi = absen.filter { it.karyawanId == employee.id },
                    kasbon = kasbonRepository.getForEmployee(employee.id, from, to),
                    sudahDibayar = paidBefore,
                )
                // A partial payment still leaves a payable remainder, so the Pay
                // button must stay enabled until the remainder reaches zero.
                gaji.copy(sudahDibayar = gaji.dibayar <= 0L)
            }
            _payroll.value = result
        }
    }

    private val _paying = MutableStateFlow(false)
    val paying: StateFlow<Boolean> = _paying.asStateFlow()

    fun paySalary(employee: Employee) {
        if (_paying.value) return
        val item = _payroll.value.find { it.karyawan.id == employee.id } ?: return
        val monthsBack = _payrollPeriod.value
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.MONTH, -monthsBack)
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val from = DateFormatter.startOfDay(calendar.timeInMillis)
        calendar.add(java.util.Calendar.MONTH, 1)
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
        val to = DateFormatter.endOfDay(calendar.timeInMillis)

        val kategori = context.getString(R.string.expense_gaji)
        _paying.value = true
        viewModelScope.launch {
            payrollRepository.pay(
                employee, from, to, item.totalUpah, item.totalKasbon, item.dibayar,
                kategori, "$kategori ${employee.nama}",
            )
                .onSuccess { loadPayroll(); _events.send(EmployeeEvent.Message("SALARY_PAID")) }
                .onFailure { _events.send(EmployeeEvent.Message("ERROR")) }
            _paying.value = false
        }
    }
}
