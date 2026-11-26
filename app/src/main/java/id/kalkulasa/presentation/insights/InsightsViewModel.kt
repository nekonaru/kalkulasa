package id.kalkulasa.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.domain.model.ProductProfit
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class InsightsUiState(
    val averageSale: Long = 0L,
    val bestHour: Int? = null,
    val bestDay: Int? = null,
    val topCustomer: Customer? = null,
    val topProduct: ProductProfit? = null,
    val hasData: Boolean = false,
    val loading: Boolean = true,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val to = DateFormatter.endOfDay()
                val from = DateFormatter.startOfMonth()
                val sales = saleRepository.salesBetween(from, to).filter { it.status == "SELESAI" }
                val calendar = Calendar.getInstance()
                val byHour = mutableMapOf<Int, Long>()
                val byDay = mutableMapOf<Int, Long>()
                sales.forEach { sale ->
                    calendar.timeInMillis = sale.waktu
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val day = calendar.get(Calendar.DAY_OF_WEEK)
                    byHour[hour] = (byHour[hour] ?: 0L) + sale.total
                    byDay[day] = (byDay[day] ?: 0L) + sale.total
                }
                val customers = customerRepository.observeAll().first()
                val topCustomerId = sales.mapNotNull { it.customerId }
                    .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
                val customer = customers.find { it.id == topCustomerId }
                val product = saleRepository.mostProfitableBetween(from, to, 1).firstOrNull()
                _state.value = InsightsUiState(
                    averageSale = if (sales.isEmpty()) 0L else sales.sumOf { it.total } / sales.size.toLong(),
                    bestHour = byHour.maxByOrNull { it.value }?.key,
                    bestDay = byDay.maxByOrNull { it.value }?.key,
                    topCustomer = customer,
                    topProduct = product,
                    hasData = sales.isNotEmpty(),
                    loading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }
}
