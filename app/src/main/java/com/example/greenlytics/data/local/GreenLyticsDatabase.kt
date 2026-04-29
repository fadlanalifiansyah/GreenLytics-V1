package com.example.greenlytics.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EmissionEntity::class], version = 1, exportSchema = false)
abstract class GreenLyticsDatabase : RoomDatabase() { // HARUS ADA BARIS INI
    abstract fun emissionDao(): EmissionDao

    companion object {
        @Volatile
        private var INSTANCE: GreenLyticsDatabase? = null

        fun getDatabase(context: Context): GreenLyticsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GreenLyticsDatabase::class.java,
                    "greenlytics_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}