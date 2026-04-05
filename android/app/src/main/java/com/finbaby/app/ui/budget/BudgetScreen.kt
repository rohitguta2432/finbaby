package com.finbaby.app.ui.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.finbaby.app.navigation.Routes
import com.finbaby.app.ui.components.FinBabyBottomNav
import com.finbaby.app.ui.theme.BudgetDanger
import com.finbaby.app.ui.theme.BudgetSafe
import com.finbaby.app.ui.theme.BudgetWarning
import com.finbaby.app.util.CurrencyFormatter

@Composable
fun BudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            FinBabyBottomNav(
                currentRoute = Routes.BUDGET,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = state.monthDisplayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next month",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Budget Summary Card
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                ),
                                start = Offset.Zero,
                                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = "Monthly Budget",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = CurrencyFormatter.format(state.totalBudget),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Spent",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(state.totalSpent),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = CurrencyFormatter.format(
                                        kotlin.math.abs(state.totalRemaining)
                                    ),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (state.totalRemaining >= 0) Color.White
                                    else Color(0xFFFFCDD2)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val overallProgress = if (state.totalBudget > 0) {
                            (state.totalSpent / state.totalBudget).toFloat().coerceIn(0f, 1f)
                        } else 0f
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            strokeCap = StrokeCap.Round,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Budget Allocation Strategy
            Text(
                text = "Budget Allocation",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            AllocationBar(
                needsPercent = state.needsPercent,
                wantsPercent = state.wantsPercent,
                savingsPercent = state.savingsPercent
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Budgets
            Text(
                text = "Category Budgets",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            state.categoryBudgets.forEach { item ->
                CategoryBudgetCard(item = item)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Category Budget Button
            Button(
                onClick = viewModel::showAddBudgetDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Category Budget",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (state.showAddDialog) {
        AddCategoryBudgetDialog(
            categories = state.allCategories,
            existingBudgetCategoryIds = state.categoryBudgets
                .filter { it.budget != null }
                .map { it.category.id }
                .toSet(),
            onDismiss = viewModel::dismissAddBudgetDialog,
            onSave = { categoryId, amount -> viewModel.saveCategoryBudget(categoryId, amount) }
        )
    }
}

@Composable
private fun AddCategoryBudgetDialog(
    categories: List<com.finbaby.app.data.db.entity.CategoryEntity>,
    existingBudgetCategoryIds: Set<Long>,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, amount: Double) -> Unit
) {
    var selectedCategoryId by remember { mutableLongStateOf(-1L) }
    var amountText by remember { mutableStateOf("") }

    val availableCategories = categories.filter { it.id !in existingBudgetCategoryIds }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category Budget") },
        text = {
            Column {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (availableCategories.isEmpty()) {
                    Text(
                        text = "All categories already have budgets",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column {
                        availableCategories.forEach { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else Color.Transparent
                                    )
                                    .clickable { selectedCategoryId = cat.id }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            Color(cat.color).copy(alpha = 0.15f),
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = cat.icon.take(2),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(cat.color)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Budget Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            val amount = amountText.toDoubleOrNull()
            TextButton(
                onClick = { if (selectedCategoryId > 0 && amount != null && amount > 0) onSave(selectedCategoryId, amount) },
                enabled = selectedCategoryId > 0 && (amount ?: 0.0) > 0
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AllocationBar(
    needsPercent: Int,
    wantsPercent: Int,
    savingsPercent: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Stacked horizontal bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(needsPercent.toFloat())
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Box(
                    modifier = Modifier
                        .weight(wantsPercent.toFloat())
                        .height(24.dp)
                        .background(BudgetWarning)
                )
                Box(
                    modifier = Modifier
                        .weight(savingsPercent.toFloat())
                        .height(24.dp)
                        .background(BudgetSafe)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AllocationLabel(
                    color = MaterialTheme.colorScheme.primary,
                    label = "Monthly Bills",
                    percent = needsPercent
                )
                AllocationLabel(
                    color = BudgetWarning,
                    label = "Daily Spending",
                    percent = wantsPercent
                )
                AllocationLabel(
                    color = BudgetSafe,
                    label = "Money Saved",
                    percent = savingsPercent
                )
            }
        }
    }
}

@Composable
private fun AllocationLabel(
    color: Color,
    label: String,
    percent: Int
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label $percent%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryBudgetCard(item: CategoryBudgetItem) {
    val isExceeded = item.limit > 0 && item.spent > item.limit
    val progressColor = when {
        item.progressPercent > 0.9f -> BudgetDanger
        item.progressPercent > 0.7f -> BudgetWarning
        else -> BudgetSafe
    }
    val containerColor = if (isExceeded) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(item.category.color).copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.category.icon.take(2),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(item.category.color)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (item.limit > 0) {
                        Text(
                            text = "${CurrencyFormatter.formatCompact(item.spent)} / ${CurrencyFormatter.formatCompact(item.limit)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = CurrencyFormatter.formatCompact(item.spent),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (item.limit > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { item.progressPercent.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = progressColor.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}
