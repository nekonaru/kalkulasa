package id.kalkulasa.presentation.cashier

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.model.PaymentMethod
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.SaleRepository
import id.kalkulasa.domain.usecase.PromosiUseCase
import id.kalkulasa.domain.usecase.StrukUseCase
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

enum class DiscountMode { RUPIAH, PERCENT }

data class CartEntry(val product: Product, val qty: Double) {
    val lineTotal: Long get() = kotlin.math.round(product.hargaJual * qty).toLong()
}

sealed interface CashierEvent {
    /** text is either an error message or one of the SALE_SAVED / SALE_CANCELLED markers. */
    data class Message(val text: String) : CashierEvent

    /** Emitted after a sale is saved; carries a ready-to-share receipt text. */
    data class SaleSaved(val receipt: String) : CashierEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CashierViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val profileRepository: ProfileRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Double>>(emptyMap())
    private val _discount = MutableStateFlow("")
    val discount: StateFlow<String> = _discount.asStateFlow()

    private val _discountMode = MutableStateFlow(DiscountMode.RUPIAH)
    val discountMode: StateFlow<DiscountMode> = _discountMode.asStateFlow()

    private val _poinDitukar = MutableStateFlow("")
    val poinDitukar: StateFlow<String> = _poinDitukar.asStateFlow()

    private val _paymentMethod = MutableStateFlow(PaymentMethod.TUNAI)
    val paymentMethod: StateFlow<String> = _paymentMethod.asStateFlow()

    private val _tendered = MutableStateFlow("")
    val tendered: StateFlow<String> = _tendered.asStateFlow()

    fun onTenderedChange(value: String) { _tendered.value = value.filter { it.isDigit() }.take(12) }

    private val _namaPelanggan = MutableStateFlow("")
    val namaPelanggan: StateFlow<String> = _namaPelanggan.asStateFlow()

