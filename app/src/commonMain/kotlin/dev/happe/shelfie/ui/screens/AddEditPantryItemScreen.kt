package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.happe.shelfie.viewmodel.AddEditItemViewEffect
import dev.happe.shelfie.viewmodel.AddEditItemViewEvent
import dev.happe.shelfie.viewmodel.AddEditItemViewModel
import dev.happe.shelfie.viewmodel.AddEditItemViewState

@Composable
fun AddEditPantryItemScreen(
    viewModel: AddEditItemViewModel,
    onNavigateBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AddEditItemViewEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

    AddEditPantryItemScreen(
        viewState = viewState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPantryItemScreen(
    viewState: AddEditItemViewState,
    onEvent: (AddEditItemViewEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isEditing = viewState is AddEditItemViewState.Content && viewState.isEditing

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Item" else "Add Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        when (val state = viewState) {
            is AddEditItemViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is AddEditItemViewState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is AddEditItemViewState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // Name
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { onEvent(AddEditItemViewEvent.NameChanged(it)) },
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.validationError == "Name is required",
                    )

                    // Quantity
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = { onEvent(AddEditItemViewEvent.QuantityChanged(it)) },
                        label = { Text("Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Unit dropdown
                    val unitOptions = listOf(
                        "count", "oz", "lb", "g", "kg", "ml", "L", "cups", "tbsp", "tsp", "gal", "qt", "pt",
                    )
                    ExposedDropdownMenuBox(
                        expanded = state.unitDropdownExpanded,
                        onExpandedChange = { onEvent(AddEditItemViewEvent.UnitDropdownExpandedChanged(it)) },
                    ) {
                        OutlinedTextField(
                            value = state.unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.unitDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = state.unitDropdownExpanded,
                            onDismissRequest = { onEvent(AddEditItemViewEvent.UnitDropdownExpandedChanged(false)) },
                        ) {
                            unitOptions.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = { onEvent(AddEditItemViewEvent.UnitSelected(unit)) },
                                )
                            }
                        }
                    }

                    // Category dropdown
                    ExposedDropdownMenuBox(
                        expanded = state.categoryDropdownExpanded,
                        onExpandedChange = { onEvent(AddEditItemViewEvent.CategoryDropdownExpandedChanged(it)) },
                    ) {
                        OutlinedTextField(
                            value = state.categories.find { it.id == state.categoryId }?.name ?: "None",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = state.categoryDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = state.categoryDropdownExpanded,
                            onDismissRequest = { onEvent(AddEditItemViewEvent.CategoryDropdownExpandedChanged(false)) },
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = { onEvent(AddEditItemViewEvent.CategorySelected(null)) },
                            )
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = { onEvent(AddEditItemViewEvent.CategorySelected(category.id)) },
                                )
                            }
                        }
                    }

                    // Expiration date
                    OutlinedTextField(
                        value = state.expirationDate ?: "",
                        onValueChange = { onEvent(AddEditItemViewEvent.ExpirationDateChanged(it.ifBlank { null })) },
                        label = { Text("Expiration Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Low stock threshold
                    OutlinedTextField(
                        value = state.lowStockThreshold,
                        onValueChange = { onEvent(AddEditItemViewEvent.LowStockThresholdChanged(it)) },
                        label = { Text("Low Stock Threshold") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Notify on low stock
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Notify on low stock")
                        Switch(
                            checked = state.notifyOnLowStock,
                            onCheckedChange = { onEvent(AddEditItemViewEvent.NotifyOnLowStockChanged(it)) },
                        )
                    }

                    // Validation error
                    if (state.validationError != null) {
                        Text(
                            text = state.validationError,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    // Save button
                    Button(
                        onClick = { onEvent(AddEditItemViewEvent.Save) },
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(if (isEditing) "Update Item" else "Add Item")
                        }
                    }
                }
            }
        }
    }
}
