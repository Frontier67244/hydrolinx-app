package com.mahendra.android.hydrolinx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_wallet")
data class PointWalletEntity(
    @PrimaryKey val id: Int = 0,
    val totalRedeem: Int = 0,
    val activePoints: Int = 0,
    val bankPoints: Int = 0,
    val tokenCount: Int = 0,
    val lastRedeemDateIso: String? = null,
)
