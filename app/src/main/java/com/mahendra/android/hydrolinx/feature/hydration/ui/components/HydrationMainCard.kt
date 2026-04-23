package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Card putih utama di Main screen. Berisi ring progress + cluster aksi utama.
 *
 *   [ Progress ring ]
 *          ↓
 *   [ Notify ] [ Swipe ] [ Insight ]
 *
 * Ring dibiarkan dominan, lalu cluster aksi diturunkan ke lower-middle screen
 * supaya lebih ergonomis untuk jempol.
 */
@Composable
fun HydrationMainCard(
    progress: Float,
    totalTodayMl: Int,
    canDrink: Boolean,
    notifyActive: Boolean,
    insightActive: Boolean,
    onSwipeToDrink: () -> Unit,
    onNotifyClick: () -> Unit,
    onInsightClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 556.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(top = 48.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HydrationProgressRing(
                progress = progress,
                totalTodayMl = totalTodayMl,
            )
            Spacer(Modifier.height(82.dp))
            HydrationActionBar(
                notifyActive = notifyActive,
                insightActive = insightActive,
                canDrink = canDrink,
                onSwipeToDrink = onSwipeToDrink,
                onNotifyClick = onNotifyClick,
                onInsightClick = onInsightClick,
            )
        }
    }
}
