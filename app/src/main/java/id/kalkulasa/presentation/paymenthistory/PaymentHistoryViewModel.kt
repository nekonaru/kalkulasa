package id.kalkulasa.presentation.paymenthistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.PayablePayment
import id.kalkulasa.domain.repository.PayablePaymentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    repository: PayablePaymentRepository,
) : ViewModel() {

    private val paymentsFlow = repository.observeRecent(200)

    val payments: StateFlow<List<PayablePayment>> = paymentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthTotal: StateFlow<Long> = paymentsFlow
        .map { list ->
            val from = DateFormatter.startOfMonth()
            list.filter { it.waktu >= from }.sumOf { it.nominal }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)
}
