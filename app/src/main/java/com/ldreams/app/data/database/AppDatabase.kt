package com.ldreams.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ldreams.app.data.models.Achievement
import com.ldreams.app.data.models.DreamEntry
import com.ldreams.app.data.models.RealityCheck

@Database(
    entities = [DreamEntry::class, RealityCheck::class, Achievement::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dreamDao(): DreamDao
    abstract fun realityCheckDao(): RealityCheckDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ldreams_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
