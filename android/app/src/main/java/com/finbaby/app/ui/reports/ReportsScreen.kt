package com.finbaby.app.ui.reports

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.finbaby.app.navigation.Routes
import com.finbaby.app.ui.components.FinBabyBottomNav
import com.finbaby.app.ui.reports.components.DailyBarChart
import com.finbaby.app.ui.reports.components.DonutChart
import com.finbaby.app.ui.reports.components.DonutSlice
import com.finbaby.app.ui.theme.BudgetSafe
import com.finbaby.app.ui.theme.ExpenseRed
import com.finbaby.app.ui.theme.IncomeGreen
import com.finbaby.app.util.CurrencyFormatter

private val chartColors = listOf(
    Color(0xFF00685D),
    Color(0xFFFFB300),
    Color(0xFF7B5500),
    Color(0xFF008376),
    Color(0xFFBA1A1A),
    Color(0xFF2E7D32),
    Color(0xFF6D7A77),
    Color(0xFF70D8C8),
    Color(0xFF9B6B00),
    Color(0xFFFFBA38),
)

@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            FinBabyBottomNav(
                currentRoute = Routes.REPORTS,
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
            MonthSelector(
                monthDisplay = state.monthDisplayName,
                onPrevious = viewModel::goToPreviousMonth,
                onNext = viewModel::goToNextMonth
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    label = "Income",
                    amount = state.totalIncome,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Expense",
                    amount = state.totalExpense,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Balance",
                    amount = state.balance,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Spending by Category
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                val slices = state.categorySpending.mapIndexed { index, cs ->
                    DonutSlice(
                        label = cs.categoryName,
                        amount = cs.total,
                        color = chartColors[index % chartColors.size]
                    )
                }
                DonutChart(
                    slices = slices,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Daily Spending
            Text(
                text = "Daily Spending",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                DailyBarChart(
                    dailyTotals = state.dailyTotals,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Trend vs Last Month
            Text(
                text = "Trend vs Last Month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            state.categorySpending.forEach { cs ->
                TrendRow(
                    categoryName = cs.categoryName,
                    currentAmount = cs.total,
                    previousAmount = cs.previousTotal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MonthSelector(
    monthDisplay: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = monthDisplay,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = CurrencyFormatter.formatCompact(kotlin.math.abs(amount)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun TrendRow(
    categoryName: String,
    currentAmount: Double,
    previousAmount: Double
) {
    val diff = currentAmount - previousAmount
    val percentChange = if (previousAmount > 0) {
        ((diff / previousAmount) * 100).toInt()
    } else if (currentAmount > 0) {
        100
    } else {
        0
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = CurrencyFormatter.format(currentAmount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val trendColor = when {
                    diff > 0 -> ExpenseRed
                    diff < 0 -> BudgetSafe
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                val trendIcon = when {
                    diff > 0 -> Icons.AutoMirrored.Filled.TrendingUp
                    diff < 0 -> Icons.AutoMirrored.Filled.TrendingDown
                    else -> Icons.Filled.Remove
                }
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if (diff > 0) "+" else ""}$percentChange%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = trendColor
                )
            }
        }
    }
}
