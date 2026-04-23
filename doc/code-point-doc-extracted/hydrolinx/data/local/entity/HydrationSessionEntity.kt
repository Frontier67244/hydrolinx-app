package com.mahendra.android.hydrolinx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hydration_sessions")
data class HydrationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amountMl: Int,
    val pointsAwarded: Int,
    val sessionTimestampEpochMillis: Long,
)
