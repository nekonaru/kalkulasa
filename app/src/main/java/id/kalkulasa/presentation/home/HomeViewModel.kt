package id.kalkulasa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.DailyPoint
import id.kalkulasa.domain.model.ProductSales
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val namaUsaha: String = "",
    val omzetToday: Long = 0,
    val netProfitToday: Long = 0,
    val transactionCount: Int = 0,
    val lowStockCount: Int = 0,
    val trend: List<DailyPoint> = emptyList(),
    val bestSellers: List<ProductSales> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val saleRepository: SaleRepository,
    private val financeRepository: FinanceRepository,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            val from = DateFormatter.startOfDay()
            val to = DateFormatter.endOfDay()
            val omzet = saleRepository.revenueBetween(from, to)
            val cogs = saleRepository.cogsBetween(from, to)
            val expenses = financeRepository.expensesBetween(from, to)
            val count = saleRepository.countBetween(from, to)
            val lowStock = productRepository.observeLowStockCount().first()
            val trend = saleRepository.dailyTrend(7)
            val weekAgo = DateFormatter.startOfDayDaysAgo(6)
            val best = saleRepository.topProductsBetween(weekAgo, to, 3)
            _state.value = HomeUiState(
                namaUsaha = profile?.namaUsaha ?: "",
                omzetToday = omzet,
                netProfitToday = omzet - cogs - expenses,
                transactionCount = count,
                lowStockCount = lowStock,
                trend = trend,
                bestSellers = best,
                loading = false,
            )
        }
    }
}
