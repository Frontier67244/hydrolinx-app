package com.mahendra.android.hydrolinx.feature.hydration.ui.components

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
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationToast

/**
 * Panel "Notify Shifting Hydration" — daftar toast yang tercatat setelah confirm minum.
 * Format row (match desain): "+X poin • Label    HH.mm"
 */
@Composable
fun HydrationNotifyPanel(
    toasts: List<HydrationToast>,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, HydrolinxCyan.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (toasts.isEmpty()) {
                EmptyNotifyRow()
            } else {
                toasts.forEachIndexed { index, toast ->
                    NotifyRow(toast = toast, highlight = index == 0)
                }
            }
        }
    }
}

@Composable
private fun NotifyRow(toast: HydrationToast, highlight: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (highlight) HydrolinxCyan.copy(alpha = 0.12f) else Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = if (highlight) HydrolinxCyan.copy(alpha = 0.45f) else HydrolinxCyan.copy(alpha = 0.22f),
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "+${toast.pointsAwarded} poin • ${toast.label}",
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxOnSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = toast.timeLabel,
                style = MaterialTheme.typography.titleMedium,
                color = if (highlight) HydrolinxCyan else HydrolinxOnSurfaceMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyNotifyRow() {
    Text(
        text = "Belum ada notifikasi. Tekan tombol MINUM untuk mencatat sesi pertama.",
        style = MaterialTheme.typography.bodyMedium,
        color = HydrolinxOnSurfaceMuted,
    )
}