    /** Points of the customer whose name is currently typed in the form. */
    val availablePoints: StateFlow<Long> = combine(customerRepository.observeAll(), _namaPelanggan) { customers, name ->
        customers.find { it.nama.equals(name.trim(), ignoreCase = true) }?.poin ?: 0L
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

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

    val discountAmount: StateFlow<Long> = combine(subtotal, _discount, _discountMode) { sub, text, mode ->
        when (mode) {
            DiscountMode.RUPIAH -> RupiahFormatter.parseOrZero(text)
            DiscountMode.PERCENT -> PromosiUseCase.diskonPersen(sub, text.filter { it.isDigit() }.toIntOrNull() ?: 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    private val pointsDiscount: StateFlow<Long> = _poinDitukar
        .map { PromosiUseCase.nilaiPenukaranPoin(it.filter(Char::isDigit).toIntOrNull() ?: 0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** Discount shown on the receipt and saved with the sale. Never above subtotal. */
    val totalDiscount: StateFlow<Long> = combine(subtotal, discountAmount, pointsDiscount) { sub, d, p ->
        PromosiUseCase.gabungkanDiskon(sub, d, p)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    val total: StateFlow<Long> = combine(subtotal, totalDiscount) { sub, diskon -> (sub - diskon).coerceAtLeast(0) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    /** Change to give back for Tunai payments. 0 when tendered is insufficient or not filled. */
    val change: StateFlow<Long> = combine(total, _tendered) { totalValue, tenderText ->
        val paid = tenderText.filter { it.isDigit() }.toLongOrNull() ?: 0L
        (paid - totalValue).coerceAtLeast(0L)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

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
    fun onDiscountChange(value: String) {
        _discount.value = if (_discountMode.value == DiscountMode.PERCENT) {
            value.filter { it.isDigit() }.take(3)
        } else {
            value.filter { it.isDigit() }.take(12)
        }
    }
    fun onDiscountModeChange(mode: DiscountMode) { _discountMode.value = mode; _discount.value = "" }
    fun onPoinDitukarChange(value: String) { _poinDitukar.value = value.filter { it.isDigit() }.take(6) }
    fun onPaymentMethodChange(method: String) { _paymentMethod.value = method.also { if (it != PaymentMethod.TUNAI) _tendered.value = "" } }
    fun onNamaPelangganChange(value: String) { _namaPelanggan.value = value }

    fun addToCart(product: Product) {
        _cart.value = _cart.value + (product.id to ((_cart.value[product.id] ?: 0.0) + 1))
    }

    /** Fills the cart with the items of the most recent completed sale, skipping products that are archived or out of stock. */
    fun repeatLastSale() {
        viewModelScope.launch {
            val last = saleRepository.lastCompletedSale() ?: return@launch _events.send(CashierEvent.Message("NO_LAST_SALE"))
            val (sale, items) = last
            if (sale.status != "SELESAI") return@launch
            // Only keep products that still exist and have enough stock
            val kept = items.mapNotNull { line ->
                val product = productRepository.getById(line.produkId) ?: return@mapNotNull null
                if (product.diarsipkan || product.stok < line.jumlah) return@mapNotNull null
                product to line.jumlah
            }
            if (kept.isEmpty()) {
                _events.send(CashierEvent.Message("REPEAT_EMPTY"))
                return@launch
            }
            _cart.value = kept.associate { (p, qty) -> p.id to qty }
            _events.send(CashierEvent.Message("REPEAT_OK:${kept.size}"))
        }
    }

    fun togglePin(product: Product) {
        viewModelScope.launch { productRepository.setPinned(product.id, !product.isPinned) }
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
        _tendered.value = ""
        _poinDitukar.value = ""
    }

    fun canSaveSale(): Boolean =
        cartEntries.value.isNotEmpty() && !_saving.value &&
            (_paymentMethod.value != PaymentMethod.HUTANG || _namaPelanggan.value.trim().isNotEmpty()) &&
            (_paymentMethod.value != PaymentMethod.TUNAI ||
                RupiahFormatter.parseOrZero(_tendered.value) >= total.value)

    fun saveSale() {
        val entries = cartEntries.value
        if (entries.isEmpty() || _saving.value) return
        if (_paymentMethod.value == PaymentMethod.HUTANG && _namaPelanggan.value.trim().isEmpty()) {
            viewModelScope.launch { _events.send(CashierEvent.Message("CUSTOMER_NAME_REQUIRED")) }
            return
        }
        if (_paymentMethod.value == PaymentMethod.TUNAI && RupiahFormatter.parseOrZero(_tendered.value) < total.value) {
            viewModelScope.launch { _events.send(CashierEvent.Message("INSUFFICIENT_PAYMENT")) }
            return
        }
        val poin = _poinDitukar.value.filter(Char::isDigit).toIntOrNull() ?: 0
        if (poin > 0) {
            if (_namaPelanggan.value.isBlank() || availablePoints.value < poin) {
                viewModelScope.launch { _events.send(CashierEvent.Message("POINTS_INSUFFICIENT")) }
                return
            }
        }
        _saving.value = true
        viewModelScope.launch {
            val metode = _paymentMethod.value
            val namaPelanggan = _namaPelanggan.value.trim().ifEmpty { null }
            val result = saleRepository.recordSale(
                items = entries.map { it.product to it.qty },
                diskon = totalDiscount.value,
                metodeBayar = metode,
                catatan = null,
                namaPelanggan = namaPelanggan,
                poinDitukar = poin,
            )
            _saving.value = false
            result.onSuccess { sale ->
                val receipt = buildReceipt(sale)
                clearCart()
                _namaPelanggan.value = ""
                refreshTodaySales()
                _events.send(CashierEvent.SaleSaved(receipt))
            }.onFailure { error ->
                _events.send(CashierEvent.Message(error.message ?: "ERROR"))
            }
        }
    }

    /** Share the receipt of any sale via WhatsApp (falls back to the system share sheet). */
    fun shareSaleReceipt(sale: Sale) {
        viewModelScope.launch { shareText(buildReceipt(sale)) }
    }

    private suspend fun buildReceipt(sale: Sale): String {
        val profile = profileRepository.getProfile()
        val items = saleRepository.saleItems(sale.id)
        return StrukUseCase.build(
            businessName = profile?.namaUsaha ?: context.getString(id.kalkulasa.R.string.app_name),
            tanggal = DateFormatter.formatDateTime(sale.waktu),
            items = items,
            diskon = sale.diskon,
            total = sale.total,
            metodeBayar = sale.metodeBayar,
        )
    }

    private fun shareText(text: String) {
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
