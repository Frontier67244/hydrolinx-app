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
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxPink

/**
 * Kartu ditampilkan bila scan sampai MAX radius tetap kosong.
 * Memberi user dua pilihan: retry scan atau kembali ke Home.
 */
@Composable
fun MapsEmptyResultCard(
    onRetry: () -> Unit,
    onReturnHome: () -> Unit,
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
                        color = HydrolinxPink.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp),
                    ) {}
                    Icon(
                        imageVector = Icons.Filled.SentimentDissatisfied,
                        contentDescription = null,
                        tint = HydrolinxPink,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Text(
                    text = "Belum ada titik air ditemukan",
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                )
            }
            Text(
                text = "Kami sudah memindai sampai radius 2 km tapi belum menemukan " +
                    "stasiun air yang cocok. Coba lagi atau kembali ke beranda.",
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onReturnHome,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Beranda")
                }
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = HydrolinxCyan),
                ) {
                    Text("Coba lagi")
                }
            }
        }
    }
}
