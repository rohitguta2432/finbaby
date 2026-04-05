package com.finbaby.app.data.db.dao

import androidx.room.*
import com.finbaby.app.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND month = :month LIMIT 1")
    suspend fun getBudgetForCategory(categoryId: Long, month: String): BudgetEntity?

    @Query("SELECT SUM(limitAmount) FROM budgets WHERE month = :month")
    fun getTotalBudget(month: String): Flow<Double?>
}
