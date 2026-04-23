package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted

/**
 * Ring progress besar untuk Main screen.
 * Menampilkan percentage di tengah dan label "X.XX Ml" di atas ring.
 */
@Composable
fun HydrationProgressRing(
    progress: Float,
    totalTodayMl: Int,
    modifier: Modifier = Modifier,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progressRing",
    )
    val percentInt = (animated * 100).toInt()

    Box(
        modifier = modifier
            .size(260.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer halo
        Surface(
            shape = CircleShape,
            color = HydrolinxCyan.copy(alpha = 0.08f),
            modifier = Modifier.size(260.dp),
        ) {}

        // Canvas for ring
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .padding(8.dp),
        ) {
            val strokeWidth = 20.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f,
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = HydrolinxCyan.copy(alpha = 0.18f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
            drawArc(
                color = HydrolinxCyan,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }

        // Inner disc + percent text
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier.size(130.dp),
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "$percentInt%",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = HydrolinxCyan,
                )
            }
        }

        // Bubble label on top-right of ring
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 8.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 3.dp,
            ) {
                Text(
                    text = formatMlLabel(totalTodayMl),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = HydrolinxOnSurface,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
            }
        }
    }
}

private fun formatMlLabel(ml: Int): String {
    if (ml < 1000) return "$ml Ml"
    val whole = ml / 1000
    val remainder = (ml % 1000) / 10
    return if (remainder == 0) {
        "$whole Ml"
    } else {
        "${whole}.${remainder.toString().padStart(2, '0')} Ml"
    }
}
