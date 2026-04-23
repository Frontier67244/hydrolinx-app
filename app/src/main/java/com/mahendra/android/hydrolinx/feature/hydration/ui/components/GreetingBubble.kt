package com.mahendra.android.hydrolinx.feature.hydration.ui.components

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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.guidance.GuidanceMessage
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOrange

/**
 * Bubble greeting di atas ring (match desain: "Halo, Healthy / Belum minum? ... 💡").
 * Konten diambil dari [GuidanceMessage] (bukan hardcoded).
 */
@Composable
fun GreetingBubble(
    message: GuidanceMessage,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        val parts = message.greeting.split(",", limit = 2)
                        if (parts.size == 2) {
                            append(parts[0])
                            append(", ")
                            withStyle(
                                SpanStyle(
                                    color = HydrolinxCyan,
                                    fontWeight = FontWeight.Bold,
                                ),
                            ) {
                                append(parts[1].trim())
                            }
                        } else {
                            append(message.greeting)
                        }
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = HydrolinxOnSurface,
                )
                Text(
                    text = message.question,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurface,
                )
                Text(
                    text = message.action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = HydrolinxOnSurfaceMuted,
                )
            }
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = HydrolinxOrange.copy(alpha = 0.18f),
                    modifier = Modifier.size(40.dp),
                ) {}
                Icon(
                    imageVector = Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = HydrolinxOrange,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}
