package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.happe.shelfie.shared.Category
import dev.happe.shelfie.viewmodel.CategoryViewEvent
import dev.happe.shelfie.viewmodel.CategoryViewModel
import dev.happe.shelfie.viewmodel.CategoryViewState

@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onNavigateBack: () -> Unit,
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    CategoryScreen(
        viewState = viewState,
        onEvent = viewModel::handleEvent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryScreen(
    viewState: CategoryViewState,
    onEvent: (CategoryViewEvent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEvent(CategoryViewEvent.AddCategory) }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
    ) { paddingValues ->
        when (val state = viewState) {
            is CategoryViewState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is CategoryViewState.Error -> {
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
            is CategoryViewState.Content -> {
                if (state.categories.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No categories yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap + to create a category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.categories, key = { it.id }) { category ->
                            CategoryCard(
                                category = category,
                                showDeleteConfirm = state.deletingCategoryId == category.id,
                                onEdit = { onEvent(CategoryViewEvent.EditCategory(category)) },
                                onRequestDelete = { onEvent(CategoryViewEvent.RequestDeleteCategory(category.id)) },
                                onConfirmDelete = { onEvent(CategoryViewEvent.ConfirmDeleteCategory) },
                                onDismissDelete = { onEvent(CategoryViewEvent.DismissDeleteConfirm) },
                            )
                        }
                    }
                }

                if (state.showDialog) {
                    CategoryDialog(
                        category = state.editingCategory,
                        onDismiss = { onEvent(CategoryViewEvent.DismissDialog) },
                        onSave = { name, description, color ->
                            onEvent(CategoryViewEvent.SaveCategory(name, description, color))
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
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
            if (category.color != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(parseColor(category.color)),
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                )
                val desc = category.description
                if (desc != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete \"${category.name}\"?") },
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

@Composable
private fun CategoryDialog(
    category: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, color: String?) -> Unit,
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var description by remember { mutableStateOf(category?.description ?: "") }
    var color by remember { mutableStateOf(category?.color ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category != null) "Edit Category" else "New Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color (hex, e.g. #FF5722)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (color.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Preview: ", style = MaterialTheme.typography.bodySmall)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColor(color)),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        name,
                        description.ifBlank { null },
                        color.ifBlank { null },
                    )
                },
                enabled = name.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun parseColor(hex: String?): Color {
    if (hex == null) return Color.Gray
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorLong = cleanHex.toLong(16)
        when (cleanHex.length) {
            6 -> Color(0xFF000000 or colorLong)
            8 -> Color(colorLong)
            else -> Color.Gray
        }
    } catch (_: Exception) {
        Color.Gray
    }
}
