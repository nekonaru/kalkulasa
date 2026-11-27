package id.kalkulasa.presentation.crmpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.usecase.CrmUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CrmRow(val nama: String, val rfm: Int, val hariUlangTahun: Int?)

@HiltViewModel
class CrmProViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _rows = MutableStateFlow<List<CrmRow>>(emptyList())
    val rows: StateFlow<List<CrmRow>> = _rows.asStateFlow()

    fun load() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            _rows.value = customerRepository.observeAll().first()
                .map {
                    CrmRow(
                        it.nama,
                        CrmUseCase.skorRfm(it.kunjungan, it.totalBelanja),
                        CrmUseCase.hariMenujuUlangTahun(it.tanggalLahir ?: 0L, now),
                    )
                }
                .sortedByDescending { it.rfm }
        }
    }
}
