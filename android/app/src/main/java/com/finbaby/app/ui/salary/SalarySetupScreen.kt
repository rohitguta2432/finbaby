package com.finbaby.app.ui.salary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalarySetupScreen(
    onNavigateToHome: () -> Unit,
    viewModel: SalarySetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var dateDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Set Up Your Salary",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We'll split your salary into 3 simple buckets",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Salary input
        OutlinedTextField(
            value = uiState.salaryText,
            onValueChange = { viewModel.onSalaryChanged(it) },
            label = { Text("Monthly Salary") },
            prefix = { Text("₹", style = MaterialTheme.typography.headlineSmall) },
            textStyle = MaterialTheme.typography.headlineSmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Salary date dropdown
        ExposedDropdownMenuBox(
            expanded = dateDropdownExpanded,
            onExpandedChange = { dateDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = "Day ${uiState.salaryDate}",
                onValueChange = {},
                readOnly = true,
                label = { Text("Salary Credit Date") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dateDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            ExposedDropdownMenu(
                expanded = dateDropdownExpanded,
                onDismissRequest = { dateDropdownExpanded = false }
            ) {
                (1..31).forEach { day ->
                    DropdownMenuItem(
                        text = { Text("Day $day") },
                        onClick = {
                            viewModel.onSalaryDateChanged(day)
                            dateDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Budget Split
        Text(
            text = "How to split your salary?",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Adjust the sliders to match your lifestyle",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        BudgetSlider(
            label = "🏠 Monthly Bills",
            percent = uiState.needsPercent,
            amount = uiState.salary * uiState.needsPercent / 100.0,
            color = MaterialTheme.colorScheme.primary,
            onValueChange = { viewModel.onNeedsPercentChanged(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BudgetSlider(
            label = "☕ Daily Spending",
            percent = uiState.wantsPercent,
            amount = uiState.salary * uiState.wantsPercent / 100.0,
            color = MaterialTheme.colorScheme.tertiary,
            onValueChange = { viewModel.onWantsPercentChanged(it) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        BudgetSlider(
            label = "💰 Money Saved",
            percent = uiState.savingsPercent,
            amount = uiState.salary * uiState.savingsPercent / 100.0,
            color = com.finbaby.app.ui.theme.BudgetSafe,
            onValueChange = { viewModel.onSavingsPercentChanged(it) }
        )

        if (uiState.needsPercent + uiState.wantsPercent + uiState.savingsPercent != 100) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Total must equal 100% (currently ${uiState.needsPercent + uiState.wantsPercent + uiState.savingsPercent}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                viewModel.saveProfile {
                    onNavigateToHome()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = uiState.salary > 0 &&
                    uiState.needsPercent + uiState.wantsPercent + uiState.savingsPercent == 100
        ) {
            Text(
                text = "Save & Continue",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BudgetSlider(
    label: String,
    percent: Int,
    amount: Double,
    color: androidx.compose.ui.graphics.Color,
    onValueChange: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$percent%",
                        style = MaterialTheme.typography.titleSmall,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Slider(
                value = percent.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.2f)
                )
            )
        }
    }
}
