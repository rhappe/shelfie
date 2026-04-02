package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.happe.shelfie.shared.PantryItem
import dev.happe.shelfie.viewmodel.PantryViewEvent
import dev.happe.shelfie.viewmodel.PantryViewModel
import dev.happe.shelfie.viewmodel.PantryViewState

@Composable
fun PantryScreen(
    viewModel: PantryViewModel,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onLogout: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    PantryScreen(
        viewState = viewState,
        onEvent = viewModel::handleEvent,
        onAddItem = onAddItem,
        onEditItem = onEditItem,
        onNavigateToCategories = onNavigateToCategories,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryScreen(
    viewState: PantryViewState,
    onEvent: (PantryViewEvent) -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantry") },
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Categories")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        },
    ) { paddingValues ->
        when (val state = viewState) {
            is PantryViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is PantryViewState.Error -> {
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
            is PantryViewState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    // Search bar
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { onEvent(PantryViewEvent.SearchQueryChanged(it)) },
                        placeholder = { Text("Search items...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )

                    // Category filter chips
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { onEvent(PantryViewEvent.CategorySelected(null)) },
                            label = { Text("All") },
                        )
                        state.categories.forEach { category ->
                            FilterChip(
                                selected = state.selectedCategoryId == category.id,
                                onClick = { onEvent(PantryViewEvent.CategorySelected(category.id)) },
                                label = { Text(category.name) },
                            )
                        }
                    }

                    // Sort row
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Sort by: ",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Box {
                            TextButton(onClick = { onEvent(PantryViewEvent.ToggleSortMenu) }) {
                                Text(
                                    when (state.sortBy) {
                                        "name" -> "Name"
                                        "quantity" -> "Quantity"
                                        "expirationDate" -> "Expiration"
                                        else -> state.sortBy
                                    }
                                )
                            }
                            DropdownMenu(
                                expanded = state.showSortMenu,
                                onDismissRequest = { onEvent(PantryViewEvent.DismissSortMenu) },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name") },
                                    onClick = { onEvent(PantryViewEvent.SortByChanged("name")) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Quantity") },
                                    onClick = { onEvent(PantryViewEvent.SortByChanged("quantity")) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Expiration") },
                                    onClick = { onEvent(PantryViewEvent.SortByChanged("expirationDate")) },
                                )
                            }
                        }
                    }

                    // Content
                    if (state.items.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No items in your pantry",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to add your first item",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(state.items, key = { it.id }) { item ->
                                PantryItemCard(
                                    item = item,
                                    categoryName = state.categories.find { it.id == item.categoryId }?.name,
                                    showDeleteConfirm = state.deletingItemId == item.id,
                                    onEdit = { onEditItem(item.id) },
                                    onRequestDelete = { onEvent(PantryViewEvent.RequestDeleteItem(item.id)) },
                                    onConfirmDelete = { onEvent(PantryViewEvent.ConfirmDeleteItem) },
                                    onDismissDelete = { onEvent(PantryViewEvent.DismissDeleteConfirm) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryItemCard(
    item: PantryItem,
    categoryName: String?,
    showDeleteConfirm: Boolean,
    onEdit: () -> Unit,
    onRequestDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${item.quantity} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (categoryName != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (item.expirationDate != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Expires: ${item.expirationDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (item.quantity <= item.lowStockThreshold && item.lowStockThreshold > 0.0) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Low stock!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onRequestDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = onDismissDelete,
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text("Cancel")
                }
            },
        )
    }
}
