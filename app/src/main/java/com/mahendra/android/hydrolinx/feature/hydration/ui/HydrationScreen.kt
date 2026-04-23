package com.mahendra.android.hydrolinx.feature.hydration.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahendra.android.hydrolinx.HydrolinxApplication
import com.mahendra.android.hydrolinx.core.guidance.GuidanceMessage
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxBackground
import com.mahendra.android.hydrolinx.core.ui.theme.HydrolinxCyan
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationEvent
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationStep
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationUiState
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.ConfigureBigTargetCard
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.ConfigureGlassCard
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.ConfigureSliderCard
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.DrinkSizeSheet
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.GreetingBubble
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationHeader
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationHistoryPanel
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationMainCard
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationNotifyPanel
import com.mahendra.android.hydrolinx.feature.hydration.ui.components.HydrationStartedContent
// HydrationProgressRing sekarang dipakai via HydrationMainCard, tidak perlu diimpor di sini.

@Composable
fun HydrationScreen() {
    val context = LocalContext.current
    val repository = remember(context) {
        (context.applicationContext as HydrolinxApplication).repository
    }
    val viewModel: com.mahendra.android.hydrolinx.feature.hydration.viewmodel.HydrationViewModel = viewModel(
        factory = com.mahendra.android.hydrolinx.feature.hydration.viewmodel.HydrationViewModel.factory(repository),
    )
    val state by viewModel.uiState.collectAsState()

    HydrationScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
    )

    if (state.isDrinkSheetOpen && state.canDrinkToday) {
        DrinkSizeSheet(
            selectedMl = state.selectedDrinkSizeMl,
            remainingMl = state.remainingTodayMl,
            onSelectSize = { viewModel.onEvent(HydrationEvent.SelectDrinkSize(it)) },
            onConfirm = { viewModel.onEvent(HydrationEvent.ConfirmDrink) },
            onDismiss = { viewModel.onEvent(HydrationEvent.CloseDrinkSheet) },
        )
    }
}

@Composable
private fun HydrationScreenContent(
    state: HydrationUiState,
    onEvent: (HydrationEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HydrolinxBackground),
    ) {
        when (state.currentStep) {
            HydrationStep.Configure -> ConfigureStep(state, onEvent)
            HydrationStep.Started -> StartedStep(onEvent)
            HydrationStep.Main -> MainStep(state, onEvent)
        }
    }
}

/**
 * Configure step dipisah dari scroll karena user harus bisa mengakses slider
 * dengan satu tangan tanpa scroll. Layout bottom-heavy:
 *   [Header]
 *   [GlassCard]
 *   [BigTargetCard]
 *   Spacer(weight=1f)   ← mendorong interaktif ke bawah
 *   [SliderCard]
 *   [CONFIRM]
 */
@Composable
private fun ConfigureStep(
    state: HydrationUiState,
    onEvent: (HydrationEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HydrationHeader(section = "Hydration", subsection = "Configure")

        // --- Info cluster (atas) ---
        ConfigureGlassCard(targetMl = state.draftTargetMl)
        ConfigureBigTargetCard(targetMl = state.draftTargetMl)

        // --- Spacer mendorong interactive cluster ke bawah ---
        Spacer(Modifier.weight(1f))

        // --- Interactive cluster (bawah, thumb-zone) ---
        ConfigureSliderCard(
            draftTargetMl = state.draftTargetMl,
            onDraftChange = { onEvent(HydrationEvent.UpdateDraftTarget(it)) },
        )
        OutlinedButton(
            onClick = { onEvent(HydrationEvent.SaveTarget) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, HydrolinxCyan),
        ) {
            Text(
                text = "CONFIRM",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = HydrolinxCyan,
            )
        }
    }
}

/**
 * Started step: tidak perlu scroll, konten terpusat.
 */
@Composable
private fun StartedStep(onEvent: (HydrationEvent) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HydrationHeader(section = "Hydration", subsection = "Started")
        Spacer(Modifier.weight(0.4f))
        HydrationStartedContent()
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { onEvent(HydrationEvent.TapLetsGo) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HydrolinxCyan),
        ) {
            Text(
                text = "LET'S GO",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
    }
}

/**
 * Main step layout:
 *   [Header]
 *   [GreetingBubble]
 *   [HydrationMainCard]
 *   [Overlay notify/history]  ← floating, tidak mendorong layout
 */
@Composable
private fun MainStep(
    state: HydrationUiState,
    onEvent: (HydrationEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HydrationHeader(
            section = "Hydration",
            subsection = "Drink Act",
            modifier = Modifier.fillMaxWidth(),
        )
        GreetingBubble(message = pickGuidance(state))
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            HydrationMainCard(
                progress = state.progress,
                totalTodayMl = state.totalTodayMl,
                canDrink = state.canDrinkToday,
                notifyActive = state.isNotifyOpen,
                insightActive = state.isHistoryOpen,
                onSwipeToDrink = { onEvent(HydrationEvent.OpenDrinkSheet) },
                onNotifyClick = { onEvent(HydrationEvent.ToggleNotifyPanel) },
                onInsightClick = { onEvent(HydrationEvent.ToggleHistoryPanel) },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = state.isNotifyOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(0.84f)
                    .padding(start = 10.dp, bottom = 142.dp)
                    .zIndex(2f),
            ) {
                HydrationNotifyPanel(
                    toasts = state.toastLog,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = state.isHistoryOpen,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 3 }),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(0.84f)
                    .padding(end = 10.dp, bottom = 142.dp)
                    .zIndex(2f),
            ) {
                HydrationHistoryPanel(
                    sessions = state.todaySessions,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun pickGuidance(state: HydrationUiState): GuidanceMessage {
    return if (state.totalTodayMl <= 0) {
        GuidanceMessage.HydrationHomeEmpty
    } else {
        GuidanceMessage.HydrationHomeProgress
    }
}
