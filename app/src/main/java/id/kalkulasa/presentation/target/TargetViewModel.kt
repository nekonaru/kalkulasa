package id.kalkulasa.presentation.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.SalesTarget
import id.kalkulasa.domain.repository.SalesTargetRepository
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

sealed interface TargetEvent {
    data class Message(val text: String) : TargetEvent
}

@HiltViewModel
class TargetViewModel @Inject constructor(
    private val repository: SalesTargetRepository,
) : ViewModel() {

    val targets: StateFlow<List<SalesTarget>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _daily = MutableStateFlow("")
    val daily: StateFlow<String> = _daily.asStateFlow()

    private val _monthly = MutableStateFlow("")
    val monthly: StateFlow<String> = _monthly.asStateFlow()

    private val _events = Channel<TargetEvent>(Channel.BUFFERED)
    val events: Flow<TargetEvent> = _events.receiveAsFlow()

    fun onDailyChange(value: String) { _daily.value = value.filter(Char::isDigit).take(12) }
    fun onMonthlyChange(value: String) { _monthly.value = value.filter(Char::isDigit).take(12) }

    fun save() {
        val daily = RupiahFormatter.parseOrZero(_daily.value).coerceAtLeast(0L)
        val monthly = RupiahFormatter.parseOrZero(_monthly.value).coerceAtLeast(0L)
        viewModelScope.launch {
            repository.save(SalesTarget.HARIAN, daily)
                .onFailure { _events.send(TargetEvent.Message("ERROR")); return@launch }
            repository.save(SalesTarget.BULANAN, monthly)
                .onSuccess { _events.send(TargetEvent.Message("SAVED")) }
                .onFailure { _events.send(TargetEvent.Message("ERROR")) }
        }
    }
}
