package id.kalkulasa.presentation.gudanglanjutan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.StockRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExpiryRow(val nama: String, val sisaHari: Int, val jumlah: Double)
data class PriceRow(val tanggal: Long, val harga: Long)

@HiltViewModel
class GudangLanjutanViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _expiry = MutableStateFlow<List<ExpiryRow>>(emptyList())
    val expiry: StateFlow<List<ExpiryRow>> = _expiry.asStateFlow()

    private val _history = MutableStateFlow<List<PriceRow>>(emptyList())
    val history: StateFlow<List<PriceRow>> = _history.asStateFlow()

    fun loadExpiry() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            _expiry.value = stockRepository.expiring().map {
                ExpiryRow(it.first, ((it.second - now) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0), it.third)
            }
        }
    }

    fun loadHistory(productId: String) {
        viewModelScope.launch {
            _history.value = stockRepository.priceHistory(productId).map { PriceRow(it.first, it.second) }
        }
    }

    suspend fun products() = productRepository.observeActive("").first()
}
