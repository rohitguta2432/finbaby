package com.finbaby.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val id: Int = 1, // Single profile
    val salary: Double,
    val salaryDate: Int, // Day of month (1-31)
    val needsPercent: Int = 50,
    val wantsPercent: Int = 30,
    val savingsPercent: Int = 20,
    val currency: String = "₹",
    val isOnboarded: Boolean = false
)
