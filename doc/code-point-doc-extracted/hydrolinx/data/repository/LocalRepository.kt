package com.mahendra.android.hydrolinx.data.repository

import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import com.mahendra.android.hydrolinx.data.local.entity.PointWalletEntity
import com.mahendra.android.hydrolinx.data.local.entity.UserProfileEntity
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Satu-satunya pintu akses ke data lokal untuk seluruh ViewModel.
 * Semua rule konsistensi (overflow, cap) di-enforce di sini, bukan di ViewModel.
 */
interface LocalRepository {

    // Maps
    suspend fun getAllWaterLocations(): List<WaterLocationEntity>
    suspend fun ensureLocationsSeeded()

    // Hydration
    suspend fun getUserProfile(): UserProfileEntity?
    fun observeUserProfile(): Flow<UserProfileEntity?>
    suspend fun setTargetMl(targetMl: Int)
    suspend fun getTodaySessions(): List<HydrationSessionEntity>
    fun observeTodaySessions(): Flow<List<HydrationSessionEntity>>
    suspend fun insertHydrationSession(amountMl: Int, pointsAwarded: Int, epochMillis: Long)

    // Points
    fun observePointWallet(): Flow<PointWalletEntity>
    suspend fun addPoints(amount: Int)
    suspend fun applyRedeem(
        newActive: Int,
        newBank: Int,
        tokenDelta: Int,
        redeemDateIso: String,
    )
    suspend fun consumeToken()
}
