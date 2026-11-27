package id.kalkulasa.presentation.gudangpro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.StockTransfer
import id.kalkulasa.domain.model.Supplier
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.StockRepository
import id.kalkulasa.domain.repository.SupplierRepositoryPro
import id.kalkulasa.domain.repository.TransferRepository
import id.kalkulasa.domain.usecase.GudangUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface GudangProEvent { data class Message(val text: String) : GudangProEvent }

@HiltViewModel
class GudangProViewModel @Inject constructor(
    private val supplierRepository: SupplierRepositoryPro,
    private val transferRepository: TransferRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
) : ViewModel() {

    val suppliers: StateFlow<List<Supplier>> = supplierRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transfers: StateFlow<List<StockTransfer>> = transferRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _barcode = MutableStateFlow("")
    val barcode: StateFlow<String> = _barcode.asStateFlow()

    private val _events = Channel<GudangProEvent>(Channel.BUFFERED)
    val events: Flow<GudangProEvent> = _events.receiveAsFlow()

    fun addSupplier(nama: String, telepon: String, leadTime: String) {
        if (nama.isBlank()) return
        viewModelScope.launch {
            supplierRepository.save(nama, telepon, leadTime.toIntOrNull() ?: 0)
                .onSuccess { _events.send(GudangProEvent.Message("SAVED")) }
                .onFailure { _events.send(GudangProEvent.Message("ERROR")) }
        }
    }

    fun addTransfer(dari: String, ke: String, productId: String, qtyText: String) {
        val qty = qtyText.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            transferRepository.save(dari, ke, null, listOf(productId to qty))
                .onSuccess { _events.send(GudangProEvent.Message("SAVED")) }
                .onFailure { _events.send(GudangProEvent.Message("ERROR")) }
        }
    }

    fun generateBarcode() {
        viewModelScope.launch {
            val product = productRepository.observeActive("").first().firstOrNull() ?: return@launch
            _barcode.value = GudangUseCase.barcodeInternal(product.id)
        }
    }

    suspend fun expiringCount(): Int {
        val now = System.currentTimeMillis()
        return stockRepository.observeRecentMovements(200).first().count {
            it.catatan?.startsWith("batch:") == true && GudangUseCase.mendekatiKedaluwarsa(
                it.waktu + 7L * 24 * 60 * 60 * 1000, now, 7,
            )
        }
    }
}
