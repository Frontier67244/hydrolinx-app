package com.mahendra.android.hydrolinx.data.repository

import com.mahendra.android.hydrolinx.data.local.dao.HydrationSessionDao
import com.mahendra.android.hydrolinx.data.local.dao.PointWalletDao
import com.mahendra.android.hydrolinx.data.local.dao.UserProfileDao
import com.mahendra.android.hydrolinx.data.local.dao.WaterLocationDao
import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import com.mahendra.android.hydrolinx.data.local.entity.PointWalletEntity
import com.mahendra.android.hydrolinx.data.local.entity.UserProfileEntity
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity
import com.mahendra.android.hydrolinx.data.local.seed.DummyLocationSeeder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

/**
 * Implementasi repository satu pintu.
 * Konstanta cap ditulis lokal (bukan import RedeemRules) agar layer data tidak bergantung ke feature.
 */
class LocalRepositoryImpl(
    private val waterLocationDao: WaterLocationDao,
    private val hydrationSessionDao: HydrationSessionDao,
    private val pointWalletDao: PointWalletDao,
    private val userProfileDao: UserProfileDao,
) : LocalRepository {

    private val seeder = DummyLocationSeeder(waterLocationDao)

    // --- Maps ---

    override suspend fun getAllWaterLocations(): List<WaterLocationEntity> =
        waterLocationDao.getAll()

    override suspend fun ensureLocationsSeeded() {
        seeder.seedIfEmpty()
    }

    // --- Hydration ---

    override suspend fun getUserProfile(): UserProfileEntity? = userProfileDao.getSingleton()

    override fun observeUserProfile(): Flow<UserProfileEntity?> = userProfileDao.observeSingleton()

    override suspend fun setTargetMl(targetMl: Int) {
        val current = userProfileDao.getSingleton() ?: UserProfileEntity()
        userProfileDao.upsert(current.copy(targetMl = targetMl))
    }

    override suspend fun getTodaySessions(): List<HydrationSessionEntity> {
        val (start, end) = todayEpochRange()
        return hydrationSessionDao.getSessionsInRange(start, end)
    }

    override fun observeTodaySessions(): Flow<List<HydrationSessionEntity>> {
        val (start, end) = todayEpochRange()
        return hydrationSessionDao.observeSessionsInRange(start, end)
    }

    override suspend fun insertHydrationSession(amountMl: Int, pointsAwarded: Int, epochMillis: Long) {
        hydrationSessionDao.insert(
            HydrationSessionEntity(
                amountMl = amountMl,
                pointsAwarded = pointsAwarded,
                sessionTimestampEpochMillis = epochMillis,
            )
        )
    }

    // --- Points ---

    override fun observePointWallet(): Flow<PointWalletEntity> =
        pointWalletDao.observeSingleton().map { it ?: PointWalletEntity() }

    override suspend fun ensureDemoPointWalletSeeded() {
        val current = pointWalletDao.getSingleton()
        val shouldSeed = current == null ||
            (
                current.totalRedeem == 0 &&
                    current.activePoints == 0 &&
                    current.bankPoints == 0 &&
                    current.tokenCount == 0 &&
                    current.lastRedeemDateIso == null
                )

        if (shouldSeed) {
            pointWalletDao.upsert(PointWalletEntity(activePoints = DEMO_INITIAL_POINTS))
        }
    }

    override suspend fun addPoints(amount: Int) {
        if (amount <= 0) return
        val current = pointWalletDao.getSingleton() ?: PointWalletEntity()
        val roomInActive = (DAILY_ACTIVE_POINT_CAP - current.activePoints).coerceAtLeast(0)
        val toActive = amount.coerceAtMost(roomInActive)
        val leftover = amount - toActive
        val roomInBank = (BANK_POINT_CAP - current.bankPoints).coerceAtLeast(0)
        val toBank = leftover.coerceAtMost(roomInBank)
        pointWalletDao.upsert(
            current.copy(
                activePoints = current.activePoints + toActive,
                bankPoints = current.bankPoints + toBank,
            )
        )
    }

    override suspend fun applyRedeem(
        newActive: Int,
        newBank: Int,
        tokenDelta: Int,
        redeemDateIso: String,
    ) {
        val current = pointWalletDao.getSingleton() ?: PointWalletEntity()
        val newTokenCount = (current.tokenCount + tokenDelta).coerceAtMost(DAILY_TOKEN_CAP)
        pointWalletDao.upsert(
            current.copy(
                activePoints = newActive,
                bankPoints = newBank,
                tokenCount = newTokenCount,
                totalRedeem = current.totalRedeem + 1,
                lastRedeemDateIso = redeemDateIso,
            )
        )
    }

    override suspend fun consumeToken() {
        val current = pointWalletDao.getSingleton() ?: return
        if (current.tokenCount <= 0) return
        pointWalletDao.upsert(current.copy(tokenCount = current.tokenCount - 1))
    }

    // --- Helpers ---

    private fun todayEpochRange(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
        val startOfTomorrow = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return startOfToday to startOfTomorrow
    }

    companion object {
        private const val DAILY_ACTIVE_POINT_CAP = 100
        private const val BANK_POINT_CAP = 300
        private const val DAILY_TOKEN_CAP = 1
        private const val DEMO_INITIAL_POINTS = 100
    }
}
