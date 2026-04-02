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
    data class Content(
        val categories: List<Category>,
        val showDialog: Boolean,
        val editingCategory: Category?,
        val deletingCategoryId: String?,
    ) : CategoryViewState
}

sealed interface CategoryViewEvent {
    data object AddCategory : CategoryViewEvent
    data class EditCategory(val category: Category) : CategoryViewEvent
    data object DismissDialog : CategoryViewEvent
    data class SaveCategory(val name: String, val description: String?, val color: String?) : CategoryViewEvent
    data class RequestDeleteCategory(val categoryId: String) : CategoryViewEvent
    data object ConfirmDeleteCategory : CategoryViewEvent
    data object DismissDeleteConfirm : CategoryViewEvent
}

private data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDialog: Boolean = false,
    val editingCategory: Category? = null,
    val deletingCategoryId: String? = null,
)

class CategoryViewModel(
    private val repository: CategoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())

    val viewState: StateFlow<CategoryViewState> = _uiState.map { state ->
        when {
            state.isLoading -> CategoryViewState.Loading
            state.error != null -> CategoryViewState.Error(state.error)
            else -> CategoryViewState.Content(
                categories = state.categories,
                showDialog = state.showDialog,
                editingCategory = state.editingCategory,
                deletingCategoryId = state.deletingCategoryId,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CategoryViewState.Loading)

    init {
        loadCategories()
    }

    fun handleEvent(event: CategoryViewEvent) {
        when (event) {
            is CategoryViewEvent.AddCategory -> {
                _uiState.value = _uiState.value.copy(showDialog = true, editingCategory = null)
            }
            is CategoryViewEvent.EditCategory -> {
                _uiState.value = _uiState.value.copy(showDialog = true, editingCategory = event.category)
            }
            is CategoryViewEvent.DismissDialog -> {
                _uiState.value = _uiState.value.copy(showDialog = false, editingCategory = null)
            }
            is CategoryViewEvent.SaveCategory -> {
                val editing = _uiState.value.editingCategory
                _uiState.value = _uiState.value.copy(showDialog = false, editingCategory = null)
                if (editing != null) {
                    updateCategory(editing.id, event.name, event.description, event.color)
                } else {
                    createCategory(event.name, event.description, event.color)
                }
            }
            is CategoryViewEvent.RequestDeleteCategory -> {
                _uiState.value = _uiState.value.copy(deletingCategoryId = event.categoryId)
            }
            is CategoryViewEvent.ConfirmDeleteCategory -> {
                val categoryId = _uiState.value.deletingCategoryId ?: return
                _uiState.value = _uiState.value.copy(deletingCategoryId = null)
                deleteCategory(categoryId)
            }
            is CategoryViewEvent.DismissDeleteConfirm -> {
                _uiState.value = _uiState.value.copy(deletingCategoryId = null)
            }
        }
    }

    private fun loadCategories() {
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

    private fun createCategory(name: String, description: String?, color: String?) {
        viewModelScope.launch {
            try {
                repository.createCategory(CreateCategoryRequest(name = name, description = description, color = color))
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun updateCategory(id: String, name: String, description: String?, color: String?) {
        viewModelScope.launch {
            try {
                repository.updateCategory(id, UpdateCategoryRequest(name = name, description = description, color = color))
                loadCategories()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    private fun deleteCategory(id: String) {
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
