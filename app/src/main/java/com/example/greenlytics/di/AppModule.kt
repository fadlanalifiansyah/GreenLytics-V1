package com.example.greenlytics.di

import android.content.Context
import com.example.greenlytics.data.local.EmissionDao
import com.example.greenlytics.data.local.GreenLyticsDatabase // PASTIKAN INI ADA
import com.example.greenlytics.data.repository.EmissionRepo
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
    fun provideDatabase(@ApplicationContext context: Context): GreenLyticsDatabase {
        return GreenLyticsDatabase.getDatabase(context)
    }

    @Provides
    fun provideEmissionDao(database: GreenLyticsDatabase): EmissionDao {
        return database.emissionDao()
    }

    @Provides
    @Singleton
    fun provideEmissionRepo(dao: EmissionDao): EmissionRepo {
        return EmissionRepo(dao)
    }
}