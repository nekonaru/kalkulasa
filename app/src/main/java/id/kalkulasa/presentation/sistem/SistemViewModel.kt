package id.kalkulasa.presentation.sistem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.repository.ChecklistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SistemViewModel @Inject constructor(
    private val repository: ChecklistRepository,
) : ViewModel() {

    val items = repository.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(id: String, selesai: Boolean) {
        viewModelScope.launch { repository.toggle(id, selesai) }
    }

    fun add(nama: String, fase: String) {
        if (nama.isBlank()) return
        viewModelScope.launch { repository.add(nama, fase) }
    }
}
