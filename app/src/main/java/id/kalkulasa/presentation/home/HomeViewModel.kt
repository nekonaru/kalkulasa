package id.kalkulasa.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.datastore.SettingsDataStore
import id.kalkulasa.domain.model.DailyPoint
import id.kalkulasa.domain.model.ProductSales
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.ReceivableRepository
import id.kalkulasa.domain.repository.ReportRepository
import id.kalkulasa.domain.repository.SaleRepository
import id.kalkulasa.domain.repository.SalesTargetRepository
import id.kalkulasa.domain.usecase.KesehatanUsahaUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
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
    val piutangOutstanding: Long = 0L,
    val targetHarian: Long = 0L,
    val targetBulanan: Long = 0L,
    val omzetMonth: Long = 0L,
    val health: KesehatanUsahaUseCase.Result? = null,
    val loading: Boolean = true,
    val error: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val saleRepository: SaleRepository,
    private val financeRepository: FinanceRepository,
    private val productRepository: ProductRepository,
    private val receivableRepository: ReceivableRepository,
    private val reportRepository: ReportRepository,
    private val salesTargetRepository: SalesTargetRepository,
    private val settings: SettingsDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    val warungMode: StateFlow<Boolean> = settings.warungMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun refresh() {
        viewModelScope.launch {
            try {
                val from = DateFormatter.startOfDay()
            val to = DateFormatter.endOfDay()
            val monthStart = DateFormatter.startOfMonth(System.currentTimeMillis())
            val weekAgo = DateFormatter.startOfDayDaysAgo(6)

            // Run independent reads concurrently to keep the home screen snappy
            val profile = async { profileRepository.getProfile() }
            val omzet = async { saleRepository.revenueBetween(from, to) }
            val cogs = async { saleRepository.cogsBetween(from, to) }
            val expenses = async { financeRepository.expensesBetween(from, to) }
            val count = async { saleRepository.countBetween(from, to) }
            val lowStock = async { productRepository.observeLowStockCount().first() }
            val trend = async { saleRepository.dailyTrend(7) }
            val best = async { saleRepository.topProductsBetween(weekAgo, to, 3) }
            val piutang = async { receivableRepository.observeOutstanding().first() }
            val monthReport = async { reportRepository.reportBetween(monthStart, to) }
            val saldoKas = async { financeRepository.cashBalance() }
            val targets = async { salesTargetRepository.observeAll().first() }

            val profileValue = profile.await()
            val omzetValue = omzet.await()
            val cogsValue = cogs.await()
            val expensesValue = expenses.await()
            val lowStockValue = lowStock.await()
            val piutangValue = piutang.await()
            val reportValue = monthReport.await()
            val health = KesehatanUsahaUseCase.evaluate(
                KesehatanUsahaUseCase.Input(
                    marginPersen = reportValue.marginPersen,
                    labaBersih = reportValue.labaBersih,
                    omzet = reportValue.omzet,
                    nilaiPersediaan = reportValue.nilaiPersediaan,
                    piutangOutstanding = piutangValue,
                    saldoKas = saldoKas.await(),
                ),
            )
            val targetList = targets.await()
            _state.value = HomeUiState(
                namaUsaha = profileValue?.namaUsaha ?: "",
                omzetToday = omzetValue,
                netProfitToday = omzetValue - cogsValue - expensesValue,
                transactionCount = count.await(),
                lowStockCount = lowStockValue,
                trend = trend.await(),
                bestSellers = best.await(),
                piutangOutstanding = piutangValue,
                targetHarian = targetList.find { it.jenis == "HARIAN" }?.nominal ?: 0L,
                targetBulanan = targetList.find { it.jenis == "BULANAN" }?.nominal ?: 0L,
                omzetMonth = reportValue.omzet,
                health = health,
                loading = false,
            )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = true)
            }
        }
    }
}
