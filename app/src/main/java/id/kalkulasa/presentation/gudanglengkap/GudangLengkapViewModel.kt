package id.kalkulasa.presentation.gudanglengkap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.SupplierRepositoryPro
import id.kalkulasa.domain.repository.TransferRepository
import id.kalkulasa.domain.usecase.GudangUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SaranRow(val nama: String, val saran: Double, val stok: Double)

@HiltViewModel
class GudangLengkapViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val supplierRepository: SupplierRepositoryPro,
    private val transferRepository: TransferRepository,
) : ViewModel() {

    val products: StateFlow<List<Product>> = productRepository.observeActive("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val suppliers = supplierRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saran = MutableStateFlow<List<SaranRow>>(emptyList())
    val saran: StateFlow<List<SaranRow>> = _saran.asStateFlow()

    fun hitungSaran(penjualanHarian: String) {
        val harian = penjualanHarian.replace(',', '.').toDoubleOrNull() ?: return
        viewModelScope.launch {
            val lead = suppliers.value.firstOrNull()?.leadTimeDays ?: 7
            _saran.value = products.value.filter { it.isLowStock }.map {
                SaranRow(
                    it.nama,
                    GudangUseCase.saranRestok(harian, lead, it.stok, it.stokMinimum),
                    it.stok,
                )
            }
        }
    }

    fun transfer(dari: String, ke: String, items: List<Pair<String, Double>>) {
        if (dari.isBlank() || ke.isBlank() || items.isEmpty()) return
        viewModelScope.launch { transferRepository.save(dari, ke, null, items) }
    }
}
