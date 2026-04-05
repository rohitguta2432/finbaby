package com.finbaby.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.ui.components.CategoryIcon
import com.finbaby.app.ui.components.FinBabyTopBar
import com.finbaby.app.ui.components.resolveIcon
import com.finbaby.app.ui.theme.Primary

private val availableIcons = listOf(
    "shopping_bag", "restaurant", "local_gas_station", "home_work",
    "receipt_long", "medical_services", "shopping_cart", "school",
    "cleaning_services", "account_balance", "movie", "directions_car"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }

    // Add/Edit bottom sheet
    if (showBottomSheet) {
        CategoryBottomSheet(
            editingCategory = editingCategory,
            onDismiss = {
                showBottomSheet = false
                editingCategory = null
            },
            onSave = { name, icon ->
                if (editingCategory != null) {
                    viewModel.updateCategory(editingCategory!!.copy(name = name, icon = icon))
                } else {
                    viewModel.addCustomCategory(name, icon)
                }
                showBottomSheet = false
                editingCategory = null
            }
        )
    }

    // Delete confirmation
    if (uiState.showDeleteDialog && uiState.categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${uiState.categoryToDelete!!.name}\"? Transactions using this category will become uncategorized.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            FinBabyTopBar(
                title = "Manage Categories",
                showBackArrow = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    showBottomSheet = true
                },
                containerColor = Primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Default Categories Section
            item {
                Text(
                    text = "Default Categories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(
                uiState.categories.filter { it.isDefault },
                key = { it.id }
            ) { category ->
                DefaultCategoryRow(
                    category = category,
                    onToggle = { viewModel.toggleCategory(category) }
                )
            }

            // Custom Categories Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Custom Categories",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            val customCategories = uiState.categories.filter { !it.isDefault }
            if (customCategories.isEmpty()) {
                item {
                    Text(
                        text = "No custom categories yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(customCategories, key = { it.id }) { category ->
                    CustomCategoryRow(
                        category = category,
                        onEdit = {
                            editingCategory = category
                            showBottomSheet = true
                        },
                        onDelete = { viewModel.requestDelete(category) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun DefaultCategoryRow(
    category: CategoryEntity,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryIcon(
                iconName = category.icon,
                color = category.color,
                size = 40.dp,
                iconSize = 22.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category.budgetType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = category.isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
private fun CustomCategoryRow(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryIcon(
                iconName = category.icon,
                color = category.color,
                size = 40.dp,
                iconSize = 22.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = category.budgetType.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBottomSheet(
    editingCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf(editingCategory?.name ?: "") }
    var selectedIcon by remember { mutableStateOf(editingCategory?.icon ?: availableIcons[0]) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (editingCategory != null) "Edit Category" else "Add Custom Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text(
                text = "Select Icon",
                style = MaterialTheme.typography.labelLarge
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableIcons) { iconName ->
                    val isSelected = iconName == selectedIcon
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedIcon = iconName },
                        label = {
                            Icon(
                                imageVector = resolveIcon(iconName),
                                contentDescription = iconName,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedIcon)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Save", modifier = Modifier.padding(vertical = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// UiState is defined in CategoryManagementViewModel.kt
