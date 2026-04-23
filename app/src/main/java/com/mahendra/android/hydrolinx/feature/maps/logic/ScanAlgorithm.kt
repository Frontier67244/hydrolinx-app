package com.mahendra.android.hydrolinx.feature.maps.logic

import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Algoritma scan radius bertahap. Murni Kotlin, tanpa dependensi Android.
 *
 * Cara kerja:
 *   1. Mulai dari INITIAL_RADIUS_M.
 *   2. Filter kandidat yang jaraknya <= radius saat ini (kecuali yang di excludedIds).
 *   3. Kalau ada hasil -> return list terurut terdekat dulu.
 *   4. Kalau kosong -> tambahkan STEP_M ke radius dan ulangi, sampai MAX_RADIUS_M.
 *
 * Jarak dikomputasi pakai equirectangular approximation — cukup akurat untuk
 * orde ratusan meter sampai beberapa km dan lebih ringan daripada Haversine.
 */
object ScanAlgorithm {

    const val INITIAL_RADIUS_M: Int = 100
    const val STEP_M: Int = 100
    const val MAX_RADIUS_M: Int = 2000
    const val DELAY_MS: Long = 800L
    const val STAGE_PAUSE_MS: Long = 900L

    val STAGE_MAX_RADII_M: List<Int> = listOf(500, 1000, 2000)

    private const val EARTH_RADIUS_M: Double = 6_371_000.0

    data class Candidate<T>(
        val item: T,
        val distanceM: Double,
    )

    /**
     * Hitung jarak antar dua titik koordinat dalam meter.
     */
    fun distanceMeters(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val latMidRad = Math.toRadians((lat1 + lat2) / 2.0)
        val dLatRad = Math.toRadians(lat2 - lat1)
        val dLonRad = Math.toRadians(lon2 - lon1)
        val x = dLonRad * cos(latMidRad)
        return sqrt(x * x + dLatRad * dLatRad) * EARTH_RADIUS_M
    }

    /**
     * Temukan semua kandidat dalam radius yang diberikan, terurut berdasarkan jarak.
     */
    fun <T> findWithin(
        userLat: Double,
        userLon: Double,
        radiusM: Int,
        candidates: List<T>,
        excludedIds: Set<String>,
        idOf: (T) -> String,
        latOf: (T) -> Double,
        lonOf: (T) -> Double,
    ): List<Candidate<T>> =
        candidates
            .asSequence()
            .filter { idOf(it) !in excludedIds }
            .map { Candidate(it, distanceMeters(userLat, userLon, latOf(it), lonOf(it))) }
            .filter { it.distanceM <= radiusM }
            .sortedBy { it.distanceM }
            .toList()

    fun filterLocationsInRadius(
        userLat: Double,
        userLon: Double,
        locations: List<WaterLocationEntity>,
        radius: Double,
        excludedIds: Set<String>,
    ): List<Candidate<WaterLocationEntity>> =
        findWithin(
            userLat = userLat,
            userLon = userLon,
            radiusM = radius.toInt(),
            candidates = locations,
            excludedIds = excludedIds,
            idOf = { it.id },
            latOf = { it.latitude },
            lonOf = { it.longitude },
        )
}
