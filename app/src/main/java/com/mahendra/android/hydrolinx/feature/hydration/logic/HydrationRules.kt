package com.mahendra.android.hydrolinx.feature.hydration.logic

/**
 * Aturan hidrasi murni (tanpa Android / tanpa Compose). Dipanggil dari ViewModel
 * dan dari UI untuk menghitung progress / jumlah gelas.
 *
 * Kebijakan poin saat ini disetel ke mode demo:
 *  - Setiap sesi minum bernilai tetap 10 poin.
 *  - Dipilih agar alur presentasi lebih cepat terlihat.
 */
object HydrationRules {

    // Range target harian (ml).
    const val TARGET_MIN_ML: Int = 1500
    const val TARGET_MAX_ML: Int = 2500
    const val TARGET_DEFAULT_ML: Int = 2000
    const val TARGET_STEP_ML: Int = 100

    // Ukuran gelas standar yang dipakai untuk visualisasi target.
    const val ML_PER_GLASS: Int = 100

    // Pilihan sekali minum (harus kelipatan 100, match desain).
    val DRINK_SIZE_OPTIONS_ML: List<Int> = listOf(100, 200, 300, 400)
    const val DRINK_SIZE_DEFAULT_ML: Int = 100

    const val DEMO_POINTS_PER_DRINK: Int = 10

    /**
     * Batasi target ke grid step 100 ml dan clamp ke [MIN, MAX].
     */
    fun clampTarget(rawMl: Int): Int {
        val snapped = (rawMl / TARGET_STEP_ML) * TARGET_STEP_ML
        return snapped.coerceIn(TARGET_MIN_ML, TARGET_MAX_ML)
    }

    /**
     * Konversi ml → poin demo tetap.
     */
    fun mlToPoints(amountMl: Int): Int {
        if (amountMl <= 0) return 0
        return DEMO_POINTS_PER_DRINK
    }

    /**
     * Target → jumlah gelas (dibulatkan ke atas agar target tetap visible).
     */
    fun targetToGlassCount(targetMl: Int): Int {
        if (targetMl <= 0) return 0
        return (targetMl + ML_PER_GLASS - 1) / ML_PER_GLASS
    }

    /**
     * Jumlah bar untuk custom discrete slider = (MAX - MIN) / step + 1.
     */
    fun targetBarCount(): Int =
        (TARGET_MAX_ML - TARGET_MIN_ML) / TARGET_STEP_ML + 1

    /**
     * Mapping index bar (0..targetBarCount-1) → ml.
     */
    fun barIndexToMl(index: Int): Int =
        (TARGET_MIN_ML + index * TARGET_STEP_ML)
            .coerceIn(TARGET_MIN_ML, TARGET_MAX_ML)

    /**
     * Mapping ml → index bar terdekat.
     */
    fun mlToBarIndex(ml: Int): Int {
        val clamped = clampTarget(ml)
        return (clamped - TARGET_MIN_ML) / TARGET_STEP_ML
    }

    /**
     * Progress 0f..1f (di-clamp supaya tidak > 1).
     */
    fun progress(totalMl: Int, targetMl: Int): Float {
        if (targetMl <= 0) return 0f
        val p = totalMl.toFloat() / targetMl.toFloat()
        return p.coerceIn(0f, 1f)
    }
}
