package com.finbaby.app.sms

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.ui.components.CategoryIcon
import androidx.compose.ui.unit.dp
import com.finbaby.app.ui.theme.*
import com.finbaby.app.util.CurrencyFormatter
import com.finbaby.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsImportScreen(
    onNavigateBack: () -> Unit,
    viewModel: SmsImportViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.scanSms()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Import from SMS") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (state.parsedTransactions.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                if (state.selectedIds.size == state.parsedTransactions.size) {
                                    viewModel.deselectAll()
                                } else {
                                    viewModel.selectAll()
                                }
                            }
                        ) {
                            Text(
                                if (state.selectedIds.size == state.parsedTransactions.size) "Deselect All"
                                else "Select All"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.selectedIds.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.importSelected() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary
                        )
                    ) {
                        Text(
                            "Import ${state.selectedIds.size} Transactions",
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Import success banner
            if (state.importedCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = BudgetSafe.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BudgetSafe
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${state.importedCount} transactions imported successfully!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BudgetSafe
                        )
                    }
                }
            }

            // Loading
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Scanning SMS...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                return@Column
            }

            // Error
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        error,
                        modifier = Modifier.padding(16.dp),
                        color = Error
                    )
                }
            }

            // Empty state
            if (state.parsedTransactions.isEmpty() && !state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Sms,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No new financial SMS found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "We scan for bank SMS from the last 30 days",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(24.dp))
                        OutlinedButton(onClick = { viewModel.scanSms() }) {
                            Text("Scan Again")
                        }
                    }
                }
                return@Column
            }

            // Info header
            Text(
                "${state.parsedTransactions.size} transactions found",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Transaction list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.parsedTransactions, key = { it.smsId }) { txn ->
                    SmsTransactionCard(
                        transaction = txn,
                        isSelected = txn.smsId in state.selectedIds,
                        onToggle = { viewModel.toggleSelection(txn.smsId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmsTransactionCard(
    transaction: SmsTransaction,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                Primary.copy(alpha = 0.05f)
            else
                MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator
            Icon(
                if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            // Category icon (or uncategorized)
            if (transaction.categoryId != null) {
                CategoryIcon(
                    iconName = "receipt_long",
                    color = Primary.value.toLong(),
                    size = 40.dp
                )
            } else {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("?", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.merchant ?: transaction.bank,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        transaction.bank,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(" · ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        transaction.accountType.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary
                    )
                }
                Text(
                    DateUtils.formatDateTime(transaction.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            // Amount
            Text(
                (if (transaction.type == "expense") "-" else "+") +
                        CurrencyFormatter.format(transaction.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == "expense") ExpenseRed else IncomeGreen
            )
        }
    }
}
