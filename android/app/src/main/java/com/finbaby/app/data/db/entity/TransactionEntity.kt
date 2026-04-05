package com.finbaby.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("date"), Index("type")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String, // "expense" or "income"
    val categoryId: Long?,
    val accountType: String, // "cash", "bank", "upi", "credit_card"
    val note: String = "",
    val date: Long, // epoch millis
    val isRecurring: Boolean = false,
    val recurringPeriod: String? = null, // "daily", "weekly", "monthly"
    val createdAt: Long = System.currentTimeMillis()
)
