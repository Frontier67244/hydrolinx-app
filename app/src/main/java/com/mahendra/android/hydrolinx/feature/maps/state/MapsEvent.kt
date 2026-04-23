package com.mahendra.android.hydrolinx.feature.maps.state

sealed interface MapsEvent {
    data class PermissionResult(val granted: Boolean) : MapsEvent
    data class UserLocationUpdated(val lat: Double, val lon: Double) : MapsEvent

    data object TapExplore : MapsEvent
    data object CancelScan : MapsEvent
    data object ExcludeCurrentAndRetry : MapsEvent

    data class LocationSelected(val locationId: String) : MapsEvent
    data object DismissPopup : MapsEvent

    data object OpenDetailSheet : MapsEvent
    data object DismissDetailSheet : MapsEvent
    data class RouteOpened(val locationId: String) : MapsEvent

    data class SelectTransportMode(val mode: TransportMode) : MapsEvent

    data object ToggleNotify : MapsEvent
    data object ReturnToHome : MapsEvent
}
