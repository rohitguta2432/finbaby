package com.finbaby.app.data.repository

import com.finbaby.app.data.db.dao.CategoryTotal
import com.finbaby.app.data.db.dao.DailyTotal
import com.finbaby.app.data.db.dao.TransactionDao
import com.finbaby.app.data.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    suspend fun insert(transaction: TransactionEntity): Long = transactionDao.insert(transaction)
    suspend fun update(transaction: TransactionEntity) = transactionDao.update(transaction)
    suspend fun delete(transaction: TransactionEntity) = transactionDao.delete(transaction)
    suspend fun deleteById(id: Long) = transactionDao.deleteById(id)
    suspend fun getById(id: Long): TransactionEntity? = transactionDao.getById(id)

    fun getAllFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllFlow()
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionEntity>> = transactionDao.getByDateRange(start, end)
    fun getTotalExpense(start: Long, end: Long): Flow<Double?> = transactionDao.getTotalExpense(start, end)
    fun getTotalIncome(start: Long, end: Long): Flow<Double?> = transactionDao.getTotalIncome(start, end)
    fun getTotalExpenseByCategory(categoryId: Long, start: Long, end: Long): Flow<Double?> = transactionDao.getTotalExpenseByCategory(categoryId, start, end)
    fun getExpenseByCategoryGrouped(start: Long, end: Long): Flow<List<CategoryTotal>> = transactionDao.getExpenseByCategoryGrouped(start, end)
    fun getDailyExpenses(start: Long, end: Long): Flow<List<DailyTotal>> = transactionDao.getDailyExpenses(start, end)
    fun search(query: String): Flow<List<TransactionEntity>> = transactionDao.search(query)

    suspend fun getRecurringTransactions(): List<TransactionEntity> = transactionDao.getRecurringTransactions()
    suspend fun getTotalExpenseSync(start: Long, end: Long): Double? = transactionDao.getTotalExpenseSync(start, end)
}
