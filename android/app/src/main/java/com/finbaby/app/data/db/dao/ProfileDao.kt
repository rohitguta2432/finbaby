package com.finbaby.app.data.db.dao

import androidx.room.*
import com.finbaby.app.data.db.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)

    @Update
    suspend fun update(profile: ProfileEntity)

    @Query("SELECT * FROM profile WHERE id = 1")
    fun getProfile(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE id = 1")
    suspend fun getProfileSync(): ProfileEntity?
}
