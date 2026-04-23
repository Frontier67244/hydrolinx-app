package com.mahendra.android.hydrolinx.feature.points.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahendra.android.hydrolinx.HydrolinxApplication
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxBackground
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationHeader
import com.mahendra.android.hydrolinx.feature.points.logic.RedeemRules
import com.mahendra.android.hydrolinx.feature.points.state.PointsEvent
import com.mahendra.android.hydrolinx.feature.points.state.PointsNotificationItem
import com.mahendra.android.hydrolinx.feature.points.state.PointsUiState
import com.mahendra.android.hydrolinx.feature.points.viewmodel.PointsViewModel

@Composable
fun PointsScreen() {
    val context = LocalContext.current
    val repository = remember(context) {
        (context.applicationContext as HydrolinxApplication).repository
    }
    val viewModel: PointsViewModel = viewModel(
        factory = remember(repository) { PointsViewModel.factory(repository) },
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isScanOpen) {
        PointsScanPage(
            state = state,
            onEvent = viewModel::onEvent,
        )
    } else {
        PointsRedeemPage(
            state = state,
            onEvent = viewModel::onEvent,
        )
    }
}

@Composable
private fun PointsRedeemPage(
    state: PointsUiState,
    onEvent: (PointsEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HydrolinxBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            HydrationHeader(
                section = "Poin",
                subsection = if (state.isNotifyPanelOpen) "Notify" else "Redeem",
                modifier = Modifier.fillMaxWidth(),
            )

            if (!state.isNotifyPanelOpen) {
                PointsInfoPanel()
            }

            WalletRedeemCard(state = state)

            Spacer(modifier = Modifier.height(12.dp))
        }

        AnimatedVisibility(
            visible = state.isNotifyPanelOpen,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 28.dp, end = 28.dp, bottom = 88.dp)
                .zIndex(2f),
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
        ) {
            PointsNotifyOverlay(notifications = state.notifications)
        }

        PointsActionBar(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .zIndex(3f),
        )
    }
}

@Composable
private fun PointsInfoPanel() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HydrolinxCyan),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Halo, Healthy",
                    style = MaterialTheme.typography.labelLarge,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Poinmu sudah cukup?\nRedeem untuk token akses air sekarang.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurface,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                )
            }
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = HydrolinxCyan,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun WalletRedeemCard(state: PointsUiState) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(276.dp),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, HydrolinxCyan),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${state.totalPoints}",
                style = MaterialTheme.typography.headlineMedium,
                color = HydrolinxOnSurface,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = "Redeem",
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxOnSurface,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WalletMetric(
                    title = "${state.activePoints}/${RedeemRules.DAILY_ACTIVE_POINT_CAP}",
                    label = "Total Poin",
                )
                WalletMetric(
                    title = "${state.tokenCount}/${RedeemRules.DAILY_TOKEN_USE_LIMIT}",
                    label = "Token",
                )
                WalletMetric(
                    title = "${state.bankPoints}/${RedeemRules.BANK_POINT_CAP}",
                    label = "Bank Poin",
                )
            }
        }
    }
}

@Composable
private fun WalletMetric(
    title: String,
    label: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = HydrolinxOnSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = HydrolinxOnSurface,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PointsActionBar(
    state: PointsUiState,
    onEvent: (PointsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SquareActionButton(
            icon = Icons.Filled.NotificationsNone,
            label = "Notify",
            selected = state.isNotifyPanelOpen,
            onClick = { onEvent(PointsEvent.ToggleNotifyPanel) },
        )

        Button(
            onClick = { onEvent(PointsEvent.TapRedeem) },
            enabled = state.canRedeemToday,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HydrolinxCyan,
                disabledContainerColor = HydrolinxOnSurface.copy(alpha = 0.14f),
                disabledContentColor = HydrolinxOnSurfaceMuted,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.CardGiftcard,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "REDEEM",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        SquareActionButton(
            icon = Icons.Filled.QrCodeScanner,
            label = "Scan",
            enabled = state.canScan,
            onClick = { onEvent(PointsEvent.OpenScan) },
        )
    }
}

@Composable
private fun SquareActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val container = when {
        selected -> HydrolinxCyan
        enabled -> Color.White
        else -> HydrolinxOnSurface.copy(alpha = 0.12f)
    }
    val content = when {
        selected -> Color.White
        enabled -> HydrolinxCyan
        else -> HydrolinxOnSurfaceMuted
    }
    Surface(
        modifier = Modifier
            .size(width = 52.dp, height = 52.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = container,
        shadowElevation = if (selected) 3.dp else 0.dp,
        border = if (selected) null else BorderStroke(1.dp, content.copy(alpha = if (enabled) 1f else 0.35f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = content,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PointsNotifyOverlay(notifications: List<PointsNotificationItem>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, HydrolinxCyan),
    ) {
        if (notifications.isEmpty()) {
            Text(
                text = "Belum ada notifikasi poin.",
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 168.dp)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { item ->
                    NotificationRow(item = item)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: PointsNotificationItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, HydrolinxCyan),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = HydrolinxOnSurface,
                )
            }
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.labelLarge,
                color = HydrolinxOnSurfaceMuted,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun PointsScanPage(
    state: PointsUiState,
    onEvent: (PointsEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HydrolinxBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 214.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            HydrationHeader(
                section = "Poin",
                subsection = "Scan",
                modifier = Modifier.fillMaxWidth(),
            )
            ScanPreview(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }

        ScanControlPanel(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .zIndex(2f),
        )
    }
}

@Composable
private fun ScanPreview(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.QrCodeScanner,
                contentDescription = null,
                tint = HydrolinxCyan.copy(alpha = 0.34f),
                modifier = Modifier.size(72.dp),
            )
        }
    }
}

@Composable
private fun ScanControlPanel(
    state: PointsUiState,
    onEvent: (PointsEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RedeemRules.SCAN_VOLUME_OPTIONS_ML.forEach { volume ->
                    VolumeChip(
                        volumeMl = volume,
                        selected = state.selectedScanVolumeMl == volume,
                        onClick = { onEvent(PointsEvent.SelectScanVolume(volume)) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onEvent(PointsEvent.CloseScan) },
                    modifier = Modifier.size(width = 52.dp, height = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, HydrolinxCyan),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = HydrolinxCyan,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Button(
                    onClick = { onEvent(PointsEvent.ConfirmScan) },
                    enabled = state.canScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HydrolinxCyan,
                        disabledContainerColor = HydrolinxOnSurface.copy(alpha = 0.14f),
                        disabledContentColor = HydrolinxOnSurfaceMuted,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SCAN",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun VolumeChip(
    volumeMl: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container = if (selected) HydrolinxCyan.copy(alpha = 0.12f) else Color.White
    val content = if (selected) HydrolinxCyan else HydrolinxCyan.copy(alpha = 0.42f)
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = container,
        border = BorderStroke(1.dp, content),
    ) {
        Text(
            text = "+$volumeMl Ml",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
