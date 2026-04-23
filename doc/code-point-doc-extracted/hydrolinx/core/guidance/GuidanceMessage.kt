package com.mahendra.android.hydrolinx.core.guidance

sealed class GuidanceMessage(
    val greeting: String,
    val question: String,
    val action: String,
) {
    data object MapsHome : GuidanceMessage(
        greeting = "Halo, Healthy!",
        question = "Sudah tahu sumber air terdekat?",
        action = "Tap EXPLORE untuk cari.",
    )

    data object HydrationHomeEmpty : GuidanceMessage(
        greeting = "Halo, Healthy!",
        question = "Belum minum hari ini?",
        action = "Yuk mulai minum sekarang.",
    )

    data object HydrationHomeProgress : GuidanceMessage(
        greeting = "Halo, Healthy!",
        question = "Sudah minum cukup?",
        action = "Lanjutkan sampai target.",
    )

    data object PointsReady : GuidanceMessage(
        greeting = "Halo, Healthy!",
        question = "Poinmu sudah cukup?",
        action = "Redeem sekarang.",
    )

    data object PointsLow : GuidanceMessage(
        greeting = "Halo, Healthy!",
        question = "Masih kurang poin?",
        action = "Minum lagi untuk dapat poin.",
    )
}
