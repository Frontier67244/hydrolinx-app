package com.mahendra.android.hydrolinx.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mahendra.android.hydrolinx.data.local.entity.PointWalletEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointWalletDao {

    @Query("SELECT * FROM point_wallet WHERE id = 0 LIMIT 1")
    suspend fun getSingleton(): PointWalletEntity?

    @Query("SELECT * FROM point_wallet WHERE id = 0 LIMIT 1")
    fun observeSingleton(): Flow<PointWalletEntity?>

    @Upsert
    suspend fun upsert(wallet: PointWalletEntity)
}
