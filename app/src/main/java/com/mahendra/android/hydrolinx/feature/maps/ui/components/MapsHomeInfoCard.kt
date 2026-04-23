package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
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
 * Kartu info besar di atas CTA "EXPLORE" pada Home mode Maps.
 * Menjelaskan fungsi layar: mencari titik stasiun air terdekat.
 */
@Composable
fun MapsHomeInfoCard(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = HydrolinxCyan.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp),
                    ) {}
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = HydrolinxCyan,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "Temukan stasiun air terdekat",
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                )
            }
            Text(
                text = "Hydrolinx akan memindai dalam radius bertahap " +
                    "(100m → 500m), lalu kamu bisa lanjut EXPAND ke 1km " +
                    "dan 2km untuk melihat lebih banyak titik air.",
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
        }
    }
}
