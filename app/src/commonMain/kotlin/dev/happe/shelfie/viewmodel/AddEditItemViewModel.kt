package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.shared.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AddEditItemViewState {
    data object Loading : AddEditItemViewState
    data class Error(val message: String) : AddEditItemViewState
    data class Content(
        val name: String,
        val quantity: String,
        val unit: String,
        val categoryId: String?,
        val expirationDate: String?,
        val lowStockThreshold: String,
        val notifyOnLowStock: Boolean,
        val barcode: String?,
        val categories: List<Category>,
        val isSaving: Boolean,
        val validationError: String?,
    ) : AddEditItemViewState
}

sealed interface AddEditItemViewEffect {
    data object NavigateBack : AddEditItemViewEffect
}

private data class AddEditItemUiState(
    val name: String = "",
    val quantity: String = "1.0",
    val unit: String = "count",
    val categoryId: String? = null,
    val expirationDate: String? = null,
    val lowStockThreshold: String = "0.0",
    val notifyOnLowStock: Boolean = true,
    val barcode: String? = null,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validationError: String? = null,
)

class AddEditItemViewModel(
    private val pantryRepository: PantryRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditItemUiState())
    private val _effects = Channel<AddEditItemViewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val viewState: StateFlow<AddEditItemViewState> = _uiState.map { state ->
        when {
            state.isLoading -> AddEditItemViewState.Loading
            state.error != null -> AddEditItemViewState.Error(state.error)
            else -> AddEditItemViewState.Content(
                name = state.name,
                quantity = state.quantity,
                unit = state.unit,
                categoryId = state.categoryId,
                expirationDate = state.expirationDate,
                lowStockThreshold = state.lowStockThreshold,
                notifyOnLowStock = state.notifyOnLowStock,
                barcode = state.barcode,
                categories = state.categories,
                isSaving = state.isSaving,
                validationError = state.validationError,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditItemViewState.Loading)

    private var editingItemId: String? = null

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(categories = categories)
            } catch (_: Exception) {
            }
        }
    }

    fun loadItem(itemId: String) {
        editingItemId = itemId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val items = pantryRepository.getItems()
                val item = items.find { it.id == itemId }
                if (item != null) {
                    _uiState.value = _uiState.value.copy(
                        name = item.name,
                        quantity = item.quantity.toString(),
                        unit = item.unit,
                        categoryId = item.categoryId,
                        expirationDate = item.expirationDate,
                        lowStockThreshold = item.lowStockThreshold.toString(),
                        notifyOnLowStock = item.notifyOnLowStock,
                        barcode = item.barcode,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, validationError = null)
    }

    fun updateQuantity(qty: String) {
        _uiState.value = _uiState.value.copy(quantity = qty)
    }

    fun updateUnit(unit: String) {
        _uiState.value = _uiState.value.copy(unit = unit)
    }

    fun updateCategoryId(id: String?) {
        _uiState.value = _uiState.value.copy(categoryId = id)
    }

    fun updateExpirationDate(date: String?) {
        _uiState.value = _uiState.value.copy(expirationDate = date)
    }

    fun updateLowStockThreshold(threshold: String) {
        _uiState.value = _uiState.value.copy(lowStockThreshold = threshold)
    }

    fun updateNotifyOnLowStock(notify: Boolean) {
        _uiState.value = _uiState.value.copy(notifyOnLowStock = notify)
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(validationError = "Name is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, validationError = null)
            try {
                val qty = state.quantity.toDoubleOrNull() ?: 1.0
                val threshold = state.lowStockThreshold.toDoubleOrNull() ?: 0.0
                if (editingItemId != null) {
                    pantryRepository.updateItem(
                        editingItemId!!,
                        UpdatePantryItemRequest(
                            name = state.name,
                            quantity = qty,
                            unit = state.unit,
                            categoryId = state.categoryId,
                            expirationDate = state.expirationDate,
                            lowStockThreshold = threshold,
                            notifyOnLowStock = state.notifyOnLowStock,
                            barcode = state.barcode,
                        ),
                    )
                } else {
                    pantryRepository.createItem(
                        CreatePantryItemRequest(
                            name = state.name,
                            quantity = qty,
                            unit = state.unit,
                            categoryId = state.categoryId,
                            expirationDate = state.expirationDate,
                            lowStockThreshold = threshold,
                            notifyOnLowStock = state.notifyOnLowStock,
                            barcode = state.barcode,
                        ),
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false)
                _effects.send(AddEditItemViewEffect.NavigateBack)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }
}
