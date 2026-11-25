package id.kalkulasa.presentation.reports

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.core.util.DateFormatter
import id.kalkulasa.domain.model.PeriodReport
import id.kalkulasa.domain.model.ProductProfit
import id.kalkulasa.domain.repository.ReportRepository
import id.kalkulasa.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

enum class ReportPeriod { TODAY, DAYS_7, DAYS_30, THIS_MONTH }

data class ReportsUiState(
    val period: ReportPeriod = ReportPeriod.DAYS_7,
    val from: Long = DateFormatter.startOfDayDaysAgo(6),
    val to: Long = DateFormatter.endOfDay(),
    val report: PeriodReport = PeriodReport.empty(),
    val loading: Boolean = true,
    val exporting: Boolean = false,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reportRepository: ReportRepository,
    private val saleRepository: SaleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    private val _mostProfitable = MutableStateFlow<List<ProductProfit>>(emptyList())
    val mostProfitable: StateFlow<List<ProductProfit>> = _mostProfitable.asStateFlow()

    init { load() }

    fun selectPeriod(period: ReportPeriod) {
        val now = System.currentTimeMillis()
        val (from, to) = when (period) {
            ReportPeriod.TODAY -> DateFormatter.startOfDay(now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_7 -> DateFormatter.startOfDayDaysAgo(6, now) to DateFormatter.endOfDay(now)
            ReportPeriod.DAYS_30 -> DateFormatter.startOfDayDaysAgo(29, now) to DateFormatter.endOfDay(now)
            ReportPeriod.THIS_MONTH -> DateFormatter.startOfMonth(now) to DateFormatter.endOfDay(now)
        }
        _state.value = _state.value.copy(period = period, from = from, to = to, loading = true)
        load()
    }

    fun load() {
        val s = _state.value
        viewModelScope.launch {
            val report = reportRepository.reportBetween(s.from, s.to)
            val profitable = saleRepository.mostProfitableBetween(s.from, s.to, 5)
            // A fast period switch can start two queries. Only the one that still
            // matches the visible period is allowed to write state.
            withContext(Dispatchers.Main) {
                val current = _state.value
                if (current.from == s.from && current.to == s.to && current.period == s.period) {
                    _mostProfitable.value = profitable
                    _state.value = current.copy(report = report, loading = false)
                }
            }
        }
    }

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    fun clearError() { _error.value = null }

    fun exportCsv() {
        shareFile("kalkulasa-laporan.csv", "text/csv") {
            reportRepository.exportCsv(_state.value.from, _state.value.to).toByteArray()
        }
    }

    fun exportPdf() {
        shareFile("kalkulasa-laporan.pdf", "application/pdf") {
            reportRepository.exportPdfBytes(_state.value.from, _state.value.to)
        }
    }

    fun shareWhatsapp() {
        viewModelScope.launch {
            val text = reportRepository.shareText(_state.value.from, _state.value.to)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(Intent.createChooser(intent, null).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (_: Exception) {
                // WhatsApp not installed; fall back to generic chooser
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(fallback, null).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            }
        }
    }

    private fun shareFile(name: String, mime: String, produce: suspend () -> ByteArray) {
        if (_state.value.exporting) return
        _state.value = _state.value.copy(exporting = true)
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val bytes = produce()
                val dir = File(context.cacheDir, "exports").apply { mkdirs() }
                val file = File(dir, name)
                file.writeBytes(bytes)
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = mime
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    context.startActivity(Intent.createChooser(intent, null).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                }
            } catch (e: Exception) {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _error.value = context.getString(id.kalkulasa.R.string.error_generic)
                }
            } finally {
                withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _state.value = _state.value.copy(exporting = false)
                }
            }
        }
    }
}
