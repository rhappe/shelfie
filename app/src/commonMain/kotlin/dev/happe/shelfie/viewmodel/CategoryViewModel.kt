package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class CategoryViewModel(
    private val repository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

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
