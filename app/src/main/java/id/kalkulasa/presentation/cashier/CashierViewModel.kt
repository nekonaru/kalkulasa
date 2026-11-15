package id.kalkulasa.presentation.cashier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.PaymentMethod
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.SaleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartEntry(val product: Product, val qty: Double) {
    val lineTotal: Long get() = (product.hargaJual * qty).toLong()
}

sealed interface CashierEvent {
    /** text is either an error message or one of the SALE_SAVED / SALE_CANCELLED markers. */
    data class Message(val text: String) : CashierEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CashierViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val _discount = MutableStateFlow("")
    val discount: StateFlow<String> = _discount.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.TUNAI)
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    /** Products currently visible in the picker (filtered by query). */
    val products: StateFlow<List<Product>> = _query
        .flatMapLatest { q -> productRepository.observeActive(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allActiveProducts: StateFlow<List<Product>> =
        productRepository.observeActive("")
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val cartEntries: StateFlow<List<CartEntry>> = combine(_cart, allActiveProducts) { cart, all ->
        cart.mapNotNull { (id, qty) -> all.find { it.id == id }?.let { CartEntry(it, qty) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subtotal: StateFlow<Long> = cartEntries
        .map { entries -> entries.sumOf { it.lineTotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val discountAmount: StateFlow<Long> = _discount
        .map { RupiahFormatter.parse(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val total: StateFlow<Long> = combine(subtotal, discountAmount) { sub, diskon -> (sub - diskon).coerceAtLeast(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val _todaySales = MutableStateFlow<List<Sale>>(emptyList())
    val todaySales: StateFlow<List<Sale>> = _todaySales.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _events = Channel<CashierEvent>(Channel.BUFFERED)
    val events: Flow<CashierEvent> = _events.receiveAsFlow()

    init {
        refreshTodaySales()
    }

    fun onQueryChange(q: String) { _query.value = q }
    fun onDiscountChange(value: String) { _discount.value = value.filter { it.isDigit() }.take(12) }
    fun onPaymentMethodChange(method: String) { _paymentMethod.value = method }

    fun addToCart(product: Product) {
        _cart.value = _cart.value + (product.id to ((_cart.value[product.id] ?: 0.0) + 1))
    }

    fun removeFromCart(productId: String) {
        _cart.value = _cart.value - productId
    }

    fun increment(productId: String) {
        _cart.value = _cart.value + (productId to ((_cart.value[productId] ?: 0.0) + 1))
    }

    fun decrement(productId: String) {
        val next = (_cart.value[productId] ?: 0.0) - 1
        _cart.value = if (next <= 0) _cart.value - productId else _cart.value + (productId to next)
    }

    fun clearCart() {
        _cart.value = emptyMap()
        _discount.value = ""
    }

    fun saveSale() {
        val entries = cartEntries.value
        if (entries.isEmpty() || _saving.value) return
        _saving.value = true
        viewModelScope.launch {
            val result = saleRepository.recordSale(
                items = entries.map { it.product to it.qty },
                diskon = discountAmount.value,
                metodeBayar = _paymentMethod.value,
                catatan = null,
            )
            _saving.value = false
            result.onSuccess {
                clearCart()
                refreshTodaySales()
                _events.send(CashierEvent.Message("SALE_SAVED"))
            }.onFailure { error ->
                _events.send(CashierEvent.Message(error.message ?: "ERROR"))
            }
        }
    }

    fun cancelSale(saleId: String) {
        viewModelScope.launch {
            saleRepository.cancelSale(saleId)
                .onSuccess {
                    refreshTodaySales()
                    _events.send(CashierEvent.Message("SALE_CANCELLED"))
                }
                .onFailure { _events.send(CashierEvent.Message("ERROR")) }
        }
    }

    fun refreshTodaySales() {
        viewModelScope.launch {
            val from = DateFormatter.startOfDay()
            val sales = saleRepository.salesBetween(from, DateFormatter.endOfDay())
            _todaySales.value = sales.filter { it.status == "SELESAI" }
        }
    }
}
