package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PantryViewState {
    data object Loading : PantryViewState
    data class Error(val message: String) : PantryViewState
    data class Content(
        val items: List<PantryItem>,
        val categories: List<Category>,
        val searchQuery: String,
        val selectedCategoryId: String?,
        val sortBy: String,
    ) : PantryViewState
}

private data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
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

    val viewState: StateFlow<PantryViewState> = _uiState.map { state ->
        when {
            state.isLoading -> PantryViewState.Loading
            state.error != null -> PantryViewState.Error(state.error)
            else -> PantryViewState.Content(
                items = state.items,
                categories = state.categories,
                searchQuery = state.searchQuery,
                selectedCategoryId = state.selectedCategoryId,
                sortBy = state.sortBy,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PantryViewState.Loading)

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
