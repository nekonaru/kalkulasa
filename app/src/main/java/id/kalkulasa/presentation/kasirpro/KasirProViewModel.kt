package id.kalkulasa.presentation.kasirpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Bundle
import id.kalkulasa.domain.model.Preorder
import id.kalkulasa.domain.model.PreorderLine
import id.kalkulasa.domain.repository.BundleRepository
import id.kalkulasa.domain.repository.PreorderRepository
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

sealed interface KasirProEvent { data class Message(val text: String) : KasirProEvent }

@HiltViewModel
class KasirProViewModel @Inject constructor(
    private val preorderRepository: PreorderRepository,
    private val bundleRepository: BundleRepository,
    private val productRepository: id.kalkulasa.domain.repository.ProductRepository,
) : ViewModel() {

    val preorders: StateFlow<List<Preorder>> = preorderRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bundles: StateFlow<List<Bundle>> = bundleRepository.observeActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val products: StateFlow<List<id.kalkulasa.domain.model.Product>> =
        productRepository.observeActive("")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createQuickPreorder(nama: String) {
        val product = products.value.firstOrNull() ?: return
        createPreorder(nama, "", listOf(Triple(product.id, product.nama, "1")))
    }

    private val _events = Channel<KasirProEvent>(Channel.BUFFERED)
    val events: Flow<KasirProEvent> = _events.receiveAsFlow()

    fun createPreorder(nama: String, catatan: String, items: List<Triple<String, String, String>>) {
        val lines = items.mapNotNull { (id, namaProduk, qtyText) ->
            val qty = qtyText.replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
            Triple(id, namaProduk, qty)
        }
        if (lines.isEmpty()) return
        viewModelScope.launch {
            preorderRepository.save(
                nama.ifBlank { null },
                catatan,
                lines.map { (id, name, qty) ->
                    val price = productRepository.getById(id)?.hargaJual ?: 0L
                    PreorderLine(id, name, qty, price)
                },
            )
                .onSuccess { _events.send(KasirProEvent.Message("SAVED")) }
                .onFailure { _events.send(KasirProEvent.Message("ERROR")) }
        }
    }

    fun changeStatus(id: String, status: String) {
        viewModelScope.launch { preorderRepository.mark(id, status) }
    }

    fun createBundle(nama: String, hargaText: String, items: List<Pair<String, String>>) {
        val harga = RupiahFormatter.parseOrZero(hargaText)
        val mapped = items.mapNotNull { (id, qtyText) ->
            val qty = qtyText.replace(',', '.').toDoubleOrNull() ?: return@mapNotNull null
            id to qty
        }
        if (nama.isBlank() || harga <= 0 || mapped.isEmpty()) return
        viewModelScope.launch {
            bundleRepository.save(nama, harga, mapped)
                .onSuccess { _events.send(KasirProEvent.Message("SAVED")) }
                .onFailure { _events.send(KasirProEvent.Message("ERROR")) }
        }
    }
}
