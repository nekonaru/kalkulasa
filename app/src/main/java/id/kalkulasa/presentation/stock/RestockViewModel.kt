package id.kalkulasa.presentation.stock

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.domain.model.Product
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.core.util.RupiahFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RestockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _refresh = MutableStateFlow(0)

    val lowStock: StateFlow<List<Product>> = _refresh
        .flatMapLatest { productRepository.observeLowStock() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refresh() { _refresh.value = _refresh.value + 1 }

    fun share(context: Context) {
        val products = lowStock.value
        if (products.isEmpty()) return
        viewModelScope.launch {
            val usaha = profileRepository.getProfile()?.namaUsaha
                ?: context.getString(id.kalkulasa.R.string.business_name_fallback)
            val message = buildString {
                appendLine(context.getString(id.kalkulasa.R.string.restock_share_title, usaha))
                products.forEach { p ->
                    val suggested = ((p.stokMinimum * 2.0) - p.stok).coerceAtLeast(1.0)
                    appendLine("- ${p.nama}: +${trim(suggested)} ${p.satuan} @ ${RupiahFormatter.format(p.hargaBeliRata2)}")
                }
                appendLine()
                append(context.getString(id.kalkulasa.R.string.restock_sent_from))
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(message)))
            try {
                context.startActivity(intent.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (_: Exception) {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(Intent.createChooser(fallback, null))
            }
        }
    }

    private fun trim(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format(java.util.Locale.forLanguageTag("id-ID"), "%.2f", value)
}
