package com.mahendra.android.hydrolinx.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mahendra.android.hydrolinx.data.local.dao.HydrationSessionDao
import com.mahendra.android.hydrolinx.data.local.dao.PointWalletDao
import com.mahendra.android.hydrolinx.data.local.dao.UserProfileDao
import com.mahendra.android.hydrolinx.data.local.dao.WaterLocationDao
import com.mahendra.android.hydrolinx.data.local.entity.HydrationSessionEntity
import com.mahendra.android.hydrolinx.data.local.entity.PointWalletEntity
import com.mahendra.android.hydrolinx.data.local.entity.UserProfileEntity
import com.mahendra.android.hydrolinx.data.local.entity.WaterLocationEntity

@Database(
    entities = [
        WaterLocationEntity::class,
        HydrationSessionEntity::class,
        PointWalletEntity::class,
        UserProfileEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class HydrolinxDatabase : RoomDatabase() {

    abstract fun waterLocationDao(): WaterLocationDao
    abstract fun hydrationSessionDao(): HydrationSessionDao
    abstract fun pointWalletDao(): PointWalletDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        private const val DB_NAME = "hydrolinx.db"

        @Volatile
        private var INSTANCE: HydrolinxDatabase? = null

        fun getInstance(context: Context): HydrolinxDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HydrolinxDatabase::class.java,
                    DB_NAME,
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
