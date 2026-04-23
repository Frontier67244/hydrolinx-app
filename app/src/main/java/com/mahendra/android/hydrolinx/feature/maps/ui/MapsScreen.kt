package com.mahendra.android.hydrolinx.feature.maps.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Point
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.mahendra.android.hydrolinx.HydrolinxApplication
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxPink
import com.mahendra.android.hydrolinx.feature.maps.logic.ScanAlgorithm
import com.mahendra.android.hydrolinx.feature.maps.state.FoundLocation
import com.mahendra.android.hydrolinx.feature.maps.state.MapsEvent
import com.mahendra.android.hydrolinx.feature.maps.state.MapsMode
import com.mahendra.android.hydrolinx.feature.maps.state.MapsUiState
import com.mahendra.android.hydrolinx.feature.maps.ui.components.LocationDetailSheet
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsActionBar
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsActionBarContainer
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsEmptyResultCard
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsInfoMessage
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsInfoPanel
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsNotifyPanel
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsScanningCard
import com.mahendra.android.hydrolinx.feature.maps.ui.components.MapsTopBar
import com.mahendra.android.hydrolinx.feature.maps.ui.components.PinPopup
import com.mahendra.android.hydrolinx.feature.maps.ui.components.openMapsRoute
import com.mahendra.android.hydrolinx.feature.maps.viewmodel.MapsViewModel
import kotlin.math.cos

