package com.finbaby.app.sms

import com.finbaby.app.util.CategoryMatcher

/**
 * Step 2: Regex Rule Engine
 * Parses Indian bank SMS messages to extract transaction details.
 *
 * Common Indian bank SMS patterns:
 * - "INR 1,234.56 debited from A/c XX1234 on 05-Apr-26. Info: UPI/Swiggy"
 * - "Rs.500.00 spent on HDFC CC ending 1234 at AMAZON on 05-Apr"
 * - "You've received Rs 25,000 in your A/c XX5678"
 * - "Txn of Rs 350.00 done via UPI. Ref: 412345678"
 * - "Amt Debited: INR 2,500.00 A/C: XX1234"
 * - "Rs 1500 credited to your account"
 */
object SmsRegexEngine {

    // Amount patterns (handles INR, Rs, Rs., ₹ with optional commas and decimals)
    private val amountPatterns = listOf(
        Regex("""(?:INR|Rs\.?|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:amount|amt)[:\s]*(?:INR|Rs\.?|₹)?\s*([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""([0-9,]+(?:\.[0-9]{1,2})?)\s*(?:debited|credited|spent|received)""", RegexOption.IGNORE_CASE),
    )

    // Debit indicators
    private val debitPatterns = listOf(
        Regex("""debit""", RegexOption.IGNORE_CASE),
        Regex("""spent""", RegexOption.IGNORE_CASE),
        Regex("""withdrawn""", RegexOption.IGNORE_CASE),
        Regex("""purchase""", RegexOption.IGNORE_CASE),
        Regex("""payment""", RegexOption.IGNORE_CASE),
        Regex("""paid""", RegexOption.IGNORE_CASE),
        Regex("""sent""", RegexOption.IGNORE_CASE),
        Regex("""txn""", RegexOption.IGNORE_CASE),
        Regex("""charged""", RegexOption.IGNORE_CASE),
    )

    // Credit indicators
    private val creditPatterns = listOf(
        Regex("""credit""", RegexOption.IGNORE_CASE),
        Regex("""received""", RegexOption.IGNORE_CASE),
        Regex("""deposited""", RegexOption.IGNORE_CASE),
        Regex("""refund""", RegexOption.IGNORE_CASE),
        Regex("""cashback""", RegexOption.IGNORE_CASE),
        Regex("""salary""", RegexOption.IGNORE_CASE),
    )

    // Merchant/Info extraction
    private val merchantPatterns = listOf(
        Regex("""(?:at|to|from|info[:\s]*|towards)\s+([A-Za-z0-9\s&.'-]+?)(?:\s+on|\s+ref|\s+txn|\.|\s*$)""", RegexOption.IGNORE_CASE),
        Regex("""UPI[/-]([A-Za-z0-9\s.@-]+?)(?:\s+on|\s+ref|\.|\s*$)""", RegexOption.IGNORE_CASE),
        Regex("""VPA\s+([a-z0-9@.-]+)""", RegexOption.IGNORE_CASE),
    )

    // Account number
    private val accountPattern = Regex("""(?:A/?[Cc]|acct?|account)[:\s]*(?:no\.?\s*)?[Xx*]*(\d{3,6})""", RegexOption.IGNORE_CASE)

    // UPI detection
    private val upiPattern = Regex("""UPI|IMPS|NEFT|RTGS""", RegexOption.IGNORE_CASE)

    data class ParseResult(
        val amount: Double?,
        val type: String?, // "expense" or "income"
        val merchant: String?,
        val accountLast4: String?,
        val isUpi: Boolean,
        val categoryId: Long?
    )

    fun parse(smsBody: String): ParseResult? {
        val amount = extractAmount(smsBody) ?: return null
        val type = detectType(smsBody) ?: return null
        val merchant = extractMerchant(smsBody)
        val accountLast4 = accountPattern.find(smsBody)?.groupValues?.get(1)
        val isUpi = upiPattern.containsMatchIn(smsBody)

        // Auto-categorize from merchant name
        val categoryId = merchant?.let { CategoryMatcher.suggestCategoryId(it) }

        return ParseResult(
            amount = amount,
            type = type,
            merchant = merchant?.trim()?.take(50), // cap length
            accountLast4 = accountLast4,
            isUpi = isUpi,
            categoryId = categoryId
        )
    }

    private fun extractAmount(body: String): Double? {
        for (pattern in amountPatterns) {
            val match = pattern.find(body)
            if (match != null) {
                val amountStr = match.groupValues[1].replace(",", "")
                val amount = amountStr.toDoubleOrNull()
                if (amount != null && amount > 0 && amount < 10_000_000) {
                    return amount
                }
            }
        }
        return null
    }

    private fun detectType(body: String): String? {
        val debitScore = debitPatterns.count { it.containsMatchIn(body) }
        val creditScore = creditPatterns.count { it.containsMatchIn(body) }

        return when {
            debitScore > creditScore -> "expense"
            creditScore > debitScore -> "income"
            debitScore > 0 -> "expense" // tie-break: default to expense
            else -> null // can't determine
        }
    }

    private fun extractMerchant(body: String): String? {
        for (pattern in merchantPatterns) {
            val match = pattern.find(body)
            if (match != null) {
                val merchant = match.groupValues[1].trim()
                if (merchant.length in 2..50 && !merchant.matches(Regex("""^\d+$"""))) {
                    return merchant
                }
            }
        }
        return null
    }
}
