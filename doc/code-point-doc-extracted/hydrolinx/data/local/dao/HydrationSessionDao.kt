package com.mahendra.android.hydrolinx.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HydrationSessionDao {

    @Insert
    suspend fun insert(session: HydrationSessionEntity): Long

    @Query(
        "SELECT * FROM hydration_sessions " +
            "WHERE sessionTimestampEpochMillis >= :startEpoch " +
            "AND sessionTimestampEpochMillis < :endEpoch " +
            "ORDER BY sessionTimestampEpochMillis DESC"
    )
    suspend fun getSessionsInRange(startEpoch: Long, endEpoch: Long): List<HydrationSessionEntity>

    @Query(
        "SELECT * FROM hydration_sessions " +
            "WHERE sessionTimestampEpochMillis >= :startEpoch " +
            "AND sessionTimestampEpochMillis < :endEpoch " +
            "ORDER BY sessionTimestampEpochMillis DESC"
    )
    fun observeSessionsInRange(startEpoch: Long, endEpoch: Long): Flow<List<HydrationSessionEntity>>
}
