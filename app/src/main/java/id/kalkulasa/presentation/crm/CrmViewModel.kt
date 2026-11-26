package id.kalkulasa.presentation.crm

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import id.kalkulasa.domain.model.Customer
import id.kalkulasa.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CrmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val customerRepository: CustomerRepository,
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = customerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun updateNotes(id: String, catatan: String, tag: String) {
        viewModelScope.launch { customerRepository.updateNotes(id, catatan, tag) }
    }

    fun shareMemberCard(customer: Customer) {
        val text = "Kartu member ${customer.nama} · Poin ${customer.poin} · Kunjungan ${customer.kunjungan}"
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
