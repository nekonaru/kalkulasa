package id.kalkulasa.presentation.karyawanpro

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.repository.AttendanceRepository
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.repository.PayrollRepository
import id.kalkulasa.domain.usecase.HitungGajiUseCase
import id.kalkulasa.domain.usecase.KaryawanProUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RankingRow(val nama: String, val hadir: Int, val upah: Long, val skor: Double)

@HiltViewModel
class KaryawanProViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val employeeRepository: EmployeeRepository,
    private val attendanceRepository: AttendanceRepository,
    private val payrollRepository: PayrollRepository,
) : ViewModel() {

    private val _rows = MutableStateFlow<List<RankingRow>>(emptyList())
    val rows: StateFlow<List<RankingRow>> = _rows.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val from = DateFormatter.startOfMonth()
            val to = DateFormatter.endOfDay()
            val employees = employeeRepository.observeAll().first()
            val attendance = attendanceRepository.getBetween(from, to)
            val payrolls = payrollRepository.observeAll().first()
            val result = employees.map { e ->
                val own = attendance.filter { it.karyawanId == e.id }
                val hadir = own.count { it.status == HitungGajiUseCase.HADIR }
                val upah = payrolls.filter { it.karyawanId == e.id }.sumOf { it.totalUpah }
                RankingRow(e.nama, hadir, upah, KaryawanProUseCase.skor(upah, hadir * 5, hadir))
            }.sortedByDescending { it.skor }
            _rows.value = result
        }
    }

    fun shareSlip(row: RankingRow) {
        val text = KaryawanProUseCase.slipGaji(row.upah, 0L, 0L, row.upah)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text)))
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(fallback, null))
        }
    }
}
