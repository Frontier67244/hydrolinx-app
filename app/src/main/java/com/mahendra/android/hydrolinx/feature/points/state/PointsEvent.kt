package com.mahendra.android.hydrolinx.feature.points.state

sealed interface PointsEvent {
    data object TapRedeem : PointsEvent
    data object OpenScan : PointsEvent
    data object CloseScan : PointsEvent
    data class SelectScanVolume(val volumeMl: Int) : PointsEvent
    data object ConfirmScan : PointsEvent
    data object ToggleNotifyPanel : PointsEvent
}
