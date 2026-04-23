package com.mahendra.android.hydrolinx.feature.maps.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxGreen
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxPink
import com.mahendra.android.hydrolinx.feature.maps.state.FoundLocation

@Composable
fun PinPopup(
    location: FoundLocation,
    onOpenDetail: () -> Unit,
    onOpenRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.widthIn(max = 248.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            shadowElevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = location.entity.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = HydrolinxOnSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PopupMeta(
                        modifier = Modifier.weight(1f),
                        label = "Status",
                        value = if (location.entity.isOpen) "Buka" else "Tutup",
                        color = if (location.entity.isOpen) HydrolinxGreen else HydrolinxPink,
                    )
                    PopupMeta(
                        modifier = Modifier.weight(1f),
                        label = "Kapasitas",
                        value = if (location.entity.hasCapacity) "Tersedia" else "Penuh",
                        color = if (location.entity.hasCapacity) HydrolinxGreen else HydrolinxPink,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onOpenDetail,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HydrolinxCyan,
                        ),
                    ) {
                        Text(
                            text = "DETAIL",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Button(
                        onClick = onOpenRoute,
                        modifier = Modifier.weight(1.15f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HydrolinxCyan,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = "ROUTE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Box(modifier = Modifier.size(6.dp))
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Filled.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .padding(top = 1.dp)
                .size(width = 20.dp, height = 10.dp),
        ) {
            val pointer = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(
                path = pointer,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun PopupMeta(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HydrolinxOnSurfaceMuted,
        )
        Surface(
            shape = RoundedCornerShape(50),
            color = color.copy(alpha = 0.15f),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            )
        }
    }
}
