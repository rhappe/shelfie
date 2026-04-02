package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CategoryViewState {
    data object Loading : CategoryViewState
    data class Error(val message: String) : CategoryViewState
    data class Content(val categories: List<Category>) : CategoryViewState
}

private data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class CategoryViewModel(
    private val repository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())

    val viewState: StateFlow<CategoryViewState> = _uiState.map { state ->
        when {
            state.isLoading -> CategoryViewState.Loading
            state.error != null -> CategoryViewState.Error(state.error)
            else -> CategoryViewState.Content(categories = state.categories)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryViewState.Loading)

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val categories = repository.getCategories()
                _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun createCategory(name: String, description: String?, color: String?) {
        viewModelScope.launch {
            try {
                repository.createCategory(CreateCategoryRequest(name = name, description = description, color = color))
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun updateCategory(id: String, name: String, description: String?, color: String?) {
        viewModelScope.launch {
            try {
                repository.updateCategory(id, UpdateCategoryRequest(name = name, description = description, color = color))
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(id)
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
