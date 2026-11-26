package id.kalkulasa.presentation.opname

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.StockRepository
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

sealed interface OpnameEvent {
    data class Message(val text: String) : OpnameEvent
}

@HiltViewModel
class StockOpnameViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.observeActive("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _counts = MutableStateFlow<Map<String, String>>(emptyMap())
    val counts: StateFlow<Map<String, String>> = _counts.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _events = Channel<OpnameEvent>(Channel.BUFFERED)
    val events: Flow<OpnameEvent> = _events.receiveAsFlow()

    fun onCount(id: String, value: String) {
        val filtered = value.filter { it.isDigit() || it == ',' || it == '.' }
        _counts.value = _counts.value + (id to filtered)
    }

    fun save(reason: String) {
        if (_saving.value) return
        val items = products.value.mapNotNull { product ->
            val raw = _counts.value[product.id]?.replace(',', '.') ?: return@mapNotNull null
            val qty = raw.toDoubleOrNull() ?: return@mapNotNull null
            if (qty < 0.0 || qty == product.stok) null else product.id to qty
        }
        if (items.isEmpty()) {
            viewModelScope.launch { _events.send(OpnameEvent.Message("NO_CHANGE")) }
            return
        }
        _saving.value = true
        viewModelScope.launch {
            stockRepository.applyAdjustments(items, reason)
                .onSuccess { count -> _events.send(OpnameEvent.Message("SAVED:$count")) }
                .onFailure { _events.send(OpnameEvent.Message("ERROR")) }
            _saving.value = false
        }
    }
}
