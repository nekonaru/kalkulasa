package id.kalkulasa.presentation.sales

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.Sale
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.repository.SaleRepository
import id.kalkulasa.domain.usecase.StrukUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SalesPeriod { DAYS_7, DAYS_30, DAYS_90 }

sealed interface SalesHistoryEvent {
    data class Message(val text: String) : SalesHistoryEvent
}

data class SalesHistoryUiState(
    val period: SalesPeriod = SalesPeriod.DAYS_30,
    val sales: List<Sale> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val saleRepository: SaleRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SalesHistoryUiState())
    val state: StateFlow<SalesHistoryUiState> = _state.asStateFlow()

    private val _events = Channel<SalesHistoryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init { load() }

    fun selectPeriod(period: SalesPeriod) {
        _state.value = _state.value.copy(period = period, loading = true)
        load()
    }

    fun load() {
        val days = when (_state.value.period) {
            SalesPeriod.DAYS_7 -> 6
            SalesPeriod.DAYS_30 -> 29
            SalesPeriod.DAYS_90 -> 89
        }
        val from = DateFormatter.startOfDayDaysAgo(days)
        viewModelScope.launch {
            try {
                val sales = saleRepository.salesBetween(from, System.currentTimeMillis())
                    .sortedByDescending { it.waktu }
                _state.value = _state.value.copy(sales = sales, loading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false)
            }
        }
    }

    fun cancelSale(saleId: String) {
        viewModelScope.launch {
            saleRepository.cancelSale(saleId)
                .onSuccess {
                    load()
                    _events.send(SalesHistoryEvent.Message("SALE_CANCELLED"))
                }
                .onFailure { _events.send(SalesHistoryEvent.Message("ERROR")) }
        }
    }

    fun shareReceipt(sale: Sale) {
        viewModelScope.launch {
            val profile = profileRepository.getProfile()
            val items = saleRepository.saleItems(sale.id)
            val text = StrukUseCase.build(
                businessName = profile?.namaUsaha ?: context.getString(id.kalkulasa.R.string.app_name),
                tanggal = DateFormatter.formatDateTime(sale.waktu),
                items = items,
                diskon = sale.diskon,
                total = sale.total,
                metodeBayar = sale.metodeBayar,
                labels = id.kalkulasa.domain.usecase.ReceiptLabels(
                    diskon = context.getString(id.kalkulasa.R.string.discount),
                    total = context.getString(id.kalkulasa.R.string.total),
                    bayar = context.getString(id.kalkulasa.R.string.receipt_payment_label),
                    thanks = context.getString(id.kalkulasa.R.string.receipt_thanks),
                    cash = context.getString(id.kalkulasa.R.string.payment_cash),
                    transfer = context.getString(id.kalkulasa.R.string.payment_transfer),
                    qris = context.getString(id.kalkulasa.R.string.payment_qris),
                    hutang = context.getString(id.kalkulasa.R.string.receipt_credit_note),
                ),
                locale = java.util.Locale.getDefault(),
            )
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text)))
            try {
                context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (_: Exception) {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallback, null))
            }
        }
    }
}
