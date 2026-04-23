package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted

/**
 * Branding pill yang tampil di pojok kiri atas Maps (Home mode).
 * Layout: [cyan circle w/ water drop]  "Hydrolinx" / "Hydration community"
 */
@Composable
fun MapsBrandLogo(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = HydrolinxCyan,
                    modifier = Modifier.size(36.dp),
                ) {}
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column {
                Text(
                    text = "Hydrolinx",
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                )
                Text(
                    text = "Hydration community",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurfaceMuted,
                )
            }
        }
    }
}
