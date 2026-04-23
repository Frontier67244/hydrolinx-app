package com.mahendra.android.hydrolinx.feature.hydration.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mahendra.android.hydrolinx.core.notification.NotificationBus
import com.mahendra.android.hydrolinx.core.notification.NotificationEvent
import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import com.mahendra.android.hydrolinx.data.repository.LocalRepository
import com.mahendra.android.hydrolinx.feature.hydration.logic.HydrationRules
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationEvent
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationStep
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationToast
import com.mahendra.android.hydrolinx.feature.hydration.state.HydrationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * ViewModel tab Hydrobit.
 *
 * Alur:
 *  - init: cek profile. Kalau targetMl belum ada → step Started.
 *          Kalau ada → step Main + subscribe sesi hari ini.
 *  - SAVE target: persist → pindah ke Main + mulai subscribe sesi.
 *  - ConfirmDrink: hitung poin, insert sesi, addPoints, emit ke bus,
 *                  tambah toast ke list, buka NotifyPanel.
 */
class HydrationViewModel(
    private val repository: LocalRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HydrationUiState())
    val uiState: StateFlow<HydrationUiState> = _uiState.asStateFlow()

    private var sessionsCollector: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            val profile = repository.getUserProfile()
            val currentTarget = profile?.targetMl ?: 0
            val initialStep = if (currentTarget >= HydrationRules.TARGET_MIN_ML) {
                HydrationStep.Main
            } else {
                HydrationStep.Started
            }
            _uiState.update {
                it.copy(
                    currentStep = initialStep,
                    targetMl = currentTarget,
                    draftTargetMl = if (currentTarget >= HydrationRules.TARGET_MIN_ML) {
                        currentTarget
                    } else {
                        HydrationRules.TARGET_DEFAULT_ML
                    },
                )
            }
            subscribeTodaySessions()
        }
    }

    fun onEvent(event: HydrationEvent) {
        when (event) {
            HydrationEvent.TapLetsGo -> handleTapLetsGo()
            HydrationEvent.OpenConfigure -> handleOpenConfigure()
            is HydrationEvent.UpdateDraftTarget -> handleUpdateDraft(event.ml)
            HydrationEvent.SaveTarget -> handleSaveTarget()
            HydrationEvent.OpenDrinkSheet -> handleOpenDrinkSheet()
            HydrationEvent.CloseDrinkSheet -> _uiState.update { it.copy(isDrinkSheetOpen = false) }
            is HydrationEvent.SelectDrinkSize -> handleSelectDrinkSize(event.ml)
            HydrationEvent.ConfirmDrink -> handleConfirmDrink()
            HydrationEvent.ToggleHistoryPanel -> _uiState.update {
                it.copy(
                    isHistoryOpen = !it.isHistoryOpen,
                    isNotifyOpen = if (!it.isHistoryOpen) false else it.isNotifyOpen,
                )
            }
            HydrationEvent.ToggleNotifyPanel -> _uiState.update {
                it.copy(
                    isNotifyOpen = !it.isNotifyOpen,
                    isHistoryOpen = if (!it.isNotifyOpen) false else it.isHistoryOpen,
                )
            }
            HydrationEvent.DismissNotifyPanel -> _uiState.update { it.copy(isNotifyOpen = false) }
        }
    }

    // region Handlers

    private fun handleTapLetsGo() {
        _uiState.update {
            it.copy(
                currentStep = HydrationStep.Configure,
                draftTargetMl = if (it.targetMl >= HydrationRules.TARGET_MIN_ML) {
                    it.targetMl
                } else {
                    HydrationRules.TARGET_DEFAULT_ML
                },
            )
        }
    }

    private fun handleOpenConfigure() {
        _uiState.update {
            it.copy(
                currentStep = HydrationStep.Configure,
                draftTargetMl = if (it.targetMl >= HydrationRules.TARGET_MIN_ML) {
                    it.targetMl
                } else {
                    HydrationRules.TARGET_DEFAULT_ML
                },
            )
        }
    }

    private fun handleUpdateDraft(rawMl: Int) {
        val clamped = HydrationRules.clampTarget(rawMl)
        _uiState.update { it.copy(draftTargetMl = clamped) }
    }

    private fun handleSaveTarget() {
        val target = _uiState.value.draftTargetMl
        viewModelScope.launch {
            repository.setTargetMl(target)
            _uiState.update {
                it.copy(
                    targetMl = target,
                    currentStep = HydrationStep.Main,
                )
            }
            subscribeTodaySessions()
        }
    }

    private fun handleOpenDrinkSheet() {
        val snapshot = _uiState.value
        if (!snapshot.canDrinkToday) {
            _uiState.update { it.copy(isDrinkSheetOpen = false) }
            return
        }

        _uiState.update {
            it.copy(
                isDrinkSheetOpen = true,
                selectedDrinkSizeMl = drinkSizeForRemaining(
                    currentSize = it.selectedDrinkSizeMl,
                    remainingMl = it.remainingTodayMl,
                ),
            )
        }
    }

    private fun handleSelectDrinkSize(ml: Int) {
        _uiState.update {
            if (ml <= it.remainingTodayMl) {
                it.copy(selectedDrinkSizeMl = ml)
            } else {
                it
            }
        }
    }

    private fun handleConfirmDrink() {
        val snapshot = _uiState.value
        if (!snapshot.canDrinkToday) {
            _uiState.update { it.copy(isDrinkSheetOpen = false) }
            return
        }

        val size = snapshot.selectedDrinkSizeMl
            .coerceAtMost(snapshot.remainingTodayMl)
            .coerceAtLeast(0)
        if (size <= 0) {
            _uiState.update { it.copy(isDrinkSheetOpen = false) }
            return
        }

        val now = System.currentTimeMillis()
        val points = HydrationRules.mlToPoints(size)

        viewModelScope.launch {
            repository.insertHydrationSession(
                amountMl = size,
                pointsAwarded = points,
                epochMillis = now,
            )
            repository.addPoints(points)

            val label = punctualityLabel()
            val toast = HydrationToast(
                pointsAwarded = points,
                label = label,
                timeLabel = formatClock(now),
            )

            _uiState.update {
                it.copy(
                    isDrinkSheetOpen = false,
                    isNotifyOpen = true,
                    isHistoryOpen = false,
                    lastToast = toast,
                    toastLog = (listOf(toast) + it.toastLog).take(MAX_TOAST_LOG),
                )
            }

            NotificationBus.emit(
                NotificationEvent.HydrationLogged(
                    points = points,
                    timestampMs = now,
                ),
            )
        }
    }

    // endregion

    // region Streams

    private fun subscribeTodaySessions() {
        sessionsCollector?.cancel()
        sessionsCollector = viewModelScope.launch {
            repository.observeTodaySessions().collect { sessions ->
                _uiState.update {
                    it.copy(
                        todaySessions = sessions,
                        totalTodayMl = sessions.sumOf(HydrationSessionEntity::amountMl),
                    )
                }
            }
        }
    }

    // endregion

    // region Helpers

    private fun punctualityLabel(): String = "Tepat waktu!"

    private fun drinkSizeForRemaining(currentSize: Int, remainingMl: Int): Int {
        if (currentSize <= remainingMl) return currentSize
        return HydrationRules.DRINK_SIZE_OPTIONS_ML
            .filter { it <= remainingMl }
            .maxOrNull()
            ?: remainingMl
    }

    private fun formatClock(epochMillis: Long): String {
        return java.time.Instant
            .ofEpochMilli(epochMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
            .format(CLOCK_FORMAT)
    }

    // endregion

    companion object {
        private const val MAX_TOAST_LOG = 6
        private val CLOCK_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm")

        fun factory(repository: LocalRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HydrationViewModel(repository) as T
                }
            }
    }
}
