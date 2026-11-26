package id.kalkulasa.presentation.datatools

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.repository.PayableRepository
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.SaleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DataToolsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val customerRepository: CustomerRepository,
    private val payableRepository: PayableRepository,
    private val saleRepository: SaleRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    fun onQueryChange(value: String) { _query.value = value }

    fun exportProducts() {
        viewModelScope.launch {
            val rows = productRepository.observeAll("").first()
            val csv = buildString {
                appendLine("nama,satuan,harga_beli,harga_jual,stok,stok_minimum,barcode,lokasi")
                rows.forEach { p ->
                    appendLine(listOf(p.nama, p.satuan, p.hargaBeliRata2, p.hargaJual, p.stok, p.stokMinimum, p.barcode ?: "", p.lokasi ?: "").joinToString(",") { csvCell(it.toString()) })
                }
            }
            shareCsv("kalkulasa-produk.csv", csv)
        }
    }

    fun exportCustomers() {
        viewModelScope.launch {
            val rows = customerRepository.observeAll().first()
            val csv = buildString {
                appendLine("nama,poin,kunjungan,total_belanja,tag,catatan")
                rows.forEach { c ->
                    appendLine(listOf(c.nama, c.poin, c.kunjungan, c.totalBelanja, c.tag ?: "", c.catatan ?: "").joinToString(",") { csvCell(it.toString()) })
                }
            }
            shareCsv("kalkulasa-pelanggan.csv", csv)
        }
    }

    fun exportPayables() {
        viewModelScope.launch {
            val rows = payableRepository.observeAll().first()
            val csv = buildString {
                appendLine("pemasok,nominal,terbayar,sisa,jatuh_tempo")
                rows.forEach { p ->
                    appendLine(listOf(p.pemasok, p.nominal, p.terbayar, p.sisa, p.jatuhTempo ?: "").joinToString(",") { csvCell(it.toString()) })
                }
            }
            shareCsv("kalkulasa-hutang.csv", csv)
        }
    }

    suspend fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults(emptyList(), emptyList())
        val q = query.trim()
        val products = productRepository.observeAll("").first().filter {
            it.nama.contains(q, true) || (it.barcode?.contains(q, true) == true)
        }.take(20)
        val sales = saleRepository.observeRecentSales(500).first().filter { sale ->
            sale.id.contains(q, true) || sale.metodeBayar.contains(q, true)
        }.take(20)
        return SearchResults(products.map { it.nama }, sales.map { "TRX ${it.id.take(8)} · ${it.total}" })
    }

    private fun csvCell(value: String): String = "\"${value.replace("\"", "\"\"")}\""

    private fun shareCsv(name: String, csv: String) {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, name)
        file.writeBytes(csv.toByteArray(Charsets.UTF_8))
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }
}

data class SearchResults(val products: List<String>, val sales: List<String>)
