package com.finbaby.app.sms

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Full SMS → Transaction Pipeline:
 *
 * SMS Reader (ContentProvider)
 *     ↓
 * Sender ID Lookup (HashMap) → known bank? → get bank info
 *     ↓
 * Regex Rule Engine → extract amount, type, merchant
 *     ↓
 * Category Matcher → known merchant? → auto-categorize
 *     ↓
 * Fallback: "Uncategorized" (categoryId = null)
 */
@Singleton
class SmsTransactionParser @Inject constructor(
    private val smsReader: SmsReader
) {
    /**
     * Read SMS and parse into transactions.
     * @param sinceTimestamp Only process SMS after this time
     * @param existingSmsIds Set of SMS IDs already imported (to prevent duplicates)
     */
    fun parseNewTransactions(
        sinceTimestamp: Long = 0,
        existingSmsIds: Set<String> = emptySet()
    ): List<SmsTransaction> {
        val rawMessages = smsReader.readFinancialSms(sinceTimestamp)
        val transactions = mutableListOf<SmsTransaction>()

        for (sms in rawMessages) {
            // Skip already imported
            if (sms.id in existingSmsIds) continue

            // Step 1: Sender ID Lookup
            val bankInfo = BankSenderMap.lookup(sms.sender) ?: continue

            // Step 2: Regex Rule Engine
            val parsed = SmsRegexEngine.parse(sms.body) ?: continue

            // Step 3: Build transaction
            val accountType = when {
                parsed.isUpi -> "upi"
                bankInfo.defaultAccountType == "credit_card" -> "credit_card"
                else -> "bank"
            }

            val note = buildNote(bankInfo.bankName, parsed.merchant, sms.body)

            transactions.add(
                SmsTransaction(
                    amount = parsed.amount ?: continue,
                    type = parsed.type ?: continue,
                    merchant = parsed.merchant,
                    bank = bankInfo.bankName,
                    accountType = accountType,
                    categoryId = parsed.categoryId, // null = uncategorized (fallback)
                    note = note,
                    date = sms.date,
                    smsId = sms.id
                )
            )
        }

        return transactions
    }

    private fun buildNote(bank: String, merchant: String?, rawBody: String): String {
        return when {
            merchant != null -> "$bank — $merchant"
            else -> "$bank — ${rawBody.take(60)}"
        }
    }
}
