package id.kalkulasa.presentation.promotion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Deposit
import id.kalkulasa.domain.model.Voucher
import id.kalkulasa.domain.repository.DepositRepository
import id.kalkulasa.domain.repository.VoucherRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface PromotionEvent {
    data class Message(val text: String) : PromotionEvent
}

@HiltViewModel
class PromotionViewModel @Inject constructor(
    private val voucherRepository: VoucherRepository,
    private val depositRepository: DepositRepository,
) : ViewModel() {

    val vouchers: StateFlow<List<Voucher>> = voucherRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val deposits: StateFlow<List<Deposit>> = depositRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<PromotionEvent>(Channel.BUFFERED)
    val events: Flow<PromotionEvent> = _events.receiveAsFlow()

    fun saveVoucher(kode: String, tipe: String, nilaiText: String, minText: String, kuotaText: String, daysText: String) {
        val nilai = RupiahFormatter.parseOrZero(nilaiText)
        if (kode.isBlank() || nilai <= 0) return
        val kuota = kuotaText.filter(Char::isDigit).toIntOrNull()?.takeIf { it > 0 } ?: 1
        val days = daysText.filter(Char::isDigit).toIntOrNull()?.takeIf { it > 0 } ?: 30
        viewModelScope.launch {
            voucherRepository.save(
                Voucher(
                    id = UUID.randomUUID().toString(),
                    kode = kode.trim().uppercase(),
                    tipe = tipe,
                    nilai = nilai,
                    minimalBelanja = RupiahFormatter.parseOrZero(minText),
                    sisaKuota = kuota,
                    berlakuSampai = System.currentTimeMillis() + days.toLong() * 24 * 60 * 60 * 1000,
                ),
            )
                .onSuccess { _events.send(PromotionEvent.Message("SAVED")) }
                .onFailure { _events.send(PromotionEvent.Message("ERROR")) }
        }
    }

    fun addDeposit(nama: String, nominalText: String, catatan: String) {
        val nominal = RupiahFormatter.parseOrZero(nominalText)
        if (nama.isBlank() || nominal <= 0) return
        viewModelScope.launch {
            depositRepository.add(nama.trim(), nominal, catatan)
                .onSuccess { _events.send(PromotionEvent.Message("SAVED")) }
                .onFailure { _events.send(PromotionEvent.Message("ERROR")) }
        }
    }
}
