package id.kalkulasa.presentation.keuanganakhir

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.model.Payable
import id.kalkulasa.domain.usecase.PpnBatchUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeuanganAkhirViewModel @Inject constructor(
    private val payableRepository: PayableRepository,
    private val financeRepository: FinanceRepository,
) : ViewModel() {

    val payables: StateFlow<List<Payable>> = payableRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ppn = MutableStateFlow(0L)
    val ppn: StateFlow<Long> = _ppn.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    fun toggle(id: String) {
        _selected.value = if (id in _selected.value) _selected.value - id else _selected.value + id
    }

    fun hitungPpn(totalText: String, aktif: Boolean) {
        val total = totalText.filter(Char::isDigit).toLongOrNull() ?: 0L
        _ppn.value = PpnBatchUseCase.ppnKeluaran(total, aktif)
    }

    fun bayarBatch(nominalPer: Map<String, Long>) {
        val items = _selected.value.mapNotNull { id -> nominalPer[id]?.let { id to it } }
        if (items.isEmpty()) return
        viewModelScope.launch { payableRepository.payBatch(items) }
    }
}
