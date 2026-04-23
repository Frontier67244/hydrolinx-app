package com.mahendra.android.hydrolinx.feature.points.logic

/**
 * Aturan bisnis murni untuk wallet poin dan redeem token.
 * Tetap bebas dari Android/Compose agar bisa dipakai dan dites terpisah.
 */
object RedeemRules {

    const val POINTS_PER_TOKEN: Int = 100
    const val DAILY_ACTIVE_POINT_CAP: Int = 100
    const val BANK_POINT_CAP: Int = 300
    const val DAILY_REDEEM_LIMIT: Int = 1
    const val DAILY_TOKEN_USE_LIMIT: Int = 1

    val SCAN_VOLUME_OPTIONS_ML: List<Int> = listOf(250, 500, 1000)
    const val DEFAULT_SCAN_VOLUME_ML: Int = 500

    data class RedeemResult(
        val newActive: Int,
        val newBank: Int,
        val tokenGained: Int,
    )

    data class OverflowResult(
        val newActive: Int,
        val newBank: Int,
        val droppedPoints: Int,
    )

    fun calculateRedeem(
        activePoints: Int,
        bankPoints: Int,
    ): RedeemResult? {
        return when {
            activePoints >= POINTS_PER_TOKEN -> {
                RedeemResult(
                    newActive = activePoints - POINTS_PER_TOKEN,
                    newBank = bankPoints,
                    tokenGained = 1,
                )
            }

            bankPoints >= POINTS_PER_TOKEN -> {
                RedeemResult(
                    newActive = activePoints,
                    newBank = bankPoints - POINTS_PER_TOKEN,
                    tokenGained = 1,
                )
            }

            else -> null
        }
    }

    fun overflowToBank(
        incomingPoints: Int,
        currentActive: Int,
        currentBank: Int,
    ): OverflowResult {
        if (incomingPoints <= 0) {
            return OverflowResult(
                newActive = currentActive,
                newBank = currentBank,
                droppedPoints = 0,
            )
        }

        val activeRoom = (DAILY_ACTIVE_POINT_CAP - currentActive).coerceAtLeast(0)
        val toActive = incomingPoints.coerceAtMost(activeRoom)
        val activeAfter = currentActive + toActive

        val leftover = incomingPoints - toActive
        val bankRoom = (BANK_POINT_CAP - currentBank).coerceAtLeast(0)
        val toBank = leftover.coerceAtMost(bankRoom)
        val bankAfter = currentBank + toBank

        return OverflowResult(
            newActive = activeAfter,
            newBank = bankAfter,
            droppedPoints = incomingPoints - toActive - toBank,
        )
    }
}
