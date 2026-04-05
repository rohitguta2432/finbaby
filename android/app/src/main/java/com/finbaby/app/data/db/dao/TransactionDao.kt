package com.finbaby.app.data.db.dao

import androidx.room.*
import com.finbaby.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpense(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income' AND date BETWEEN :startDate AND :endDate")
    fun getTotalIncome(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense' AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpenseByCategory(categoryId: Long, startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT categoryId, SUM(amount) as total
        FROM transactions
        WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
        ORDER BY total DESC
    """)
    fun getExpenseByCategoryGrouped(startDate: Long, endDate: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE note LIKE '%' || :query || '%' ORDER BY date DESC")
    fun search(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isRecurring = 1")
    suspend fun getRecurringTransactions(): List<TransactionEntity>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalExpenseSync(startDate: Long, endDate: Long): Double?

    @Query("""
        SELECT date, SUM(amount) as total
        FROM transactions
        WHERE type = 'expense' AND date BETWEEN :startDate AND :endDate
        GROUP BY date / 86400000
        ORDER BY date
    """)
    fun getDailyExpenses(startDate: Long, endDate: Long): Flow<List<DailyTotal>>
}

data class CategoryTotal(
    val categoryId: Long?,
    val total: Double
)

data class DailyTotal(
    val date: Long,
    val total: Double
)
