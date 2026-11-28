package id.kalkulasa.presentation.keuanganlanjutan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.usecase.ArusKasUseCase
import id.kalkulasa.domain.usecase.PiutangAgingUseCase
import id.kalkulasa.presentation.reports.ReportPeriod
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class KeuanganLanjutanViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val payableRepository: PayableRepository,
) : ViewModel() {

    private val _period = MutableStateFlow(ReportPeriod.THIS_MONTH)
    val period: StateFlow<ReportPeriod> = _period.asStateFlow()

    private val _cashFlow = MutableStateFlow<ArusKasUseCase.Ringkasan?>(null)
    val cashFlow: StateFlow<ArusKasUseCase.Ringkasan?> = _cashFlow.asStateFlow()

    private val _aging = MutableStateFlow<Map<PiutangAgingUseCase.Bucket, Long>>(emptyMap())
    val aging: StateFlow<Map<PiutangAgingUseCase.Bucket, Long>> = _aging.asStateFlow()

    fun selectPeriod(period: ReportPeriod) {
        _period.value = period
        load()
    }

    fun load() {
        val p = _period.value
        val now = System.currentTimeMillis()
        val (from, to) = when (p) {
            ReportPeriod.TODAY -> DateFormatter.startOfDay(now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_7 -> DateFormatter.startOfDayDaysAgo(6, now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_30 -> DateFormatter.startOfDayDaysAgo(29, now) to DateFormatter.endOfDay(now)
            ReportPeriod.THIS_MONTH -> DateFormatter.startOfMonth(now) to DateFormatter.endOfDay(now)
        }
        viewModelScope.launch {
            val flow = async { financeRepository.cashFlowBetween(from, to) }
            val agingFlow = async {
                val payables = payableRepository.observeAll().first()
                PiutangAgingUseCase.totals(
                    payables.filter { !it.isLunas }.map { Triple(it.sisa, it.jatuhTempo, now) },
                )
            }
            _cashFlow.value = flow.await()
            _aging.value = agingFlow.await()
        }
    }
}
