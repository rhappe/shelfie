package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val sortBy: String = "name",
)

class PantryViewModel(
    private val pantryRepository: PantryRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val items = pantryRepository.getItems(
                    search = _uiState.value.searchQuery.ifBlank { null },
                    categoryId = _uiState.value.selectedCategoryId,
                    sortBy = _uiState.value.sortBy,
                )
                val categories = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(items = items, categories = categories, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadData()
    }

    fun setSelectedCategory(categoryId: String?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadData()
    }

    fun setSortBy(sortBy: String) {
        _uiState.value = _uiState.value.copy(sortBy = sortBy)
        loadData()
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            try {
                pantryRepository.deleteItem(id)
                loadData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
