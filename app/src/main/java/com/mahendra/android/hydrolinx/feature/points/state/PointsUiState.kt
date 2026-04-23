package com.mahendra.android.hydrolinx.feature.points.state

import com.mahendra.android.hydrolinx.feature.points.logic.RedeemRules

data class PointsNotificationItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
)

data class PointsUiState(
    val totalRedeem: Int = 0,
    val activePoints: Int = 0,
    val bankPoints: Int = 0,
    val tokenCount: Int = 0,
    val lastRedeemDateIso: String? = null,
    val todayIso: String = "",
    val isNotifyPanelOpen: Boolean = false,
    val isScanOpen: Boolean = false,
    val selectedScanVolumeMl: Int = RedeemRules.DEFAULT_SCAN_VOLUME_ML,
    val lastNotifyMessage: String = "",
    val notifications: List<PointsNotificationItem> = emptyList(),
) {
    val totalPoints: Int
        get() = activePoints + bankPoints

    val canRedeemToday: Boolean
        get() = lastRedeemDateIso != todayIso &&
            (activePoints >= RedeemRules.POINTS_PER_TOKEN || bankPoints >= RedeemRules.POINTS_PER_TOKEN)

    val canScan: Boolean
        get() = tokenCount > 0
}
