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
        val showSortMenu: Boolean,
        val deletingItemId: String?,
    ) : PantryViewState
}

sealed interface PantryViewEvent {
    data class SearchQueryChanged(val query: String) : PantryViewEvent
    data class CategorySelected(val categoryId: String?) : PantryViewEvent
    data class SortByChanged(val sortBy: String) : PantryViewEvent
    data object ToggleSortMenu : PantryViewEvent
    data object DismissSortMenu : PantryViewEvent
    data class RequestDeleteItem(val itemId: String) : PantryViewEvent
    data object ConfirmDeleteItem : PantryViewEvent
    data object DismissDeleteConfirm : PantryViewEvent
}

private data class PantryUiState(
    val items: List<PantryItem> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val sortBy: String = "name",
    val showSortMenu: Boolean = false,
    val deletingItemId: String? = null,
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
                showSortMenu = state.showSortMenu,
                deletingItemId = state.deletingItemId,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PantryViewState.Loading)

    init {
        loadData()
    }

    fun handleEvent(event: PantryViewEvent) {
        when (event) {
            is PantryViewEvent.SearchQueryChanged -> {
                _uiState.value = _uiState.value.copy(searchQuery = event.query)
                loadData()
            }
            is PantryViewEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(selectedCategoryId = event.categoryId)
                loadData()
            }
            is PantryViewEvent.SortByChanged -> {
                _uiState.value = _uiState.value.copy(sortBy = event.sortBy, showSortMenu = false)
                loadData()
            }
            is PantryViewEvent.ToggleSortMenu -> {
                _uiState.value = _uiState.value.copy(showSortMenu = !_uiState.value.showSortMenu)
            }
            is PantryViewEvent.DismissSortMenu -> {
                _uiState.value = _uiState.value.copy(showSortMenu = false)
            }
            is PantryViewEvent.RequestDeleteItem -> {
                _uiState.value = _uiState.value.copy(deletingItemId = event.itemId)
            }
            is PantryViewEvent.ConfirmDeleteItem -> {
                val itemId = _uiState.value.deletingItemId ?: return
                _uiState.value = _uiState.value.copy(deletingItemId = null)
                deleteItem(itemId)
            }
            is PantryViewEvent.DismissDeleteConfirm -> {
                _uiState.value = _uiState.value.copy(deletingItemId = null)
            }
        }
    }

    private fun loadData() {
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

    private fun deleteItem(id: String) {
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
