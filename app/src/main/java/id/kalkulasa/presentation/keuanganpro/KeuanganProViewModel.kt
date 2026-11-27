package id.kalkulasa.presentation.keuanganpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.repository.ProfitShareRepository
import id.kalkulasa.domain.repository.RecurringExpenseRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeuanganProViewModel @Inject constructor(
    private val recurringRepository: RecurringExpenseRepository,
    private val shareRepository: ProfitShareRepository,
) : ViewModel() {

    val recurring = recurringRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val shares = shareRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = _events.receiveAsFlow()

    fun addRecurring(kategori: String, nominalText: String, hariText: String) {
        val nominal = RupiahFormatter.parseOrZero(nominalText)
        val hari = hariText.toIntOrNull() ?: 1
        if (kategori.isBlank() || nominal <= 0) return
        viewModelScope.launch {
            recurringRepository.save(kategori, nominal, hari)
                .onSuccess { _events.send("SAVED") }
        }
    }

    fun addShare(nama: String, persenText: String) {
        val persen = persenText.toIntOrNull() ?: return
        if (nama.isBlank()) return
        viewModelScope.launch {
            shareRepository.save(nama, persen)
                .onSuccess { _events.send("SAVED") }
        }
    }
}
