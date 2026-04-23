package com.mahendra.android.hydrolinx.feature.maps.state

import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity

/**
 * Sub-mode yang dipakai Maps tab.
 * Home   — tampil info card & CTA EXPLORE.
 * Explore — scan aktif atau hasil scan (circle + pin).
 * Notify — panel notifikasi shifting.
 */
enum class MapsMode { Home, Explore, Notify }

enum class TransportMode { Walk, Motor, Car }

data class FoundLocation(
    val entity: WaterLocationEntity,
    val distanceM: Double,
)

data class ShiftNotificationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val triggerAtMs: Long,
)

/**
 * Single source of truth untuk layar Maps.
 * Dibuat seluas mungkin agar UI (popup/sheet/empty state) hanya membaca flag di sini.
 */
data class MapsUiState(
    val mode: MapsMode = MapsMode.Home,

    // Lokasi user (fallback Salatiga saat permission belum granted)
    val userLat: Double = DEFAULT_SALATIGA_LAT,
    val userLon: Double = DEFAULT_SALATIGA_LON,
    val hasLocationPermission: Boolean = false,

    // Scan
    val isScanning: Boolean = false,
    val scanStage: Int = 0,
    val currentScanRadiusM: Int = 0,
    val currentStageMaxRadiusM: Int = DEFAULT_STAGE_RADIUS_M,

    // Hasil
    val foundLocations: List<FoundLocation> = emptyList(),
    val excludedIds: Set<String> = emptySet(),
    val selectedLocationId: String? = null,

    // UI flags
    val showDetailSheet: Boolean = false,
    val isEmptyResult: Boolean = false,
    val transportMode: TransportMode = TransportMode.Walk,

    // Notify
    val isNotifyPanelOpen: Boolean = false,
    val isNotificationActive: Boolean = false,
    val listNotifications: List<ShiftNotificationItem> = emptyList(),
) {
    val selectedLocation: FoundLocation?
        get() = foundLocations.firstOrNull { it.entity.id == selectedLocationId }

    companion object {
        const val DEFAULT_SALATIGA_LAT = -7.3240
        const val DEFAULT_SALATIGA_LON = 110.5000
        const val DEFAULT_STAGE_RADIUS_M = 500
    }
}
