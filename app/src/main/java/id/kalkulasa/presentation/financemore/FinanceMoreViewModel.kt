package id.kalkulasa.presentation.financemore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.core.util.RupiahFormatter
import id.kalkulasa.domain.model.CashAudit
import id.kalkulasa.domain.repository.BudgetRepository
import id.kalkulasa.domain.repository.CashAuditRepository
import id.kalkulasa.domain.repository.FinanceRepository
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.usecase.KeuanganLanjutanUseCase
import id.kalkulasa.domain.usecase.PiutangAgingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetRow(val kategori: String, val anggaran: Long, val terpakai: Long, val persen: Double)

@HiltViewModel
class FinanceMoreViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val cashAuditRepository: CashAuditRepository,
    private val financeRepository: FinanceRepository,
    private val payableRepository: PayableRepository,
) : ViewModel() {

    val audits: StateFlow<List<CashAudit>> = cashAuditRepository.observeRecent(100)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _budgetInput = MutableStateFlow("")
    val budgetInput: StateFlow<String> = _budgetInput.asStateFlow()

    private val _events = Channel<String>(Channel.BUFFERED)
    val events: Flow<String> = _events.receiveAsFlow()

    fun onBudgetInput(value: String) { _budgetInput.value = value.filter(Char::isDigit) }

    fun saveBudget(kategori: String) {
        val nominal = RupiahFormatter.parseOrZero(_budgetInput.value)
        if (kategori.isBlank() || nominal <= 0) return
        val bulan = DateFormatter.fileStamp(System.currentTimeMillis()).take(6)
        viewModelScope.launch {
            budgetRepository.save(kategori, nominal, bulan).onSuccess { _events.send("SAVED") }
        }
    }

    fun recordAudit(fisikText: String, catatan: String) {
        val fisik = RupiahFormatter.parseOrZero(fisikText)
        if (fisik <= 0) return
        viewModelScope.launch {
            val sistem = financeRepository.cashBalance()
            cashAuditRepository.record(sistem, fisik, catatan).onSuccess { _events.send("SAVED") }
        }
    }

    suspend fun budgetRows(): List<BudgetRow> {
        val bulan = DateFormatter.fileStamp(System.currentTimeMillis()).take(6)
        val budgets = budgetRepository.observeForMonth(bulan).first()
        val spent = financeRepository.expensesByCategoryBetween(
            DateFormatter.startOfMonth(), DateFormatter.endOfDay(),
        ).associateBy { it.nama }
        return budgets.map { b ->
            val used = spent[b.kategoriNama]?.total ?: 0L
            BudgetRow(
                b.kategoriNama, b.nominal, used,
                KeuanganLanjutanUseCase.persentaseBudget(used, b.nominal),
            )
        }
    }

    suspend fun payableAging(): Map<PiutangAgingUseCase.Bucket, Long> {
        val payables = payableRepository.observeAll().first()
        val now = System.currentTimeMillis()
        return PiutangAgingUseCase.totals(
            payables.filter { !it.isLunas }.map {
                Triple(it.sisa, it.jatuhTempo, now)
            },
        )
    }
}
