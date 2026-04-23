package com.mahendra.android.hydrolinx.feature.maps.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahendra.android.hydrolinx.core.notification.NotificationBus
import com.mahendra.android.hydrolinx.core.notification.NotificationEvent
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity
import com.mahendra.android.hydrolinx.data.repository.LocalRepository
import com.mahendra.android.hydrolinx.feature.maps.logic.ScanAlgorithm
import com.mahendra.android.hydrolinx.feature.maps.state.FoundLocation
import com.mahendra.android.hydrolinx.feature.maps.state.MapsEvent
import com.mahendra.android.hydrolinx.feature.maps.state.MapsMode
import com.mahendra.android.hydrolinx.feature.maps.state.MapsUiState
import com.mahendra.android.hydrolinx.feature.maps.state.ShiftNotificationItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * ViewModel tunggal untuk layar Maps.
 *
 * Tanggung jawab:
 *  - Menyimpan sumber tunggal lokasi user & daftar lokasi air.
 *  - Menjalankan scan bertahap per stage: 100 -> 500, lalu 1000, lalu 2000.
 *  - Mengelola state popup pin, bottom sheet detail, dan panel notifikasi.
 *
 * Semua aksi dari UI masuk lewat [onEvent], supaya kontrol tetap unidirectional.
 */
