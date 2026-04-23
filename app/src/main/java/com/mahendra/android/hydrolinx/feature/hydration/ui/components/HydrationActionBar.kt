package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface

/**
 * Action cluster bawah Main screen.
 * Tiga aksi diletakkan dalam satu garis horizontal:
 *   kiri  = Notify
 *   tengah = Swipe MINUM
 *   kanan = Insight
 *
 * Dengan begini aksi primer dan sekunder terasa satu sistem layout,
 * tidak lagi tampak seperti elemen terpisah.
 */
@Composable
fun HydrationActionBar(
    notifyActive: Boolean,
    insightActive: Boolean,
    canDrink: Boolean,
    onSwipeToDrink: () -> Unit,
    onNotifyClick: () -> Unit,
    onInsightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomStart,
        ) {
            LabeledPill(
                label = "Notify",
                icon = Icons.Filled.Notifications,
                active = notifyActive,
                onClick = onNotifyClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier.width(96.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            SwipeToDrinkTrack(
                onSwipeComplete = onSwipeToDrink,
                enabled = canDrink,
                trackHeight = 132.dp,
                buttonSize = 54.dp,
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.BottomEnd,
        ) {
            LabeledPill(
                label = "Insight",
                icon = Icons.Filled.BarChart,
                active = insightActive,
                onClick = onInsightClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LabeledPill(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (active) HydrolinxCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.94f)
    val contentColor = if (active) HydrolinxCyan else HydrolinxOnSurface
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bg,
        border = BorderStroke(
            width = if (active) 1.4.dp else 1.dp,
            color = if (active) {
                HydrolinxCyan.copy(alpha = 0.68f)
            } else {
                HydrolinxCyan.copy(alpha = 0.22f)
            },
        ),
        shadowElevation = if (active) 3.dp else 1.dp,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = contentColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}
