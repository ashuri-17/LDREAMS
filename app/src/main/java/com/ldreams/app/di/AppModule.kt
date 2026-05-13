package com.ldreams.app.di

import android.content.Context
import com.ldreams.app.data.database.AppDatabase
import com.ldreams.app.data.database.AchievementDao
import com.ldreams.app.data.database.DreamDao
import com.ldreams.app.data.database.RealityCheckDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDreamDao(database: AppDatabase): DreamDao {
        return database.dreamDao()
    }

    @Provides
    @Singleton
    fun provideRealityCheckDao(database: AppDatabase): RealityCheckDao {
        return database.realityCheckDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: AppDatabase): AchievementDao {
        return database.achievementDao()
    }
}
