package com.finbaby.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.finbaby.app.ui.theme.BudgetDanger
import com.finbaby.app.ui.theme.BudgetSafe
import com.finbaby.app.ui.theme.BudgetWarning
import com.finbaby.app.util.CurrencyFormatter

@Composable
fun BudgetProgressBar(
    spent: Double,
    limit: Double,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val percentage = if (limit > 0) (spent / limit).coerceIn(0.0, 1.0) else 0.0
    val percentInt = (percentage * 100).toInt()

    val progressColor by animateColorAsState(
        targetValue = when {
            percentInt > 90 -> BudgetDanger
            percentInt > 70 -> BudgetWarning
            else -> BudgetSafe
        },
        animationSpec = tween(300),
        label = "progressColor"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(600),
        label = "progress"
    )

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        if (showLabel) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${CurrencyFormatter.format(spent)} / ${CurrencyFormatter.format(limit)} ($percentInt%)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
