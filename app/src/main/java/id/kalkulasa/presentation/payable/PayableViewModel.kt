package id.kalkulasa.presentation.payable

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.R
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Payable
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.repository.ProfileRepository
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

sealed interface PayableEvent {
    data class Message(val text: String) : PayableEvent
}

@HiltViewModel
class PayableViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val payableRepository: PayableRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    val items: StateFlow<List<Payable>> = payableRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val outstanding: StateFlow<Long> = payableRepository.observeOutstanding()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _events = Channel<PayableEvent>(Channel.BUFFERED)
    val events: Flow<PayableEvent> = _events.receiveAsFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun onQueryChange(value: String) { _query.value = value }

    fun add(pemasok: String, nominalText: String, dueDaysText: String, catatan: String) {
        val nominal = RupiahFormatter.parse(nominalText).getOrNull() ?: return
        if (pemasok.isBlank() || nominal <= 0) return
        val days = dueDaysText.filter { it.isDigit() }.toIntOrNull()?.takeIf { it > 0 }
        val due = days?.let { System.currentTimeMillis() + it * 24L * 60 * 60 * 1000 }
        viewModelScope.launch {
            payableRepository.add(pemasok, nominal, due, catatan)
                .onSuccess { _events.send(PayableEvent.Message("SAVED")) }
                .onFailure { _events.send(PayableEvent.Message("ERROR")) }
        }
    }

    fun recordPayment(id: String, amountText: String) {
        val amount = RupiahFormatter.parse(amountText).getOrNull() ?: return
        if (amount <= 0) return
        viewModelScope.launch {
            payableRepository.recordPayment(id, amount)
                .onSuccess { _events.send(PayableEvent.Message("PAYMENT_SAVED")) }
                .onFailure { _events.send(PayableEvent.Message("ERROR")) }
        }
    }

    fun shareReminder(item: Payable) {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            val usaha = profile?.namaUsaha ?: "Usaha kami"
            val sisa = RupiahFormatter.format(item.sisa)
            val due = item.jatuhTempo?.let {
                context.getString(R.string.payable_reminder_due, DateFormatter.formatDayMonthYear(it))
            } ?: ""
            val text = context.getString(
                R.string.payable_reminder_message,
                item.pemasok,
                usaha,
                sisa,
                due,
            )
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
