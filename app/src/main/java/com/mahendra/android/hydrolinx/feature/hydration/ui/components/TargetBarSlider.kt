package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.hydration.logic.HydrationRules
import kotlin.math.roundToInt

/**
 * Custom discrete slider berbasis bar. Tiap bar = 100 ml.
 *
 * Interaksi:
 *  - tap sebuah bar → langsung set target ke nilai bar itu.
 *  - drag horizontal → snap ke bar terdekat.
 *
 * Visual:
 *  - bar aktif = cyan penuh, yang di sebelah aktif makin panjang.
 *  - bar non-aktif = netral abu-abu.
 *
 * Height dibuat variabel agar terlihat seperti "histogram" di desain.
 */
@Composable
fun TargetBarSlider(
    currentMl: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    barMaxHeight: androidx.compose.ui.unit.Dp = 72.dp,
    horizontalPadding: androidx.compose.ui.unit.Dp = 8.dp,
) {
    val barCount = HydrationRules.targetBarCount()
    val activeIndex = HydrationRules.mlToBarIndex(currentMl)
    val density = LocalDensity.current

    val onValueChangeState = rememberUpdatedState(onValueChange)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barMaxHeight)
            .padding(horizontal = horizontalPadding)
            .pointerInput(barCount) {
                detectTapGestures { offset ->
                    val index = indexFromPosition(
                        positionX = offset.x,
                        widthPx = size.width.toFloat(),
                        barCount = barCount,
                    )
                    val ml = HydrationRules.barIndexToMl(index)
                    onValueChangeState.value(ml)
                }
            }
            .pointerInput(barCount) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        change.consume()
                        val index = indexFromPosition(
                            positionX = change.position.x,
                            widthPx = size.width.toFloat(),
                            barCount = barCount,
                        )
                        val ml = HydrationRules.barIndexToMl(index)
                        onValueChangeState.value(ml)
                    },
                )
            },
        contentAlignment = Alignment.BottomCenter,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            for (i in 0 until barCount) {
                val distance = kotlin.math.abs(i - activeIndex)
                // Bar tertinggi di active, makin jauh makin pendek (min 30% height).
                val heightFraction = when {
                    distance == 0 -> 1.0f
                    distance in 1..2 -> 0.78f
                    distance in 3..4 -> 0.62f
                    distance in 5..6 -> 0.5f
                    distance in 7..8 -> 0.42f
                    else -> 0.35f
                }
                val barColor = if (i == activeIndex) HydrolinxCyan else HydrolinxCyan.copy(alpha = 0.28f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barMaxHeight * heightFraction)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor),
                )
            }
        }
    }
}

/**
 * Label min-max di bawah slider.
 */
@Composable
fun TargetRangeLabel(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "${HydrationRules.TARGET_MIN_ML} Ml",
            style = MaterialTheme.typography.labelLarge,
            color = HydrolinxOnSurfaceMuted,
        )
        Text(
            text = "${HydrationRules.TARGET_MAX_ML} Ml",
            style = MaterialTheme.typography.labelLarge,
            color = HydrolinxOnSurfaceMuted,
        )
    }
}

private fun indexFromPosition(
    positionX: Float,
    widthPx: Float,
    barCount: Int,
): Int {
    if (widthPx <= 0f || barCount <= 0) return 0
    val ratio = (positionX / widthPx).coerceIn(0f, 1f)
    val raw = (ratio * (barCount - 1)).roundToInt()
    return raw.coerceIn(0, barCount - 1)
}
