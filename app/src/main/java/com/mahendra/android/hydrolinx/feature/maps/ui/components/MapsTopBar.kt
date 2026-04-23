package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface

@Composable
fun MapsTopBar(
    sectionLabel: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(
                    width = 2.dp,
                    color = HydrolinxCyan,
                    shape = CircleShape,
                )
                .background(
                    color = Color.White.copy(alpha = 0.96f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                tint = HydrolinxCyan,
                modifier = Modifier.size(22.dp),
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 60.dp),
            color = Color.White.copy(alpha = 0.96f),
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text(
                        text = "MAPS",
                        style = MaterialTheme.typography.labelLarge,
                        color = HydrolinxOnSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = ">",
                        style = MaterialTheme.typography.labelLarge,
                        color = HydrolinxOnSurface,
                    )
                    Text(
                        text = sectionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = HydrolinxOnSurface,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
