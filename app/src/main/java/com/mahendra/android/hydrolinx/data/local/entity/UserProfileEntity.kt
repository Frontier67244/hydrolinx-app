package com.mahendra.android.hydrolinx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 0,
    val targetMl: Int = 0,
    val displayName: String? = null,
)
