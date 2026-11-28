package id.kalkulasa.presentation.broadcast

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.domain.repository.CustomerRepository
import id.kalkulasa.domain.repository.ProfileRepository
import id.kalkulasa.domain.usecase.MemberUseCase
import id.kalkulasa.domain.usecase.SegmentasiUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BroadcastViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customerRepository: CustomerRepository,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _all = MutableStateFlow<List<Customer>>(emptyList())
    val all: StateFlow<List<Customer>> = _all.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    fun load() {
        viewModelScope.launch { _all.value = customerRepository.observeAll().first() }
    }

    fun toggle(id: String) {
        _selected.value = if (id in _selected.value) _selected.value - id else _selected.value + id
    }

    fun filtered(tag: String, minimal: Long): List<Customer> = _all.value.filter {
        SegmentasiUseCase.cocok(it.tag, it.totalBelanja, tag, minimal)
    }

    fun memberCode(customer: Customer): String = "KLK-MEMBER-" + customer.id.take(8).uppercase()

    fun tier(customer: Customer): String = MemberUseCase.tier(customer.totalBelanja).name

    fun broadcast(pesan: String) {
        if (pesan.isBlank()) return
        viewModelScope.launch {
            val usaha = profileRepository.getProfile()?.namaUsaha ?: "Kalkulasa"
            val daftar = _all.value.filter { it.id in _selected.value }
            val text = daftar.joinToString("\n\n") {
                SegmentasiUseCase.pesanBroadcast(usaha, it.nama, pesan)
            }
            if (text.isBlank()) return@launch
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(text)))
            try {
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
