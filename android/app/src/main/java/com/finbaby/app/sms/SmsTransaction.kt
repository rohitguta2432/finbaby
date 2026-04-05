package com.finbaby.app.sms

data class SmsTransaction(
    val amount: Double,
    val type: String, // "expense" or "income"
    val merchant: String?,
    val bank: String,
    val accountType: String, // "bank", "upi", "credit_card"
    val categoryId: Long?,
    val note: String,
    val date: Long,
    val smsId: String // to prevent duplicates
)
