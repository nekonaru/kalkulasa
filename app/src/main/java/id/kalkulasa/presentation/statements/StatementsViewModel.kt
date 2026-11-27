package id.kalkulasa.presentation.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.CashBreakdown
import id.kalkulasa.domain.model.CategorySpend
import id.kalkulasa.domain.model.Neraca
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ReceivableRepository
import id.kalkulasa.domain.repository.ReportRepository
import id.kalkulasa.domain.usecase.LaporanKeuanganUseCase
import id.kalkulasa.presentation.reports.ReportPeriod
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatementsUiState(
    val period: ReportPeriod = ReportPeriod.THIS_MONTH,
    val neraca: Neraca? = null,
    val breakdown: CashBreakdown? = null,
    val categories: List<CategorySpend> = emptyList(),
    val modalInfo: Long = 0L,
    val labaDitahanInfo: Long = 0L,
    val loading: Boolean = true,
    val error: Boolean = false,
)

@HiltViewModel
class StatementsViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val productRepository: ProductRepository,
    private val receivableRepository: ReceivableRepository,
    private val payableRepository: PayableRepository,
    private val reportRepository: ReportRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(StatementsUiState())
    val state: StateFlow<StatementsUiState> = _state.asStateFlow()

    fun selectPeriod(period: ReportPeriod) {
        _state.value = _state.value.copy(period = period, loading = true)
        load()
    }

    fun load() {
        val period = _state.value.period
        val now = System.currentTimeMillis()
        val (from, to) = when (period) {
            ReportPeriod.TODAY -> DateFormatter.startOfDay(now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_7 -> DateFormatter.startOfDayDaysAgo(6, now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_30 -> DateFormatter.startOfDayDaysAgo(29, now) to DateFormatter.endOfDay(now)
            ReportPeriod.THIS_MONTH -> DateFormatter.startOfMonth(now) to DateFormatter.endOfDay(now)
        }
        viewModelScope.launch {
            try {
            val breakdown = async { financeRepository.cashBreakdown() }
            val piutang = async { receivableRepository.observeOutstanding().first() }
            val hutang = async { payableRepository.observeOutstanding().first() }
            val kasbon = async { financeRepository.unsettledKasbon() }
            val cumulative = async { reportRepository.reportBetween(0L, Long.MAX_VALUE) }
            val categories = async { financeRepository.expensesByCategoryBetween(from, to) }

            val cash = breakdown.await()
            // Modal is derived from the same breakdown; netCapital() would repeat two queries.
            val modal = cash.modal - cash.prive
            val report = cumulative.await()
            val retained = LaporanKeuanganUseCase.labaDitahan(
                report.omzet, report.hpp, report.pengeluaran,
            )
            val neraca = LaporanKeuanganUseCase.neraca(
                kas = cash.saldo,
                piutang = piutang.await(),
                persediaan = report.nilaiPersediaan,
                kasbon = kasbon.await(),
                hutangUsaha = hutang.await(),
                modalBersih = modal,
                labaDitahan = retained,
            )
            if (_state.value.period == period) {
                _state.value = StatementsUiState(
                    period = period,
                    neraca = neraca,
                    breakdown = cash,
                    categories = categories.await(),
                    modalInfo = modal,
                    labaDitahanInfo = retained,
                    loading = false,
                )
            }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = true)
            }
        }
    }
}
