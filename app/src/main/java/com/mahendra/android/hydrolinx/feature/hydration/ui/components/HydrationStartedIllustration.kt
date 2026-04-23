package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
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
 * Placeholder slot untuk ilustrasi Started screen.
 * Nanti mudah diganti oleh aset PNG/SVG tanpa merubah struktur.
 */
@Composable
fun HydrationStartedIllustration(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = HydrolinxCyan.copy(alpha = 0.15f),
            modifier = Modifier.size(220.dp),
        ) {}
        Icon(
            imageVector = Icons.Filled.RocketLaunch,
            contentDescription = "Start illustration",
            tint = HydrolinxCyan,
            modifier = Modifier.size(100.dp),
        )
    }
}

/**
 * Seluruh konten Started screen (tanpa header/action-bar global).
 */
@Composable
fun HydrationStartedContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "ATUR SEKARANG !",
            style = MaterialTheme.typography.titleMedium,
            color = HydrolinxOnSurfaceMuted,
        )
        HydrationStartedIllustration(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Konfigurasi target minum harianmu",
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxOnSurface,
            )
            Text(
                text = "dan dapatkan jadwalnya",
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
        }
        Box(Modifier.height(4.dp))
    }
}
