package id.kalkulasa.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.kalkulasa.domain.model.Category
import id.kalkulasa.domain.model.CategoryType
import id.kalkulasa.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    val categories: StateFlow<List<Category>> = categoryRepository.observeProductCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        if (name.trim().isEmpty()) return
        viewModelScope.launch { categoryRepository.addCategory(name.trim(), CategoryType.PRODUCT) }
    }

    fun rename(id: String, name: String) {
        if (name.trim().isEmpty()) return
        viewModelScope.launch { categoryRepository.rename(id, name) }
    }

    fun delete(id: String) {
        viewModelScope.launch { categoryRepository.delete(id) }
    }
}
