package com.finbaby.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finbaby.app.ui.components.CategoryIcon
import com.finbaby.app.ui.components.FinBabyBottomNav
import com.finbaby.app.ui.components.FinBabyTopBar
import com.finbaby.app.ui.theme.ExpenseRed
import com.finbaby.app.ui.theme.IncomeGreen
import com.finbaby.app.util.CurrencyFormatter
import com.finbaby.app.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val grouped = viewModel.getGroupedTransactions()

    Scaffold(
        topBar = {
            FinBabyTopBar(title = "Search")
        },
        bottomBar = {
            FinBabyBottomNav(currentRoute = currentRoute, onNavigate = onNavigate)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search input
            OutlinedTextField(
                value = uiState.query,
                onValueChange = { viewModel.updateQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search transactions...") },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearQuery() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // Filter chips row
            LazyRow(
                modifier = Modifier.padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.timeFilter == TimeFilter.ALL,
                        onClick = { viewModel.updateTimeFilter(TimeFilter.ALL) },
                        label = { Text("All") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.timeFilter == TimeFilter.THIS_WEEK,
                        onClick = { viewModel.updateTimeFilter(TimeFilter.THIS_WEEK) },
                        label = { Text("This Week") }
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.timeFilter == TimeFilter.THIS_MONTH,
                        onClick = { viewModel.updateTimeFilter(TimeFilter.THIS_MONTH) },
                        label = { Text("This Month") }
                    )
                }
                items(uiState.categories) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategoryId == cat.id,
                        onClick = { viewModel.updateCategoryFilter(cat.id) },
                        label = { Text(cat.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No transactions found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    grouped.forEach { (dateHeader, txns) ->
                        item {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(txns, key = { it.id }) { txn ->
                            val category = uiState.categories.find { it.id == txn.categoryId }
                            TransactionRow(
                                txn = txn,
                                categoryName = category?.name ?: "Uncategorized",
                                categoryIcon = category?.icon ?: "category",
                                categoryColor = category?.color ?: 0xFF9E9E9E,
                                onClick = { onTransactionClick(txn.id) }
                            )
                        }
                    }
                }

                // Summary bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${uiState.transactions.size} transactions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Total: ${CurrencyFormatter.format(viewModel.getTotalAmount())}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    txn: com.finbaby.app.data.db.entity.TransactionEntity,
    categoryName: String,
    categoryIcon: String,
    categoryColor: Long,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryIcon(
                iconName = categoryIcon,
                color = categoryColor,
                size = 40.dp,
                iconSize = 22.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (txn.note.isNotBlank()) {
                    Text(
                        text = txn.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (txn.type == "expense") "-" else "+"}${CurrencyFormatter.format(txn.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (txn.type == "expense") ExpenseRed else IncomeGreen
                )
                Text(
                    text = DateUtils.formatTime(txn.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
