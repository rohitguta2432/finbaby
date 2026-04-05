package com.finbaby.app.ui.reports.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.finbaby.app.data.db.dao.DailyTotal
import com.finbaby.app.ui.theme.BudgetWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyBarChart(
    dailyTotals: List<DailyTotal>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val amberColor = BudgetWarning
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = MaterialTheme.typography.labelSmall
    val textMeasurer = rememberTextMeasurer()

    val average = remember(dailyTotals) {
        if (dailyTotals.isEmpty()) 0.0 else dailyTotals.sumOf { it.total } / dailyTotals.size
    }
    val maxTotal = remember(dailyTotals) {
        dailyTotals.maxOfOrNull { it.total } ?: 1.0
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 4.dp)
    ) {
        if (dailyTotals.isEmpty()) return@Canvas

        val chartHeight = size.height - 24.dp.toPx()
        val barWidth = ((size.width - 8.dp.toPx()) / dailyTotals.size) - 2.dp.toPx()
        val gap = 2.dp.toPx()
        val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

        dailyTotals.forEachIndexed { index, daily ->
            val barHeight = (daily.total / maxTotal * chartHeight).toFloat()
            val x = index * (barWidth + gap) + 4.dp.toPx()
            val y = chartHeight - barHeight

            val barColor = if (daily.total > average) amberColor else primaryColor

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = cornerRadius
            )
        }

        // Average dashed line
        if (average > 0) {
            val avgY = (chartHeight - (average / maxTotal * chartHeight)).toFloat()
            drawLine(
                color = outlineColor,
                start = Offset(0f, avgY),
                end = Offset(size.width, avgY),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(8.dp.toPx(), 6.dp.toPx()),
                    0f
                )
            )
        }

        // Day labels (show every few days to avoid overlap)
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val step = when {
            dailyTotals.size > 20 -> 5
            dailyTotals.size > 10 -> 3
            else -> 1
        }
        dailyTotals.forEachIndexed { index, daily ->
            if (index % step == 0) {
                val x = index * (barWidth + gap) + 4.dp.toPx()
                val dayLabel = dayFormat.format(Date(daily.date))
                val textResult = textMeasurer.measure(
                    text = dayLabel,
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = textResult,
                    topLeft = Offset(
                        x + (barWidth - textResult.size.width) / 2f,
                        chartHeight + 4.dp.toPx()
                    )
                )
            }
        }
    }
}