@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapsScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as HydrolinxApplication
    val viewModel: MapsViewModel = viewModel(
        factory = remember(app.repository) { MapsViewModel.factory(app.repository) }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLocation = uiState.selectedLocation
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val cameraPaddingPx = with(LocalDensity.current) { 104.dp.roundToPx() }
    val popupHorizontalMarginPx = with(LocalDensity.current) { 16.dp.roundToPx() }
    val popupVerticalGapPx = with(LocalDensity.current) { 12.dp.roundToPx() }
    val popupBottomSafePx = with(LocalDensity.current) { 132.dp.roundToPx() }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(uiState.userLat, uiState.userLon),
            DEFAULT_ZOOM,
        )
    }
    var isMapLoaded by remember { mutableStateOf(false) }
    var infoMessageIndex by remember { mutableIntStateOf(0) }
    var recenterNonce by remember { mutableIntStateOf(0) }
    var mapContainerSize by remember { mutableStateOf(IntSize.Zero) }
    var popupSize by remember { mutableStateOf(IntSize.Zero) }
    var popupAnchorPoint by remember { mutableStateOf<Point?>(null) }
    var topOverlayHeightPx by remember { mutableIntStateOf(0) }
    val infoMessages = remember {
        listOf(
            MapsInfoMessage(
                title = "Belum punya air?",
                body = "Temukan sumber air terdekat di sekitarmu.",
            ),
            MapsInfoMessage(
                title = "Sudah punya air?",
                body = "Jangan lupa minum sesuai jadwalmu.",
            ),
            MapsInfoMessage(
                title = "Sudah punya token?",
                body = "Gunakan tokenmu di lokasi air terdekat.",
            ),
        )
    }
    val scanRadius by animateFloatAsState(
        targetValue = uiState.currentScanRadiusM.toFloat(),
        label = "scanRadius",
    )
    val showInfoPanel = uiState.mode == MapsMode.Home &&
        !uiState.isScanning &&
        !uiState.isNotifyPanelOpen

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onEvent(MapsEvent.PermissionResult(granted))
        if (granted) {
            fetchUserLocation(context, fusedLocationClient) { lat, lon ->
                viewModel.onEvent(MapsEvent.UserLocationUpdated(lat, lon))
                recenterNonce++
            }
        }
    }

    fun syncCurrentLocation(recenter: Boolean = false) {
        if (context.hasLocationPermission()) {
            viewModel.onEvent(MapsEvent.PermissionResult(true))
            fetchUserLocation(context, fusedLocationClient) { lat, lon ->
                viewModel.onEvent(MapsEvent.UserLocationUpdated(lat, lon))
                if (recenter) recenterNonce++
            }
        } else {
            requestPermission.launch(LOCATION_PERMISSIONS)
        }
    }

    fun openRouteFor(location: FoundLocation) {
        viewModel.onEvent(MapsEvent.RouteOpened(location.entity.id))
        openMapsRoute(
            context = context,
            lat = location.entity.latitude,
            lon = location.entity.longitude,
        )
    }

    LaunchedEffect(Unit) {
        val granted = context.hasLocationPermission()
        viewModel.onEvent(MapsEvent.PermissionResult(granted))
        if (granted) {
            syncCurrentLocation(recenter = true)
        } else {
            requestPermission.launch(LOCATION_PERMISSIONS)
        }
    }

    LaunchedEffect(uiState.userLat, uiState.userLon, recenterNonce) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(
                LatLng(uiState.userLat, uiState.userLon),
                DEFAULT_ZOOM,
            ),
            durationMs = 750,
        )
    }

    LaunchedEffect(isMapLoaded, selectedLocation?.entity?.id) {
        val location = selectedLocation ?: return@LaunchedEffect
        if (!isMapLoaded) return@LaunchedEffect

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLng(
                LatLng(location.entity.latitude, location.entity.longitude),
            ),
            durationMs = 450,
        )
        cameraPositionState.animate(
            update = CameraUpdateFactory.scrollBy(0f, -POPUP_CAMERA_OFFSET_PX),
            durationMs = 300,
        )
    }

    LaunchedEffect(
        isMapLoaded,
        uiState.mode,
        uiState.currentScanRadiusM,
        uiState.isScanning,
        uiState.foundLocations.map { it.entity.id },
    ) {
        if (!isMapLoaded || uiState.mode != MapsMode.Explore || uiState.currentScanRadiusM <= 0) return@LaunchedEffect
        if (uiState.isScanning) {
            kotlinx.coroutines.delay(120)
        }

        val update = buildScanCameraUpdate(
            userLat = uiState.userLat,
            userLon = uiState.userLon,
            radiusM = uiState.currentScanRadiusM,
            foundLocations = uiState.foundLocations,
            paddingPx = cameraPaddingPx,
        )
        cameraPositionState.animate(
            update = update,
            durationMs = 700,
        )
    }

    LaunchedEffect(showInfoPanel) {
        if (!showInfoPanel) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(INFO_PANEL_ROTATION_MS)
            infoMessageIndex = (infoMessageIndex + 1) % infoMessages.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { mapContainerSize = it },
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = uiState.hasLocationPermission,
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,
            ),
            onMapLoaded = {
                isMapLoaded = true
            },
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 120.dp,
                end = 16.dp,
                bottom = 260.dp,
            ),
            onMapClick = {
                viewModel.onEvent(MapsEvent.DismissPopup)
            },
        ) {
            val userLatLng = LatLng(uiState.userLat, uiState.userLon)

            if (scanRadius > 0f) {
                Circle(
                    center = userLatLng,
                    radius = scanRadius.toDouble(),
                    fillColor = HydrolinxCyan.copy(alpha = 0.12f),
                    strokeColor = HydrolinxCyan.copy(alpha = 0.45f),
                    strokeWidth = 4f,
                )
            }

            uiState.foundLocations.forEach { location ->
                LocationMarker(
                    location = location,
                    isSelected = location.entity.id == uiState.selectedLocationId,
                    onSelect = {
                        viewModel.onEvent(MapsEvent.LocationSelected(location.entity.id))
                    },
                )
            }

            MapEffect(
                selectedLocation?.entity?.id,
                cameraPositionState.position.target,
                cameraPositionState.position.zoom,
            ) { googleMap ->
                popupAnchorPoint = selectedLocation
                    ?.takeIf { !uiState.showDetailSheet }
                    ?.let {
                        googleMap.projection.toScreenLocation(
                            LatLng(it.entity.latitude, it.entity.longitude),
                        )
                    }
            }
        }

        selectedLocation
            ?.takeIf { !uiState.showDetailSheet && uiState.mode == MapsMode.Explore }
            ?.let { location ->
                popupAnchorPoint?.let { anchor ->
                    val popupX = (anchor.x - (popupSize.width / 2)).coerceIn(
                        popupHorizontalMarginPx,
                        (mapContainerSize.width - popupSize.width - popupHorizontalMarginPx)
                            .coerceAtLeast(popupHorizontalMarginPx),
                    )
                    val popupMinY = topOverlayHeightPx + popupVerticalGapPx
                    val popupMaxY = (mapContainerSize.height - popupSize.height - popupBottomSafePx)
                        .coerceAtLeast(popupMinY)
                    val popupY = (anchor.y - popupSize.height - popupVerticalGapPx)
                        .coerceIn(popupMinY, popupMaxY)

                    PinPopup(
                        location = location,
                        onOpenDetail = {
                            viewModel.onEvent(MapsEvent.OpenDetailSheet)
                        },
                        onOpenRoute = {
                            openRouteFor(location)
                        },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset { IntOffset(popupX, popupY) }
                            .onSizeChanged { popupSize = it },
                    )
                }
            }

        MapsTopOverlay(
            uiState = uiState,
            showInfoPanel = showInfoPanel,
            infoMessage = infoMessages[infoMessageIndex],
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            onHeightChanged = { topOverlayHeightPx = it },
        )

        MapsBottomOverlay(
            uiState = uiState,
            onRetry = { viewModel.onEvent(MapsEvent.TapExplore) },
            onReturnHome = { viewModel.onEvent(MapsEvent.ReturnToHome) },
            onPrimaryAction = {
                if (uiState.isScanning) {
                    viewModel.onEvent(MapsEvent.CancelScan)
                } else {
                    viewModel.onEvent(MapsEvent.TapExplore)
                }
            },
            onNotifyClick = { viewModel.onEvent(MapsEvent.ToggleNotify) },
            onRecenterClick = {
                viewModel.onEvent(MapsEvent.ReturnToHome)
                syncCurrentLocation(recenter = true)
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 8.dp),
        )
    }

    if (uiState.showDetailSheet && selectedLocation != null) {
        LocationDetailSheet(
            location = selectedLocation,
            onOpenRoute = {
                openRouteFor(selectedLocation)
            },
            onDismiss = { viewModel.onEvent(MapsEvent.DismissDetailSheet) },
        )
    }
}

