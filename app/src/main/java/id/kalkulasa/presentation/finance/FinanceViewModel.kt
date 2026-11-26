package id.kalkulasa.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.CapitalEntry
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.CategoryType
import id.kalkulasa.domain.model.Expense
import id.kalkulasa.domain.model.PurchaseRecord
import id.kalkulasa.domain.repository.CategoryRepository
import id.kalkulasa.domain.repository.FinanceRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FinanceItem(val waktu: Long) {
    data class Capital(val entry: CapitalEntry) : FinanceItem(entry.waktu)
    data class ExpenseItem(val expense: Expense) : FinanceItem(expense.waktu)
    data class Purchase(val purchase: PurchaseRecord) : FinanceItem(purchase.waktu)
}

sealed interface FinanceEvent {
    /** Text is SAVED marker or error message. */
    data class Message(val text: String) : FinanceEvent
}

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val financeRepository: FinanceRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _cashBalance = MutableStateFlow(0L)
    val cashBalance: StateFlow<Long> = _cashBalance.asStateFlow()

    val expenseCategories: StateFlow<List<Category>> =
        categoryRepository.observeByType(CategoryType.EXPENSE)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val items: StateFlow<List<FinanceItem>> = combine(
        financeRepository.observeRecentCapitalEntries(100),
        financeRepository.observeRecentExpenses(100),
        financeRepository.observeRecentPurchases(100),
    ) { capital, expenses, purchases ->
        (capital.map { FinanceItem.Capital(it) } +
            expenses.map { FinanceItem.ExpenseItem(it) } +
            purchases.map { FinanceItem.Purchase(it) })
            .sortedByDescending { it.waktu }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _events = Channel<FinanceEvent>(Channel.BUFFERED)
    val events: Flow<FinanceEvent> = _events.receiveAsFlow()

    init { refreshBalance() }

    fun refreshBalance() {
        viewModelScope.launch { _cashBalance.value = financeRepository.cashBalance() }
    }

    fun addCapital(nominalText: String, catatan: String, tipe: String) {
        val nominal = nominalText.filter { it.isDigit() }.toLongOrNull() ?: return
        if (nominal <= 0) return
        viewModelScope.launch {
            financeRepository.addCapital(tipe, nominal, catatan)
            refreshBalance()
            _events.send(FinanceEvent.Message("SAVED"))
        }
    }

    fun addExpense(kategoriId: String?, kategoriNama: String, nominalText: String, catatan: String) {
        val nominal = nominalText.filter { it.isDigit() }.toLongOrNull() ?: return
        if (nominal <= 0) return
        viewModelScope.launch {
            financeRepository.addExpense(kategoriId, kategoriNama, nominal, catatan)
                .onSuccess {
                    refreshBalance()
                    _events.send(FinanceEvent.Message("SAVED"))
                }
                .onFailure { _events.send(FinanceEvent.Message("ERROR")) }
        }
    }
}
