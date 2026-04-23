package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Track vertikal "swipe-to-drink".
 *
 * Interaksi:
 *  - User drag tombol bulat (bawah) ke atas mengikuti track.
 *  - Threshold default = 65% tinggi efektif track.
 *  - Lewat threshold saat lepas → trigger [onSwipeComplete], tombol balik ke bawah dengan spring.
 *  - Belum lewat threshold → langsung spring balik tanpa trigger.
 *
 * Visual guide:
 *  - Track pill cyan transparan sebagai jalur.
 *  - Glow vertikal dari bawah yang intensitasnya mengikuti posisi drag.
 *  - Panah kecil di atas (ghost hint) yang meredup saat user drag.
 *  - Tombol bulat putih berborder cyan dengan ikon panah atas.
 */
@Composable
fun SwipeToDrinkTrack(
    onSwipeComplete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trackHeight: Dp = 132.dp,
    buttonSize: Dp = 52.dp,
    thresholdFraction: Float = 0.65f,
) {
    val density = LocalDensity.current
    val maxDragPx = with(density) { (trackHeight - buttonSize).toPx() }
    val offsetY = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val progress = if (maxDragPx <= 0f) 0f else (offsetY.value / maxDragPx).coerceIn(0f, 1f)
    val trackAlpha = if (enabled) 0.06f else 0.025f
    val borderAlpha = if (enabled) 0.25f else 0.12f
    val iconAlpha = if (enabled) 1f else 0.32f

    Box(
        modifier = modifier
            .width(buttonSize + 16.dp)
            .height(trackHeight),
    ) {
        // Track background (pill)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(buttonSize / 2 + 8.dp))
                .background(HydrolinxCyan.copy(alpha = trackAlpha))
                .border(
                    width = 1.dp,
                    color = HydrolinxCyan.copy(alpha = borderAlpha),
                    shape = RoundedCornerShape(buttonSize / 2 + 8.dp),
                ),
        )

        // Glow gradient dari bawah, intensitas & tinggi ikut progress drag.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.25f + progress * 0.75f)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(buttonSize / 2 + 4.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HydrolinxCyan.copy(alpha = 0f),
                            HydrolinxCyan.copy(alpha = 0.15f + progress * 0.25f),
                        ),
                    ),
                ),
        )

        // Ghost arrow di atas — petunjuk arah swipe, meredup saat user sudah drag.
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = HydrolinxCyan.copy(alpha = 0.55f * (1f - progress) * iconAlpha),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .size(22.dp),
        )

        // Tombol bulat yang bisa di-drag ke atas.
        Surface(
            shape = CircleShape,
            color = Color.White,
            border = BorderStroke(1.5.dp, HydrolinxCyan.copy(alpha = iconAlpha)),
            shadowElevation = if (enabled) 4.dp else 0.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = -offsetY.value.roundToInt()) }
                .size(buttonSize)
                .then(
                    if (enabled) {
                        Modifier.pointerInput(maxDragPx) {
                            detectDragGestures(
                                onDragEnd = {
                                    val reached = offsetY.value >= maxDragPx * thresholdFraction
                                    scope.launch {
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMediumLow,
                                            ),
                                        )
                                        if (reached) onSwipeComplete()
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        offsetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(),
                                        )
                                    }
                                },
                                onDrag = { change, drag ->
                                    change.consume()
                                    // drag.y negatif = ke atas. offsetY positif = jarak dari bawah.
                                    val next = (offsetY.value - drag.y).coerceIn(0f, maxDragPx)
                                    scope.launch { offsetY.snapTo(next) }
                                },
                            )
                        }
                    } else {
                        Modifier
                    },
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = "Swipe up to drink",
                    tint = HydrolinxCyan.copy(alpha = iconAlpha),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}
