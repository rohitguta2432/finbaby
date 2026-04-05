package com.finbaby.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Material icon name
    val color: Long, // Color as ARGB long
    val isDefault: Boolean = true,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0,
    val budgetType: String = "needs" // "needs", "wants", "savings"
)
