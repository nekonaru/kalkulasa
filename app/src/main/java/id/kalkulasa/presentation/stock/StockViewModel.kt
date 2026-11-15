package id.kalkulasa.presentation.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.StockMovement
import id.kalkulasa.domain.repository.CategoryRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.StockRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

sealed interface StockEvent {
    /** Text is a user-facing message, or a SAVED marker. */
    data class Message(val text: String) : StockEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val products: StateFlow<List<Product>> = _query
        .flatMapLatest { q -> productRepository.observeAll(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = productRepository.observeLowStock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val movements: StateFlow<List<StockMovement>> = stockRepository.observeRecentMovements(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val productCategories: StateFlow<List<Category>> = categoryRepository.observeByType(
        id.kalkulasa.domain.model.CategoryType.PRODUCT,
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<StockEvent>(Channel.BUFFERED)
    val events: Flow<StockEvent> = _events.receiveAsFlow()

    fun onQueryChange(q: String) { _query.value = q }

    fun saveProduct(
        editing: Product?,
        nama: String,
        kategoriId: String?,
        satuan: String,
        barcode: String,
        hargaBeli: Long,
        hargaJual: Long,
        stokAwal: Double,
        stokMinimum: Double,
    ) {
        viewModelScope.launch {
            val trimmed = nama.trim()
            if (trimmed.isEmpty()) return@launch
            val base = editing
            val product = Product(
                id = base?.id ?: UUID.randomUUID().toString(),
                nama = trimmed,
                kategoriId = kategoriId,
                satuan = satuan.trim().ifEmpty { "pcs" },
                barcode = barcode.trim().ifEmpty { null },
                hargaBeliRata2 = if (base != null) base.hargaBeliRata2 else hargaBeli,
                hargaJual = hargaJual,
                stok = if (base != null) base.stok else stokAwal,
                stokMinimum = stokMinimum,
                fotoUri = base?.fotoUri,
                diarsipkan = base?.diarsipkan ?: false,
            )
            productRepository.save(product)
            _events.send(StockEvent.Message("SAVED"))
        }
    }

    fun setArchived(productId: String, archived: Boolean) {
        viewModelScope.launch { productRepository.setArchived(productId, archived) }
    }

    fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { categoryRepository.addCategory(trimmed, id.kalkulasa.domain.model.CategoryType.PRODUCT) }
    }

    fun recordStockIn(productId: String?, qtyText: String, priceText: String, pemasok: String, catatan: String) {
        val id = productId ?: return
        val qty = qtyText.replace(',', '.').toDoubleOrNull() ?: return
        val price = priceText.filter { it.isDigit() }.toLongOrNull() ?: return
        viewModelScope.launch {
            stockRepository.recordStockIn(id, qty, price, pemasok, catatan)
                .onSuccess { _events.send(StockEvent.Message("STOCK_IN_SAVED")) }
                .onFailure { _events.send(StockEvent.Message(it.message ?: "ERROR")) }
        }
    }

    fun recordAdjustment(productId: String?, newQtyText: String, reason: String) {
        val id = productId ?: return
        val newQty = newQtyText.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            stockRepository.recordAdjustment(id, newQty, reason)
                .onSuccess { _events.send(StockEvent.Message("ADJUSTMENT_SAVED")) }
                .onFailure { _events.send(StockEvent.Message(it.message ?: "ERROR")) }
        }
    }
}
