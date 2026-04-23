package com.mahendra.android.hydrolinx.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mahendra.android.hydrolinx.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE id = 0 LIMIT 1")
    suspend fun getSingleton(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = 0 LIMIT 1")
    fun observeSingleton(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsert(profile: UserProfileEntity)
}
