package com.finbaby.app.data.repository

import com.finbaby.app.data.db.dao.CategoryDao
import com.finbaby.app.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getEnabledCategories(): Flow<List<CategoryEntity>> = categoryDao.getEnabledCategories()
    fun getAllCategories(): Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun insert(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun update(category: CategoryEntity) = categoryDao.update(category)
    suspend fun delete(category: CategoryEntity) = categoryDao.delete(category)
    suspend fun getCount(): Int = categoryDao.getCount()
}
