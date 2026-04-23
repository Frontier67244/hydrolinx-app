package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Panel log sesi minum hari ini. Muncul ketika user menekan Insight lalu "History".
 * Untuk Fase 2: dipakai juga ketika user ingin melihat list minum.
 */
@Composable
fun HydrationHistoryPanel(
    sessions: List<HydrationSessionEntity>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, HydrolinxCyan.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Riwayat minum hari ini",
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxOnSurface,
                fontWeight = FontWeight.SemiBold,
            )
            if (sessions.isEmpty()) {
                EmptyHistoryRow()
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(sessions, key = { it.id }) { session ->
                        HistoryRow(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: HydrationSessionEntity) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = HydrolinxCyan.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.size(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = HydrolinxCyan.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp),
                ) {}
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = HydrolinxCyan,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${session.amountMl} Ml",
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "+${session.pointsAwarded} poin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurfaceMuted,
                )
            }
            Text(
                text = formatClock(session.sessionTimestampEpochMillis),
                style = MaterialTheme.typography.titleMedium,
                color = HydrolinxCyan,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EmptyHistoryRow() {
    Text(
        text = "Belum ada sesi minum hari ini. Yuk mulai dengan tombol MINUM.",
        style = MaterialTheme.typography.bodyMedium,
        color = HydrolinxOnSurfaceMuted,
    )
}

private val clockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")

private fun formatClock(epochMillis: Long): String {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalTime().format(clockFormatter)
}
