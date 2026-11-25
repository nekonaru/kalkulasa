package id.kalkulasa.presentation.piutang

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.Receivable
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.ReceivableRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PiutangEvent {
    /** Text is SAVED, ERROR or an exception message. */
    data class Message(val text: String) : PiutangEvent
}

@HiltViewModel
class PiutangViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receivableRepository: ReceivableRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val items: StateFlow<List<Receivable>> = receivableRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val outstanding: StateFlow<Long> = receivableRepository.observeOutstanding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _events = Channel<PiutangEvent>(Channel.BUFFERED)
    val events: Flow<PiutangEvent> = _events.receiveAsFlow()

    fun addManual(nama: String, nominalText: String, dueDaysText: String, catatan: String) {
        val nominal = nominalText.filter { it.isDigit() }.toLongOrNull() ?: return
        val trimmed = nama.trim()
        if (trimmed.isEmpty() || nominal <= 0) return
        val dueDays = dueDaysText.filter { it.isDigit() }.toIntOrNull()
        val due = dueDays?.takeIf { it > 0 }?.let { System.currentTimeMillis() + it * 24L * 60 * 60 * 1000 }
        viewModelScope.launch {
            receivableRepository.addManual(trimmed, nominal, due, catatan)
                .onSuccess { _events.send(PiutangEvent.Message("SAVED")) }
                .onFailure { _events.send(PiutangEvent.Message(it.message ?: "ERROR")) }
        }
    }

    fun recordPayment(id: String, amountText: String) {
        val amount = amountText.filter { it.isDigit() }.toLongOrNull() ?: return
        if (amount <= 0) return
        viewModelScope.launch {
            receivableRepository.recordPayment(id, amount)
                .onSuccess { _events.send(PiutangEvent.Message("SAVED")) }
                .onFailure { _events.send(PiutangEvent.Message(it.message ?: "ERROR")) }
        }
    }

    fun shareReminder(item: Receivable) {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            val usaha = profile?.namaUsaha ?: "Usaha kami"
            val sisa = RupiahFormatter.format(item.sisa)
            val due = item.jatuhTempo?.let { " Jatuh tempo: ${DateFormatter.formatDayMonthYear(it)}." } ?: ""
            val text = "Halo ${item.namaPelanggan}, ini pengingat ramah dari $usaha. " +
                "Tagihan $sisa masih belum lunas.$due Terima kasih."
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text)))
            try {
                context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
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
}
