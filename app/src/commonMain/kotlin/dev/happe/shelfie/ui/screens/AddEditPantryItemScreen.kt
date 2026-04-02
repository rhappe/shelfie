package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.happe.shelfie.viewmodel.AddEditItemViewEffect
import dev.happe.shelfie.viewmodel.AddEditItemViewModel
import dev.happe.shelfie.viewmodel.AddEditItemViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPantryItemScreen(
    viewModel: AddEditItemViewModel,
    itemId: String?,
    onNavigateBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsState()
    val isEditing = itemId != null

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
        if (itemId != null) {
            viewModel.loadItem(itemId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AddEditItemViewEffect.NavigateBack -> onNavigateBack()
            }
        }
    }

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
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = state.validationError == "Name is required",
                    )

                    // Quantity
                    OutlinedTextField(
                        value = state.quantity,
                        onValueChange = { viewModel.updateQuantity(it) },
                        label = { Text("Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Unit dropdown
                    var unitExpanded by remember { mutableStateOf(false) }
                    val unitOptions = listOf("count", "oz", "lb", "g", "kg", "ml", "L", "cups", "tbsp", "tsp", "gal", "qt", "pt")
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = state.unit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false },
                        ) {
                            unitOptions.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        viewModel.updateUnit(unit)
                                        unitExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    // Category dropdown
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = state.categories.find { it.id == state.categoryId }?.name ?: "None",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    viewModel.updateCategoryId(null)
                                    categoryExpanded = false
                                },
                            )
                            state.categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        viewModel.updateCategoryId(category.id)
                                        categoryExpanded = false
                                    },
                                )
                            }
                        }
                    }

                    // Expiration date
                    OutlinedTextField(
                        value = state.expirationDate ?: "",
                        onValueChange = { viewModel.updateExpirationDate(it.ifBlank { null }) },
                        label = { Text("Expiration Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // Low stock threshold
                    OutlinedTextField(
                        value = state.lowStockThreshold,
                        onValueChange = { viewModel.updateLowStockThreshold(it) },
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
                            onCheckedChange = { viewModel.updateNotifyOnLowStock(it) },
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
                        onClick = { viewModel.save() },
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
