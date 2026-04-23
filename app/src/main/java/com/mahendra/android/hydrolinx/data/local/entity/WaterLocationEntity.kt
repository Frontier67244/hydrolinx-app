package com.mahendra.android.hydrolinx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_locations")
data class WaterLocationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isOpen: Boolean,
    val hasCapacity: Boolean,
)
