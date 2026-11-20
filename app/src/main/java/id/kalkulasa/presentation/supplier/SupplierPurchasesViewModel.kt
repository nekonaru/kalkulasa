package id.kalkulasa.presentation.supplier

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.PurchaseRecord
import id.kalkulasa.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierPurchasesViewModel @Inject constructor(
    private val repository: SupplierRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val supplier: String = java.net.URLDecoder.decode(savedStateHandle.get<String>("supplier").orEmpty(), "UTF-8")

    private val _purchases = MutableStateFlow<List<PurchaseRecord>>(emptyList())
    val purchases: StateFlow<List<PurchaseRecord>> = _purchases.asStateFlow()

    fun load(days: Int) {
        val from = id.kalkulasa.core.util.DateFormatter.startOfDayDaysAgo(days)
        val to = System.currentTimeMillis()
        viewModelScope.launch {
            _purchases.value = repository.purchasesFromSupplier(supplier, from, to)
        }
    }
}
