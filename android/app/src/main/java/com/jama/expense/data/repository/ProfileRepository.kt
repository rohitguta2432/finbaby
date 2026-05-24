package com.jama.expense.data.repository

import com.jama.expense.data.db.dao.ProfileDao
import com.jama.expense.data.db.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao
) {
    fun getProfile(): Flow<ProfileEntity?> = profileDao.getProfile()
    suspend fun getProfileSync(): ProfileEntity? = profileDao.getProfileSync()
    suspend fun insert(profile: ProfileEntity) = profileDao.insert(profile)
    suspend fun update(profile: ProfileEntity) = profileDao.update(profile)
}