class MapsViewModel(
    private val repository: LocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapsUiState())
    val uiState: StateFlow<MapsUiState> = _uiState.asStateFlow()

    private var allLocations: List<WaterLocationEntity> = emptyList()
    private var scanJob: Job? = null
    private val scheduledNotificationJobs = mutableListOf<Job>()

    init {
        viewModelScope.launch {
            repository.ensureLocationsSeeded()
            allLocations = repository.getAllWaterLocations()
            Log.d(TAG, "Loaded ${allLocations.size} water locations")
        }
    }

    fun onEvent(event: MapsEvent) {
        when (event) {
            is MapsEvent.PermissionResult -> handlePermissionResult(event.granted)
            is MapsEvent.UserLocationUpdated -> handleUserLocationUpdated(event.lat, event.lon)
            MapsEvent.TapExplore -> handleTapExplore()
            MapsEvent.CancelScan -> handleCancelScan()
            MapsEvent.ExcludeCurrentAndRetry -> handleExcludeAndRetry()
            is MapsEvent.LocationSelected -> handleLocationSelected(event.locationId)
            MapsEvent.DismissPopup -> handleDismissPopup()
            MapsEvent.OpenDetailSheet -> handleOpenDetailSheet()
            MapsEvent.DismissDetailSheet -> handleDismissDetailSheet()
            is MapsEvent.RouteOpened -> handleRouteOpened(event.locationId)
            is MapsEvent.SelectTransportMode -> _uiState.update { it.copy(transportMode = event.mode) }
            MapsEvent.ToggleNotify -> handleToggleNotify()
            MapsEvent.ReturnToHome -> handleReturnToHome()
        }
    }

    // region Handlers

    private fun handlePermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = granted) }
    }

    private fun handleUserLocationUpdated(lat: Double, lon: Double) {
        Log.d(TAG, "User location updated -> lat=$lat, lon=$lon")
        _uiState.update { it.copy(userLat = lat, userLon = lon) }
    }

    private fun handleTapExplore() {
        val snapshot = _uiState.value
        if (snapshot.isScanning) return

        val lastStageIndex = ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex
        val maxReached = snapshot.scanStage == lastStageIndex &&
            snapshot.currentScanRadiusM >= ScanAlgorithm.STAGE_MAX_RADII_M.last()
        if (maxReached) return

        _uiState.update {
            it.copy(
                mode = MapsMode.Explore,
                isNotifyPanelOpen = false,
                isEmptyResult = false,
                showDetailSheet = false,
            )
        }
        startScan(stageIndex = snapshot.scanStage)
    }

    private fun handleCancelScan() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update {
            it.copy(
                isScanning = false,
                isEmptyResult = false,
                showDetailSheet = false,
            )
        }
    }

    private fun handleExcludeAndRetry() {
        val currentId = _uiState.value.selectedLocationId ?: return
        val currentRadius = _uiState.value.currentScanRadiusM
        val stageToScan = stageIndexForRadius(currentRadius)
        _uiState.update {
            it.copy(
                excludedIds = it.excludedIds + currentId,
                scanStage = stageToScan,
                currentScanRadiusM = stageStartFloor(stageToScan),
                currentStageMaxRadiusM = ScanAlgorithm.STAGE_MAX_RADII_M[stageToScan],
                selectedLocationId = null,
                showDetailSheet = false,
                foundLocations = emptyList(),
                isEmptyResult = false,
            )
        }
        startScan(stageIndex = stageToScan)
    }

    private fun handleLocationSelected(locationId: String) {
        _uiState.update { it.copy(selectedLocationId = locationId, showDetailSheet = false) }
    }

    private fun handleDismissPopup() {
        _uiState.update { it.copy(selectedLocationId = null, showDetailSheet = false) }
    }

    private fun handleOpenDetailSheet() {
        if (_uiState.value.selectedLocationId == null) return
        _uiState.update { it.copy(showDetailSheet = true) }
    }

    private fun handleDismissDetailSheet() {
        _uiState.update { it.copy(showDetailSheet = false) }
    }

    private fun handleToggleNotify() {
        val opening = !_uiState.value.isNotifyPanelOpen
        if (opening) {
            scanJob?.cancel()
            scanJob = null
        }
        _uiState.update {
            it.copy(
                isNotifyPanelOpen = opening,
                mode = if (opening) MapsMode.Notify else MapsMode.Home,
                isScanning = if (opening) false else it.isScanning,
                scanStage = if (opening) 0 else it.scanStage,
                currentScanRadiusM = if (opening) 0 else it.currentScanRadiusM,
                currentStageMaxRadiusM = if (opening) MapsUiState.DEFAULT_STAGE_RADIUS_M else it.currentStageMaxRadiusM,
                foundLocations = if (opening) emptyList() else it.foundLocations,
                selectedLocationId = if (opening) null else it.selectedLocationId,
                showDetailSheet = if (opening) false else it.showDetailSheet,
                isNotificationActive = if (opening) false else it.isNotificationActive,
            )
        }
    }

    private fun handleReturnToHome() {
        scanJob?.cancel()
        scanJob = null
        _uiState.update {
            MapsUiState(
                userLat = it.userLat,
                userLon = it.userLon,
                hasLocationPermission = it.hasLocationPermission,
                transportMode = it.transportMode,
                listNotifications = it.listNotifications,
                isNotificationActive = it.isNotificationActive,
            )
        }
    }

    private fun handleRouteOpened(locationId: String) {
        val location = resolveLocation(locationId) ?: return
        scheduleNotifyShifting(location)
    }

    // endregion

    // region Scan loop

    private fun startScan(stageIndex: Int) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            if (allLocations.isEmpty()) {
                allLocations = repository.getAllWaterLocations()
            }

            val safeStageIndex = stageIndex.coerceIn(0, ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex)
            val stageMaxRadius = ScanAlgorithm.STAGE_MAX_RADII_M[safeStageIndex]
            val currentRadius = _uiState.value.currentScanRadiusM
            val startRadius = when {
                currentRadius <= 0 -> ScanAlgorithm.INITIAL_RADIUS_M
                currentRadius >= stageMaxRadius -> stageMaxRadius
                else -> currentRadius + ScanAlgorithm.STEP_M
            }

            _uiState.update {
                it.copy(
                    mode = MapsMode.Explore,
                    isScanning = true,
                    isEmptyResult = false,
                    scanStage = safeStageIndex,
                    currentStageMaxRadiusM = stageMaxRadius,
                )
            }

            var hasEmittedFoundNotification = false
            var radius = startRadius
            while (radius <= stageMaxRadius) {
                val snapshot = _uiState.value
                val found = ScanAlgorithm.filterLocationsInRadius(
                    userLat = snapshot.userLat,
                    userLon = snapshot.userLon,
                    locations = allLocations,
                    radius = radius.toDouble(),
                    excludedIds = snapshot.excludedIds,
                )
                val mapped = found.map { FoundLocation(it.item, it.distanceM) }

                Log.d(
                    TAG,
                    "Radius ${radius}m (stage ${safeStageIndex + 1}/${ScanAlgorithm.STAGE_MAX_RADII_M.size}) -> ${mapped.size} locations"
                )

                _uiState.update { state ->
                    val keptSelection = state.selectedLocationId
                        ?.takeIf { selectedId -> mapped.any { it.entity.id == selectedId } }

                    state.copy(
                        isScanning = true,
                        scanStage = safeStageIndex,
                        currentScanRadiusM = radius,
                        currentStageMaxRadiusM = stageMaxRadius,
                        foundLocations = mapped,
                        selectedLocationId = keptSelection,
                        isEmptyResult = false,
                    )
                }

                if (!hasEmittedFoundNotification && mapped.isNotEmpty()) {
                    NotificationBus.emit(
                        NotificationEvent.MapsFound(mapped.first().entity.name)
                    )
                    hasEmittedFoundNotification = true
                }

                if (radius == stageMaxRadius) break

                delay(ScanAlgorithm.DELAY_MS)
                radius += ScanAlgorithm.STEP_M
            }

            val nextStage = (safeStageIndex + 1).coerceAtMost(ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex)
            val stageFinishedWithoutResults = _uiState.value.foundLocations.isEmpty() &&
                safeStageIndex == ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex

            Log.d(
                TAG,
                "Stage ${safeStageIndex + 1} finished at ${stageMaxRadius}m with ${_uiState.value.foundLocations.size} locations"
            )

            if (stageFinishedWithoutResults) {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStage = safeStageIndex,
                        isEmptyResult = true,
                        foundLocations = emptyList(),
                        selectedLocationId = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isScanning = false,
                        scanStage = if (safeStageIndex == ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex) {
                            safeStageIndex
                        } else {
                            nextStage
                        },
                        currentScanRadiusM = stageMaxRadius,
                        currentStageMaxRadiusM = stageMaxRadius,
                        isEmptyResult = false,
                    )
                }
            }
        }
    }

    // endregion

    companion object {
        private const val TAG = "MapsViewModel"
        private const val TEST_SHIFT_DELAY_MS = 30_000L
        private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")

        fun factory(repository: LocalRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MapsViewModel(repository) as T
                }
            }
    }

    private fun stageIndexForRadius(radius: Int): Int =
        ScanAlgorithm.STAGE_MAX_RADII_M.indexOfFirst { radius <= it }
            .takeIf { it >= 0 }
            ?: ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex

    private fun stageStartFloor(stageIndex: Int): Int =
        if (stageIndex <= 0) 0 else ScanAlgorithm.STAGE_MAX_RADII_M[stageIndex - 1]

    private fun resolveLocation(locationId: String): WaterLocationEntity? =
        _uiState.value.foundLocations.firstOrNull { it.entity.id == locationId }?.entity
            ?: allLocations.firstOrNull { it.id == locationId }

    private fun scheduleNotifyShifting(location: WaterLocationEntity) {
        val now = System.currentTimeMillis()
        val triggerAtMs = now + TEST_SHIFT_DELAY_MS
        val triggerTime = Instant.ofEpochMilli(triggerAtMs)
            .atZone(ZoneId.systemDefault())
        val title = if (triggerTime.hour < 12) {
            "Shifting pagi"
        } else {
            "Shifting sore"
        }

        Log.d(
            TAG,
            "Route opened for ${location.name} -> scheduling notify at ${timeFormatter.format(triggerTime)}"
        )

        scheduledNotificationJobs += viewModelScope.launch {
            delay((triggerAtMs - System.currentTimeMillis()).coerceAtLeast(0L))

            val notification = ShiftNotificationItem(
                id = "${location.id}-$triggerAtMs",
                title = title,
                subtitle = location.name,
                timeLabel = timeFormatter.format(triggerTime),
                triggerAtMs = triggerAtMs,
            )

            _uiState.update {
                it.copy(
                    listNotifications = listOf(notification) + it.listNotifications,
                    isNotificationActive = true,
                    isNotifyPanelOpen = true,
                    mode = MapsMode.Notify,
                    isScanning = false,
                    showDetailSheet = false,
                    selectedLocationId = null,
                )
            }

            NotificationBus.emit(
                NotificationEvent.NotifyShifting(
                    title = notification.title,
                    locationName = notification.subtitle,
                    triggerAtMs = notification.triggerAtMs,
                )
            )
        }
    }

    override fun onCleared() {
        scanJob?.cancel()
        scheduledNotificationJobs.forEach { it.cancel() }
        scheduledNotificationJobs.clear()
        super.onCleared()
    }
}
