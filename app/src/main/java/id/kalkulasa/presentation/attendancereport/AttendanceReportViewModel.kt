package id.kalkulasa.presentation.attendancereport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.repository.AttendanceRepository
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.usecase.HitungGajiUseCase
import id.kalkulasa.presentation.reports.ReportPeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AttendanceRow(val nama: String, val hadir: Int, val totalUpah: Long)

data class AttendanceReportUiState(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val rows: List<AttendanceRow> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class AttendanceReportViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val employeeRepository: EmployeeRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AttendanceReportUiState())
    val state: StateFlow<AttendanceReportUiState> = _state.asStateFlow()

    init { load() }

    fun selectPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period, loading = true)
        load()
    }

    fun load() {
        val period = _state.value.period
        val now = System.currentTimeMillis()
        val (from, to) = when (period) {
            ReportPeriod.TODAY -> DateFormatter.startOfDay(now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_7 -> DateFormatter.startOfDayDaysAgo(6, now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_30 -> DateFormatter.startOfDayDaysAgo(29, now) to DateFormatter.endOfDay(now)
            ReportPeriod.THIS_MONTH -> DateFormatter.startOfMonth(now) to DateFormatter.endOfDay(now)
        }
        viewModelScope.launch {
            try {
                val employees = employeeRepository.observeAll().first()
                val attendance = attendanceRepository.getBetween(from, to)
                val rows = employees.map { employee ->
                    val own = attendance.filter { it.karyawanId == employee.id }
                    AttendanceRow(
                        nama = employee.nama,
                        hadir = own.count { it.status == HitungGajiUseCase.HADIR },
                        totalUpah = own.sumOf { it.upahDibayar.coerceAtLeast(0L) },
                    )
                }.filter { it.hadir > 0 || it.totalUpah > 0 }
                if (_state.value.period == period) {
                    _state.value = AttendanceReportUiState(period, rows, false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }
}
