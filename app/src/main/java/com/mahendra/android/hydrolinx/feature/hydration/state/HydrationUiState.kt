package com.mahendra.android.hydrolinx.feature.hydration.state

import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import com.mahendra.android.hydrolinx.feature.hydration.logic.HydrationRules

/**
 * 3 sub-mode utama Hydration tab.
 *  - Started   : user belum pernah set target (onboarding singkat)
 *  - Configure : user sedang set / ubah target harian
 *  - Main      : layar utama — ring progress + action bar
 */
enum class HydrationStep { Started, Configure, Main }

/**
 * Notifikasi mini (shifting hydration) yang muncul setelah confirm minum.
 * Fase 2: hanya base point dulu, bonus masih dummy copy.
 */
data class HydrationToast(
    val pointsAwarded: Int,
    val label: String,          // contoh: "Tepat waktu"
    val timeLabel: String,      // "09.30"
)

data class HydrationUiState(
    val currentStep: HydrationStep = HydrationStep.Started,

    // Target
    val targetMl: Int = 0,
    val draftTargetMl: Int = HydrationRules.TARGET_DEFAULT_ML,

    // Hari ini
    val totalTodayMl: Int = 0,
    val todaySessions: List<HydrationSessionEntity> = emptyList(),

    // Drink sheet
    val isDrinkSheetOpen: Boolean = false,
    val selectedDrinkSizeMl: Int = HydrationRules.DRINK_SIZE_DEFAULT_ML,

    // Overlay panels
    val isHistoryOpen: Boolean = false,
    val isNotifyOpen: Boolean = false,

    // Notifikasi terakhir (untuk ditampilkan di NotifyPanel)
    val lastToast: HydrationToast? = null,
    val toastLog: List<HydrationToast> = emptyList(),
) {
    val progress: Float
        get() = HydrationRules.progress(totalTodayMl, targetMl)

    val isTargetConfigured: Boolean
        get() = targetMl >= HydrationRules.TARGET_MIN_ML

    val remainingTodayMl: Int
        get() = (targetMl - totalTodayMl).coerceAtLeast(0)

    val canDrinkToday: Boolean
        get() = isTargetConfigured && remainingTodayMl > 0
}
