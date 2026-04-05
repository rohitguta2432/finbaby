package com.finbaby.app.data.repository

import com.finbaby.app.data.db.dao.BudgetDao
import com.finbaby.app.data.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao
) {
    fun getBudgetsForMonth(month: String): Flow<List<BudgetEntity>> = budgetDao.getBudgetsForMonth(month)
    suspend fun getBudgetForCategory(categoryId: Long, month: String): BudgetEntity? = budgetDao.getBudgetForCategory(categoryId, month)
    suspend fun insert(budget: BudgetEntity) = budgetDao.insert(budget)
    suspend fun update(budget: BudgetEntity) = budgetDao.update(budget)
    suspend fun delete(budget: BudgetEntity) = budgetDao.delete(budget)
    fun getTotalBudget(month: String): Flow<Double?> = budgetDao.getTotalBudget(month)
}
