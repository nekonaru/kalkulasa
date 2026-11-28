package id.kalkulasa.presentation.shiftpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.repository.EmployeeRepository
import id.kalkulasa.domain.repository.ShiftRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftProViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val employeeRepository: EmployeeRepository,
) : ViewModel() {

    val shifts = shiftRepository.observeShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val employees = employeeRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addShift(nama: String, mulai: Int, selesai: Int) {
        if (nama.isBlank()) return
        viewModelScope.launch { shiftRepository.saveShift(nama, mulai, selesai) }
    }

    fun setPin(employeeId: String, pin: String) {
        viewModelScope.launch { shiftRepository.setPin(employeeId, pin) }
    }

    fun addOvertime(employeeId: String, menit: Int) {
        viewModelScope.launch {
            val employee = employeeRepository.observeAll().first().find { it.id == employeeId }
            if (employee != null) shiftRepository.addOvertime(employeeId, menit, employee.upahHarian)
        }
    }
}
