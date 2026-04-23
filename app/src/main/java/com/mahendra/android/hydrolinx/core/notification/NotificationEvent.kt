package com.mahendra.android.hydrolinx.core.notification

sealed interface NotificationEvent {
    data class HydrationLogged(val points: Int, val timestampMs: Long) : NotificationEvent
    data class RedeemSuccess(val tokenGained: Int) : NotificationEvent
    data class ScanSuccess(val volumeMl: Int) : NotificationEvent
    data class MapsFound(val locationName: String) : NotificationEvent
    data class NotifyShifting(val title: String, val locationName: String, val triggerAtMs: Long) : NotificationEvent
    data class GenericInfo(val message: String) : NotificationEvent
}
