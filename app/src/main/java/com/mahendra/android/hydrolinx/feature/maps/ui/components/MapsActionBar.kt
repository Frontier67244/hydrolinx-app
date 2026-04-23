package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxPink

/**
 * Floating action bar di bagian bawah layar Maps.
 * Layout: [bell] [ EXPLORE / CANCEL / RETRY ] [target-my-location].
 */
@Composable
fun MapsActionBar(
    primaryLabel: String,
    primaryColor: Color,
    primaryEnabled: Boolean,
    notifyActive: Boolean,
    onPrimaryClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onRecenterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircleIconButton(
            icon = Icons.Filled.Notifications,
            tint = if (notifyActive) Color.White else HydrolinxOnSurface,
            background = if (notifyActive) HydrolinxPink else Color.White,
            contentDescription = "Notifikasi shifting",
            onClick = onNotifyClick,
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .clickable(enabled = primaryEnabled) { onPrimaryClick() },
            color = if (primaryEnabled) primaryColor else primaryColor.copy(alpha = 0.45f),
            shadowElevation = 6.dp,
            shape = RoundedCornerShape(28.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = primaryLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }
        }

        CircleIconButton(
            icon = Icons.Filled.MyLocation,
            tint = HydrolinxCyan,
            background = Color.White,
            contentDescription = "Pusatkan lokasi",
            onClick = onRecenterClick,
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    background: Color,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(background)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * Versi overloaded sederhana untuk Surface wrapping yang butuh elevation lebih ringan.
 */
@Composable
fun MapsActionBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            content()
        }
    }
}
