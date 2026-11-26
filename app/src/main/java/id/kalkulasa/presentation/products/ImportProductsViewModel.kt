package id.kalkulasa.presentation.products

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.domain.repository.ProductRepository
import id.kalkulasa.domain.usecase.ImportProdukUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class ImportProductsUiState(
    val fileName: String? = null,
    val products: List<ImportProdukUseCase.ImportedProduct> = emptyList(),
    val errors: List<ImportProdukUseCase.RowError> = emptyList(),
    val importedCount: Int? = null,
    val importing: Boolean = false,
)

sealed interface ImportProductsEvent {
    data class Message(val text: String) : ImportProductsEvent
}

@HiltViewModel
class ImportProductsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ImportProductsUiState())
    val state: StateFlow<ImportProductsUiState> = _state.asStateFlow()

    private val _events = Channel<ImportProductsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onFilePicked(uri: Uri) {
        viewModelScope.launch {
            val size = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            if (size > MAX_CSV_BYTES) {
                _events.send(ImportProductsEvent.Message("TOO_BIG"))
                return@launch
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()
                        ?.use { it.readText() } ?: return@runCatching null
                    ImportProdukUseCase.parse(text)
                }
            }
            val parsed = result.getOrNull()
            if (parsed == null) {
                _events.send(ImportProductsEvent.Message("ERROR"))
                return@launch
            }
            val name = uri.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':')
            _state.value = ImportProductsUiState(
                fileName = name,
                products = parsed.products,
                errors = parsed.errors,
            )
        }
    }

    fun runImport() {
        val products = _state.value.products
        if (products.isEmpty() || _state.value.importing) return
        _state.value = _state.value.copy(importing = true)
        viewModelScope.launch {
            try {
                val count = productRepository.importProducts(products)
                _state.value = _state.value.copy(importedCount = count, importing = false, products = emptyList(), errors = emptyList())
                _events.send(ImportProductsEvent.Message("DONE:$count"))
            } catch (e: Exception) {
                _state.value = _state.value.copy(importing = false)
                _events.send(ImportProductsEvent.Message("ERROR"))
            }
        }
    }

    fun shareTemplate() {
        val template = ImportProdukUseCase.templateCsv()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, template)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null))
    }

    companion object {
        private const val MAX_CSV_BYTES = 5L * 1024 * 1024
    }
}
