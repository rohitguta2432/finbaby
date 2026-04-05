package com.finbaby.app.util

import java.text.DecimalFormat

object CurrencyFormatter {

    private val formatter = DecimalFormat("#,##,##0")
    private val formatterWithDecimals = DecimalFormat("#,##,##0.00")

    fun format(amount: Double, showDecimals: Boolean = false): String {
        val formatted = if (showDecimals) {
            formatterWithDecimals.format(amount)
        } else {
            formatter.format(amount)
        }
        return "₹$formatted"
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 10_000_000 -> "₹${formatter.format(amount / 10_000_000)}Cr"
            amount >= 100_000 -> "₹${formatter.format(amount / 100_000)}L"
            amount >= 1_000 -> "₹${formatter.format(amount / 1_000)}K"
            else -> format(amount)
        }
    }
}
