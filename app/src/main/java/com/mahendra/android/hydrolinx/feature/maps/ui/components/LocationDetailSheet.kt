package com.mahendra.android.hydrolinx.feature.maps.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxGreen
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxPink
import com.mahendra.android.hydrolinx.feature.maps.state.FoundLocation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailSheet(
    location: FoundLocation,
    onOpenRoute: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            DetailHeader(location = location)
            AddressCopyCard(
                address = location.entity.address,
                onCopy = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(
                        ClipData.newPlainText("Hydrolinx address", location.entity.address),
                    )
                    Toast.makeText(context, "Alamat disalin", Toast.LENGTH_SHORT).show()
                },
            )
            StatusStrip(location = location)
            Button(
                onClick = onOpenRoute,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HydrolinxCyan),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Navigation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "  Buka Rute",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DetailHeader(location: FoundLocation) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = HydrolinxCyan.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp),
            ) {}
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = HydrolinxCyan,
                modifier = Modifier.size(26.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = location.entity.name,
                style = MaterialTheme.typography.headlineSmall,
                color = HydrolinxOnSurface,
            )
            Text(
                text = "${location.distanceM.toInt()} meter dari lokasi kamu",
                style = MaterialTheme.typography.labelLarge,
                color = HydrolinxCyan,
            )
        }
    }
}

@Composable
private fun AddressCopyCard(
    address: String,
    onCopy: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = HydrolinxCyan.copy(alpha = 0.14f),
        onClick = onCopy,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurface,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Filled.ContentCopy,
                contentDescription = "Salin alamat",
                tint = HydrolinxCyan,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun StatusStrip(location: FoundLocation) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusTile(
            modifier = Modifier.weight(1f),
            title = if (location.entity.isOpen) "Buka" else "Tutup",
            subtitle = "Status",
            color = if (location.entity.isOpen) HydrolinxGreen else HydrolinxPink,
        )
        StatusTile(
            modifier = Modifier.weight(1f),
            title = if (location.entity.hasCapacity) "Tersedia" else "Penuh",
            subtitle = "Kapasitas",
            color = if (location.entity.hasCapacity) HydrolinxGreen else HydrolinxPink,
        )
    }
}

@Composable
private fun StatusTile(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    color: Color,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )
        }
    }
}

fun openMapsRoute(
    context: Context,
    lat: Double,
    lon: Double,
) {
    val uri = buildMapsRouteUri(lat = lat, lon = lon)
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
    runCatching { context.startActivity(mapsIntent) }
        .onFailure { context.startActivity(fallbackIntent) }
}

private fun buildMapsRouteUri(lat: Double, lon: Double): Uri =
    Uri.parse(
        "https://www.google.com/maps/dir/?api=1" +
            "&destination=$lat,$lon" +
            "&travelmode=driving",
    )
