package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.maps.state.ShiftNotificationItem

@Composable
fun MapsNotifyPanel(
    notifications: List<ShiftNotificationItem>,
    isNotificationActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val shouldScroll = notifications.size >= 2
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, HydrolinxCyan.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .then(
                    if (shouldScroll) {
                        Modifier
                            .heightIn(max = 236.dp)
                            .verticalScroll(scrollState)
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (notifications.isEmpty()) {
                EmptyNotifyCard(
                    title = "Notify shifting belum aktif",
                    subtitle = "Tekan ROUTE untuk menjadwalkan simulasi notifikasi sekitar 30 detik dari sekarang.",
                )
            } else {
                notifications.forEachIndexed { index, notification ->
                    NotifyShiftCard(
                        item = notification,
                        highlight = isNotificationActive && index == 0,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotifyCard(
    title: String,
    subtitle: String,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, HydrolinxCyan.copy(alpha = 0.28f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxOnSurface,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
        }
    }
}

@Composable
private fun NotifyShiftCard(
    item: ShiftNotificationItem,
    highlight: Boolean,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (highlight) HydrolinxCyan.copy(alpha = 0.12f) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) {
                HydrolinxCyan.copy(alpha = 0.48f)
            } else {
                HydrolinxCyan.copy(alpha = 0.22f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = item.timeLabel,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) HydrolinxCyan else HydrolinxOnSurfaceMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
