package dev.happe.shelfie.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.happe.shelfie.shared.Category
import dev.happe.shelfie.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

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
            FloatingActionButton(onClick = {
                editingCategory = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState.isLoading && uiState.categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.categories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category = category,
                            onEdit = {
                                editingCategory = category
                                showDialog = true
                            },
                            onDelete = { viewModel.deleteCategory(category.id) },
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoryDialog(
            category = editingCategory,
            onDismiss = { showDialog = false },
            onSave = { name, description, color ->
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.id, name, description, color)
                } else {
                    viewModel.createCategory(name, description, color)
                }
                showDialog = false
            },
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
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
            // Color swatch
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
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Category") },
            text = { Text("Are you sure you want to delete \"${category.name}\"?") },
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

/**
 * Parse a hex color string like "#FF5722" or "FF5722" to a Compose Color.
 * Falls back to gray on parse failure.
 */
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
