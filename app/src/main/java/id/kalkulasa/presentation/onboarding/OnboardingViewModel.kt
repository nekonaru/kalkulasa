package id.kalkulasa.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.BusinessProfile
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.core.util.RupiahFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class OnboardingUiState(
    val namaUsaha: String = "",
    val jenisUsaha: String = "",
    val modalAwal: String = "",
    val error: String? = null,
    val saving: Boolean = false,
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val financeRepository: FinanceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun onNama(value: String) { _state.value = _state.value.copy(namaUsaha = value, error = null) }
    fun onJenis(value: String) { _state.value = _state.value.copy(jenisUsaha = value) }
    fun onModal(value: String) { _state.value = _state.value.copy(modalAwal = value.filter { it.isDigit() }, error = null) }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        val nama = s.namaUsaha.trim()
        if (nama.isEmpty()) {
            _state.value = s.copy(error = "NAME_REQUIRED")
            return
        }
        val modal = RupiahFormatter.parse(s.modalAwal.ifEmpty { "0" })
        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            profileRepository.saveProfile(
                BusinessProfile(
                    id = UUID.randomUUID().toString(),
                    namaUsaha = nama,
                    jenisUsaha = s.jenisUsaha.trim().ifEmpty { "Lainnya" },
                    modalAwal = modal,
                    dibuatPada = System.currentTimeMillis(),
                ),
            )
            if (modal > 0) {
                financeRepository.addCapital("MODAL_AWAL", modal, null)
            }
            _state.value = _state.value.copy(saving = false)
            onDone()
        }
    }
}
