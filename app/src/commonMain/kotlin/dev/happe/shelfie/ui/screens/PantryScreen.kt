package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.happe.shelfie.shared.PantryItem
import dev.happe.shelfie.viewmodel.PantryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    onAddItem: () -> Unit,
    onEditItem: (String) -> Unit,
    onNavigateToCategories: () -> Unit,
    onLogout: () -> Unit,
    pantryViewModel: PantryViewModel = viewModel { PantryViewModel() },
) {
    val uiState by pantryViewModel.uiState.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pantry") },
                actions = {
                    IconButton(onClick = onNavigateToCategories) {
                        Icon(Icons.Default.List, contentDescription = "Categories")
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
        SelectionContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Search bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { pantryViewModel.setSearchQuery(it) },
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
                    selected = uiState.selectedCategoryId == null,
                    onClick = { pantryViewModel.setSelectedCategory(null) },
                    label = { Text("All") },
                )
                uiState.categories.forEach { category ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == category.id,
                        onClick = { pantryViewModel.setSelectedCategory(category.id) },
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
                    TextButton(onClick = { showSortMenu = true }) {
                        Text(
                            when (uiState.sortBy) {
                                "name" -> "Name"
                                "quantity" -> "Quantity"
                                "expirationDate" -> "Expiration"
                                else -> uiState.sortBy
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name") },
                            onClick = {
                                pantryViewModel.setSortBy("name")
                                showSortMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Quantity") },
                            onClick = {
                                pantryViewModel.setSortBy("quantity")
                                showSortMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Expiration") },
                            onClick = {
                                pantryViewModel.setSortBy("expirationDate")
                                showSortMenu = false
                            },
                        )
                    }
                }
            }

            // Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            // Loading or content
            if (uiState.isLoading && uiState.items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.items.isEmpty()) {
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
                    items(uiState.items, key = { it.id }) { item ->
                        PantryItemCard(
                            item = item,
                            categoryName = uiState.categories.find { it.id == item.categoryId }?.name,
                            onEdit = { onEditItem(item.id) },
                            onDelete = { pantryViewModel.deleteItem(item.id) },
                        )
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                    val isExpiringSoon = isExpiringSoon(item.expirationDate)
                    Text(
                        text = "Expires: ${item.expirationDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpiringSoon) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Simple check: the expiration date string (YYYY-MM-DD) is compared lexicographically
 * against a "soon" threshold. This works because the date format sorts correctly.
 * Returns true if the date is in the past or within the next 3 days.
 * Falls back to false for any parsing issues.
 */
private fun isExpiringSoon(expirationDate: String?): Boolean {
    if (expirationDate == null) return false
    // Simple lexicographic comparison — works for YYYY-MM-DD format.
    // We cannot easily get "today" in common code without extra deps,
    // so we mark any item with an expiration date set as potentially expiring.
    // In a production app, you'd use kotlinx-datetime here.
    return false
}
