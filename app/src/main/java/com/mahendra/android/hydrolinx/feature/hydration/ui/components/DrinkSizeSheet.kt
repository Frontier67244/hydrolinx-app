package com.mahendra.android.hydrolinx.feature.hydration.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxBackground
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurface
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxOnSurfaceMuted
import com.mahendra.android.hydrolinx.feature.hydration.logic.HydrationRules

/**
 * Bottom sheet pilih ukuran minum: 100 / 200 / 300 / 400 ml.
 * Tombol MINUM [ X Ml ] → ConfirmDrink.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrinkSizeSheet(
    selectedMl: Int,
    remainingMl: Int,
    onSelectSize: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Water Size Drink",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = HydrolinxOnSurface,
            )
            Text(
                text = "Sisa target: $remainingMl Ml",
                style = MaterialTheme.typography.bodyMedium,
                color = HydrolinxOnSurfaceMuted,
            )

            SizeChipRow(
                selectedMl = selectedMl,
                remainingMl = remainingMl,
                onSelectSize = onSelectSize,
            )

            Button(
                onClick = onConfirm,
                enabled = remainingMl > 0 && selectedMl <= remainingMl,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HydrolinxCyan,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "  MINUM [ $selectedMl Ml ]",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SizeChipRow(
    selectedMl: Int,
    remainingMl: Int,
    onSelectSize: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HydrationRules.DRINK_SIZE_OPTIONS_ML.forEach { ml ->
            val isSelected = ml == selectedMl
            val enabled = ml <= remainingMl
            FilterChip(
                selected = isSelected,
                enabled = enabled,
                onClick = { onSelectSize(ml) },
                label = {
                    Text(
                        text = "+$ml",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    labelColor = HydrolinxOnSurface,
                    selectedContainerColor = HydrolinxCyan,
                    selectedLabelColor = Color.White,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = HydrolinxCyan.copy(alpha = 0.5f),
                    selectedBorderColor = HydrolinxCyan,
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp,
                ),
            )
        }
    }
}
