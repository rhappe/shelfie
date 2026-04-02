package dev.happe.shelfie.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.happe.shelfie.data.repository.CategoryRepository
import dev.happe.shelfie.data.repository.PantryRepository
import dev.happe.shelfie.shared.Category
import dev.happe.shelfie.shared.CreatePantryItemRequest
import dev.happe.shelfie.shared.UpdatePantryItemRequest
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
        val isEditing: Boolean,
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
        val unitDropdownExpanded: Boolean,
        val categoryDropdownExpanded: Boolean,
    ) : AddEditItemViewState
}

sealed interface AddEditItemViewEffect {
    data object NavigateBack : AddEditItemViewEffect
}

sealed interface AddEditItemViewEvent {
    data class NameChanged(val name: String) : AddEditItemViewEvent
    data class QuantityChanged(val quantity: String) : AddEditItemViewEvent
    data class UnitSelected(val unit: String) : AddEditItemViewEvent
    data class CategorySelected(val categoryId: String?) : AddEditItemViewEvent
    data class ExpirationDateChanged(val date: String?) : AddEditItemViewEvent
    data class LowStockThresholdChanged(val threshold: String) : AddEditItemViewEvent
    data class NotifyOnLowStockChanged(val notify: Boolean) : AddEditItemViewEvent
    data class UnitDropdownExpandedChanged(val expanded: Boolean) : AddEditItemViewEvent
    data class CategoryDropdownExpandedChanged(val expanded: Boolean) : AddEditItemViewEvent
    data object Save : AddEditItemViewEvent
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
    val unitDropdownExpanded: Boolean = false,
    val categoryDropdownExpanded: Boolean = false,
)

class AddEditItemViewModel(
    private val pantryRepository: PantryRepository,
    private val categoryRepository: CategoryRepository,
    private val itemId: String? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditItemUiState())
    private val _effects = Channel<AddEditItemViewEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    val viewState: StateFlow<AddEditItemViewState> = _uiState.map { state ->
        when {
            state.isLoading -> AddEditItemViewState.Loading
            state.error != null -> AddEditItemViewState.Error(state.error)
            else -> AddEditItemViewState.Content(
                isEditing = itemId != null,
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
                unitDropdownExpanded = state.unitDropdownExpanded,
                categoryDropdownExpanded = state.categoryDropdownExpanded,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AddEditItemViewState.Loading)

    init {
        loadCategories()
        if (itemId != null) {
            loadItem(itemId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getCategories()
                _uiState.value = _uiState.value.copy(categories = categories)
            } catch (_: Exception) {
            }
        }
    }

    private fun loadItem(itemId: String) {
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

    fun handleEvent(event: AddEditItemViewEvent) {
        when (event) {
            is AddEditItemViewEvent.NameChanged -> {
                _uiState.value = _uiState.value.copy(name = event.name, validationError = null)
            }
            is AddEditItemViewEvent.QuantityChanged -> {
                _uiState.value = _uiState.value.copy(quantity = event.quantity)
            }
            is AddEditItemViewEvent.UnitSelected -> {
                _uiState.value = _uiState.value.copy(unit = event.unit, unitDropdownExpanded = false)
            }
            is AddEditItemViewEvent.CategorySelected -> {
                _uiState.value = _uiState.value.copy(categoryId = event.categoryId, categoryDropdownExpanded = false)
            }
            is AddEditItemViewEvent.ExpirationDateChanged -> {
                _uiState.value = _uiState.value.copy(expirationDate = event.date)
            }
            is AddEditItemViewEvent.LowStockThresholdChanged -> {
                _uiState.value = _uiState.value.copy(lowStockThreshold = event.threshold)
            }
            is AddEditItemViewEvent.NotifyOnLowStockChanged -> {
                _uiState.value = _uiState.value.copy(notifyOnLowStock = event.notify)
            }
            is AddEditItemViewEvent.UnitDropdownExpandedChanged -> {
                _uiState.value = _uiState.value.copy(unitDropdownExpanded = event.expanded)
            }
            is AddEditItemViewEvent.CategoryDropdownExpandedChanged -> {
                _uiState.value = _uiState.value.copy(categoryDropdownExpanded = event.expanded)
            }
            is AddEditItemViewEvent.Save -> save()
        }
    }

    private fun save() {
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
                if (itemId != null) {
                    pantryRepository.updateItem(
                        itemId!!,
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
