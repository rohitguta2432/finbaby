package com.finbaby.app.ui.addtransaction

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.ui.components.CategoryIcon
import com.finbaby.app.ui.theme.ExpenseRed
import com.finbaby.app.ui.theme.IncomeGreen

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    onDismiss: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val typeColor by animateColorAsState(
        targetValue = if (uiState.isExpense) ExpenseRed else IncomeGreen,
        label = "typeColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Expense / Income toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = uiState.isExpense,
                onClick = { viewModel.setIsExpense(true) },
                label = { Text("Expense") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ExpenseRed.copy(alpha = 0.15f),
                    selectedLabelColor = ExpenseRed
                ),
                border = null
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilterChip(
                selected = !uiState.isExpense,
                onClick = { viewModel.setIsExpense(false) },
                label = { Text("Income") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IncomeGreen.copy(alpha = 0.15f),
                    selectedLabelColor = IncomeGreen
                ),
                border = null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Amount display
        Text(
            text = "₹${uiState.amountText.ifEmpty { "0" }}",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = typeColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Numpad
        val numpadKeys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "DEL")

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(numpadKeys) { key ->
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable {
                            if (key == "DEL") {
                                viewModel.onBackspace()
                            } else {
                                viewModel.onNumpadClick(key)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (key == "DEL") {
                        Icon(
                            imageVector = Icons.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Category grid
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 3
        ) {
            uiState.categories.forEach { category ->
                val isSelected = uiState.selectedCategoryId == category.id
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.onCategorySelected(category.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CategoryIcon(
                            iconName = category.icon,
                            color = category.color,
                            size = 36.dp,
                            iconSize = 18.dp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note field
        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.onNoteChanged(it) },
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Account type selector
        Text(
            text = "Account",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val accountTypes = listOf("cash", "bank", "upi", "credit_card")
            val accountLabels = listOf("Cash", "Bank", "UPI", "Credit Card")

            accountTypes.forEachIndexed { index, type ->
                FilterChip(
                    selected = uiState.accountType == type,
                    onClick = { viewModel.onAccountTypeChanged(type) },
                    label = {
                        Text(
                            text = accountLabels[index],
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    border = null
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recurring toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recurring",
                style = MaterialTheme.typography.bodyMedium
            )
            Switch(
                checked = uiState.isRecurring,
                onCheckedChange = { viewModel.onRecurringChanged(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save button
        Button(
            onClick = {
                viewModel.saveTransaction {
                    onDismiss()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = typeColor
            ),
            enabled = uiState.amountText.isNotEmpty() &&
                    (uiState.amountText.toDoubleOrNull() ?: 0.0) > 0 &&
                    uiState.selectedCategoryId != null
        ) {
            Text(
                text = "Save",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
