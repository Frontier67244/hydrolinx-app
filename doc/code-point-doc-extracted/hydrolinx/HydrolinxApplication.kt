package com.mahendra.android.hydrolinx

import android.app.Application
import android.util.Log
import com.mahendra.android.hydrolinx.data.local.db.HydrolinxDatabase
import com.mahendra.android.hydrolinx.data.repository.LocalRepository
import com.mahendra.android.hydrolinx.data.repository.LocalRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual DI root. Tidak pakai Hilt/Koin.
 * Semua Composable mengakses repository lewat `(LocalContext.current.applicationContext as HydrolinxApplication).repository`.
 */
class HydrolinxApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    lateinit var database: HydrolinxDatabase
        private set

    lateinit var repository: LocalRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = HydrolinxDatabase.getInstance(this)
        repository = LocalRepositoryImpl(
            waterLocationDao = database.waterLocationDao(),
            hydrationSessionDao = database.hydrationSessionDao(),
            pointWalletDao = database.pointWalletDao(),
            userProfileDao = database.userProfileDao(),
        )

        // Seed dummy locations saat pertama kali jalan, di background.
        appScope.launch {
            try {
                repository.ensureLocationsSeeded()
            } catch (t: Throwable) {
                Log.e(TAG, "seed failed", t)
            }
        }
    }

    companion object {
        private const val TAG = "HydrolinxApplication"
    }
}