@Composable
private fun MapsTopOverlay(
    uiState: MapsUiState,
    showInfoPanel: Boolean,
    infoMessage: MapsInfoMessage,
    modifier: Modifier = Modifier,
    onHeightChanged: (Int) -> Unit = {},
) {
    Column(
        modifier = modifier.onSizeChanged { onHeightChanged(it.height) },
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MapsTopBar(
            sectionLabel = when (uiState.mode) {
                MapsMode.Home -> "HOME"
                MapsMode.Explore -> "EXPLORE"
                MapsMode.Notify -> "NOTIFY"
            },
        )

        AnimatedVisibility(
            visible = showInfoPanel,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 3 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 4 }),
        ) {
            MapsInfoPanel(
                message = infoMessage,
            )
        }
    }
}

@Composable
private fun MapsBottomOverlay(
    uiState: MapsUiState,
    onRetry: () -> Unit,
    onReturnHome: () -> Unit,
    onPrimaryAction: () -> Unit,
    onNotifyClick: () -> Unit,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxStageReached = !uiState.isScanning &&
        uiState.scanStage == ScanAlgorithm.STAGE_MAX_RADII_M.lastIndex &&
        uiState.currentScanRadiusM >= ScanAlgorithm.STAGE_MAX_RADII_M.last()
    val primaryLabel = when {
        uiState.isScanning -> "CANCEL"
        maxStageReached -> "MAX"
        uiState.scanStage == 0 && uiState.currentScanRadiusM == 0 -> "EXPLORE"
        else -> "EXPAND"
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            uiState.mode == MapsMode.Notify -> {
                MapsNotifyPanel(
                    notifications = uiState.listNotifications,
                    isNotificationActive = uiState.isNotificationActive,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            uiState.isScanning -> {
                MapsScanningCard(
                    currentRadiusM = uiState.currentScanRadiusM,
                    maxRadiusM = uiState.currentStageMaxRadiusM,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            uiState.isEmptyResult -> {
                MapsEmptyResultCard(
                    onRetry = onRetry,
                    onReturnHome = onReturnHome,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

        }

        MapsActionBarContainer(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MapsActionBar(
                primaryLabel = primaryLabel,
                primaryColor = if (uiState.isScanning) HydrolinxPink else HydrolinxCyan,
                primaryEnabled = uiState.isScanning || !maxStageReached,
                notifyActive = uiState.isNotifyPanelOpen || uiState.isNotificationActive,
                onPrimaryClick = onPrimaryAction,
                onNotifyClick = onNotifyClick,
                onRecenterClick = onRecenterClick,
            )
        }
    }
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

private fun fetchUserLocation(
    context: Context,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    onLocation: (Double, Double) -> Unit,
) {
    if (!context.hasLocationPermission()) return

    fusedLocationClient.lastLocation
        .addOnSuccessListener { lastLocation ->
            if (lastLocation != null) {
                onLocation(lastLocation.latitude, lastLocation.longitude)
                return@addOnSuccessListener
            }

            val tokenSource = CancellationTokenSource()
            fusedLocationClient
                .getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    tokenSource.token,
                )
                .addOnSuccessListener { currentLocation ->
                    currentLocation?.let {
                        onLocation(it.latitude, it.longitude)
                    }
                }
        }
}

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

private const val DEFAULT_ZOOM = 15f
private const val POPUP_CAMERA_OFFSET_PX = 180f
private const val INFO_PANEL_ROTATION_MS = 5_000L

@Composable
private fun LocationMarker(
    location: FoundLocation,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val markerState = remember(location.entity.id) {
        MarkerState(
            position = LatLng(location.entity.latitude, location.entity.longitude),
        )
    }

    Marker(
        state = markerState,
        title = location.entity.name,
        snippet = location.entity.address,
        icon = BitmapDescriptorFactory.defaultMarker(
            when {
                isSelected -> BitmapDescriptorFactory.HUE_AZURE
                location.entity.isOpen && location.entity.hasCapacity ->
                    BitmapDescriptorFactory.HUE_GREEN
                else -> BitmapDescriptorFactory.HUE_ROSE
            }
        ),
        zIndex = if (isSelected) 2f else 0f,
        onClick = {
            onSelect()
            true
        },
    )
}

private fun buildScanCameraUpdate(
    userLat: Double,
    userLon: Double,
    radiusM: Int,
    foundLocations: List<com.mahendra.android.hydrolinx.feature.maps.state.FoundLocation>,
    paddingPx: Int,
): CameraUpdate {
    val center = LatLng(userLat, userLon)
    if (radiusM <= 0 && foundLocations.isEmpty()) {
        return CameraUpdateFactory.newLatLngZoom(center, DEFAULT_ZOOM)
    }

    val bounds = LatLngBounds.Builder().apply {
        include(center)
        radiusBoundsPoints(center, radiusM.toDouble()).forEach(::include)
        foundLocations.forEach { include(LatLng(it.entity.latitude, it.entity.longitude)) }
    }.build()

    return CameraUpdateFactory.newLatLngBounds(bounds, paddingPx)
}

private fun radiusBoundsPoints(center: LatLng, radiusM: Double): List<LatLng> {
    if (radiusM <= 0.0) return emptyList()

    val latOffset = radiusM / 111_320.0
    val lonOffset = radiusM / (111_320.0 * cos(Math.toRadians(center.latitude)).coerceAtLeast(0.01))
    return listOf(
        LatLng(center.latitude + latOffset, center.longitude),
        LatLng(center.latitude - latOffset, center.longitude),
        LatLng(center.latitude, center.longitude + lonOffset),
        LatLng(center.latitude, center.longitude - lonOffset),
    )
}

private fun Double.coerceAtLeast(minimumValue: Double): Double =
    if (this < minimumValue) minimumValue else this
