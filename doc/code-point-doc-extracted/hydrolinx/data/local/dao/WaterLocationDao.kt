package com.mahendra.android.hydrolinx.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity

@Dao
interface WaterLocationDao {

    @Query("SELECT * FROM water_locations")
    suspend fun getAll(): List<WaterLocationEntity>

    @Query("SELECT COUNT(*) FROM water_locations")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<WaterLocationEntity>)
}
