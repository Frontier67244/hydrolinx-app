package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted

/**
 * Kartu status scan di atas ActionBar saat isScanning = true.
 * Menunjukkan radius saat ini dan progress bar indeterminate lembut.
 */
@Composable
fun MapsScanningCard(
    currentRadiusM: Int,
    maxRadiusM: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PulsingDot()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Memindai titik air…",
                        style = MaterialTheme.typography.titleMedium,
                        color = HydrolinxOnSurface,
                    )
                    Text(
                        text = "Radius ${currentRadiusM} m dari $maxRadiusM m",
                        style = MaterialTheme.typography.bodyMedium,
                        color = HydrolinxOnSurfaceMuted,
                    )
                }
            }
            val progress = currentRadiusM.toFloat() / maxRadiusM.toFloat()
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = HydrolinxCyan,
                trackColor = HydrolinxCyan.copy(alpha = 0.15f),
            )
        }
    }
}

@Composable
private fun PulsingDot() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = HydrolinxCyan.copy(alpha = 0.2f),
                radius = size.minDimension / 2f * scale,
                center = Offset(size.width / 2f, size.height / 2f),
            )
            drawCircle(
                color = HydrolinxCyan,
                radius = size.minDimension / 4f,
                center = Offset(size.width / 2f, size.height / 2f),
            )
        }
    }
}
