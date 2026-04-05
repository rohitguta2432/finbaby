package com.finbaby.app.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.ui.components.CategoryIcon
import com.finbaby.app.ui.components.FinBabyTopBar
import com.finbaby.app.ui.theme.ExpenseRed
import com.finbaby.app.ui.theme.Primary
import com.finbaby.app.util.CurrencyFormatter
import com.finbaby.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved, uiState.isDeleted) {
        if (uiState.isSaved || uiState.isDeleted) {
            onNavigateBack()
        }
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteDialog(false) },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteTransaction() },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            FinBabyTopBar(
                title = "Transaction Detail",
                showBackArrow = true,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.showDeleteDialog(true) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.transaction == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Transaction not found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Display card
                val category = uiState.categories.find { it.id == uiState.selectedCategoryId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (category != null) {
                            CategoryIcon(
                                iconName = category.icon,
                                color = category.color,
                                size = 56.dp,
                                iconSize = 30.dp
                            )
                            SuggestionChip(
                                onClick = {},
                                label = { Text(category.name) }
                            )
                        }
                        Text(
                            text = CurrencyFormatter.format(uiState.amount.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.type == "expense") ExpenseRed else Primary
                        )
                        Text(
                            text = DateUtils.formatDateTime(uiState.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(uiState.accountType.replaceFirstChar { it.uppercase() }.replace("_", " ")) }
                        )
                    }
                }

                // Editable fields
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.updateAmount(it) },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    prefix = { Text("\u20B9") }
                )

                // Category selector
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        uiState.categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    viewModel.updateCategory(cat.id)
                                    categoryExpanded = false
                                },
                                leadingIcon = {
                                    CategoryIcon(
                                        iconName = cat.icon,
                                        color = cat.color,
                                        size = 28.dp,
                                        iconSize = 16.dp
                                    )
                                }
                            )
                        }
                    }
                }

                // Account type selector
                val accountTypes = listOf("cash", "bank", "upi", "credit_card")
                var accountExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.accountType.replaceFirstChar { it.uppercase() }.replace("_", " "),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accountTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.replaceFirstChar { it.uppercase() }.replace("_", " ")) },
                                onClick = {
                                    viewModel.updateAccountType(type)
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.updateNote(it) },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Date picker
                var showDatePicker by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = DateUtils.formatDate(uiState.date),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date")
                        }
                    }
                )

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = uiState.date
                    )
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { viewModel.updateDate(it) }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Recurring toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recurring", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = uiState.isRecurring,
                        onCheckedChange = { viewModel.updateRecurring(it) }
                    )
                }

                if (uiState.isRecurring) {
                    val periods = listOf("daily", "weekly", "monthly")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periods.forEach { period ->
                            FilterChip(
                                selected = uiState.recurringPeriod == period,
                                onClick = { viewModel.updateRecurringPeriod(period) },
                                label = { Text(period.replaceFirstChar { it.uppercase() }) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action buttons
                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Save Changes", modifier = Modifier.padding(vertical = 4.dp))
                }

                OutlinedButton(
                    onClick = { viewModel.showDeleteDialog(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ExpenseRed
                    )
                ) {
                    Text("Delete", modifier = Modifier.padding(vertical = 4.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
