package com.finbaby.app.data.db.dao

import androidx.room.*
import com.finbaby.app.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE isEnabled = 1 ORDER BY sortOrder")
    fun getEnabledCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY isDefault DESC, sortOrder")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
