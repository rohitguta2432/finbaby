package com.finbaby.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BarLight = Color(0xFF2DD4BF)
private val BarMid = Color(0xFF14B8A6)
private val BarDark = Color(0xFF0D9488)
private val StarGold = Color(0xFFFBBF24)

@Composable
fun FinBabyLogo(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stacked bars icon + sparkle
        Canvas(modifier = Modifier.size(width = 56.dp, height = 56.dp)) {
            drawBarsAndStar()
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Wordmark + tagline
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "FinBaby",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "FINANCIAL ADVISOR",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 3.sp
                ),
                color = BarDark
            )
        }
    }
}

private fun DrawScope.drawBarsAndStar() {
    val w = size.width
    val h = size.height

    val barHeight = h * 0.2f
    val radius = barHeight / 2f
    val gap = h * 0.06f

    // Top bar (widest, lightest)
    val topY = h * 0.08f
    drawRoundRect(
        color = BarLight,
        topLeft = Offset(0f, topY),
        size = Size(w * 0.88f, barHeight),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Middle bar
    val midY = topY + barHeight + gap
    drawRoundRect(
        color = BarMid,
        topLeft = Offset(w * 0.06f, midY),
        size = Size(w * 0.76f, barHeight),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Bottom bar (narrowest, darkest)
    val botY = midY + barHeight + gap
    drawRoundRect(
        color = BarDark,
        topLeft = Offset(w * 0.12f, botY),
        size = Size(w * 0.64f, barHeight),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Star sparkle (top-right area)
    val starCx = w * 0.85f
    val starCy = h * 0.12f
    val starSize = w * 0.12f
    drawStar(starCx, starCy, starSize, StarGold)
}

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
    val path = Path().apply {
        moveTo(cx, cy - r)          // top
        lineTo(cx + r * 0.3f, cy - r * 0.3f)
        lineTo(cx + r, cy)          // right
        lineTo(cx + r * 0.3f, cy + r * 0.3f)
        lineTo(cx, cy + r)          // bottom
        lineTo(cx - r * 0.3f, cy + r * 0.3f)
        lineTo(cx - r, cy)          // left
        lineTo(cx - r * 0.3f, cy - r * 0.3f)
        close()
    }
    drawPath(path, color)
}
