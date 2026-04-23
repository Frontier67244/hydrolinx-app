package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.feature.hydration.logic.HydrationRules

/**
 * Grid gelas untuk visualisasi jumlah gelas yang akan diminum.
 * Baris max per row = 10.
 */
@Composable
fun WaterGlassGrid(
    targetMl: Int,
    modifier: Modifier = Modifier,
    maxPerRow: Int = 10,
) {
    val glassCount = HydrationRules.targetToGlassCount(targetMl)
    val capped = glassCount.coerceAtMost(MAX_VISIBLE_GLASSES)
    val rows = (capped + maxPerRow - 1) / maxPerRow

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (row in 0 until rows) {
            val startIdx = row * maxPerRow
            val endIdx = (startIdx + maxPerRow).coerceAtMost(capped)
            val itemsInRow = endIdx - startIdx
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(itemsInRow) {
                    GlassIcon()
                }
            }
        }
    }
}

@Composable
private fun GlassIcon() {
    Icon(
        imageVector = Icons.Filled.LocalDrink,
        contentDescription = null,
        tint = HydrolinxCyan,
        modifier = Modifier.size(22.dp),
    )
}

private const val MAX_VISIBLE_GLASSES = 30
