package com.finbaby.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.finbaby.app.data.db.entity.CategoryEntity
import com.finbaby.app.data.db.entity.TransactionEntity
import com.finbaby.app.ui.components.CategoryIcon
import com.finbaby.app.ui.theme.ExpenseRed
import com.finbaby.app.ui.theme.IncomeGreen
import com.finbaby.app.util.CurrencyFormatter
import com.finbaby.app.util.DateUtils

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    category: CategoryEntity?,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == "expense"
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val amountPrefix = if (isExpense) "-" else "+"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryIcon(
                iconName = category?.icon ?: "category",
                color = category?.color ?: 0xFF6D7A77
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category?.name ?: "Uncategorized",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (category != null) {
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = when (category.budgetType) {
                                        "needs" -> "Monthly Bills"
                                        "wants" -> "Daily Spending"
                                        "savings" -> "Money Saved"
                                        else -> category.budgetType
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null
                        )
                    }
                    Text(
                        text = DateUtils.formatTime(transaction.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${CurrencyFormatter.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            }
        }
    }
}
