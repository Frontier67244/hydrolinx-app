package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted

data class MapsInfoMessage(
    val title: String,
    val body: String,
)

@Composable
fun MapsInfoPanel(
    message: MapsInfoMessage,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Halo, ",
                        style = MaterialTheme.typography.titleMedium,
                        color = HydrolinxOnSurface,
                    )
                    Text(
                        text = "Healthy",
                        style = MaterialTheme.typography.titleMedium,
                        color = HydrolinxCyan,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.WaterDrop,
                        contentDescription = null,
                        tint = HydrolinxCyan,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Crossfade(
                targetState = message,
                label = "mapsInfoMessage",
            ) { current ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = HydrolinxOnSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = current.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = HydrolinxOnSurfaceMuted,
                    )
                }
            }
        }
    }
}
