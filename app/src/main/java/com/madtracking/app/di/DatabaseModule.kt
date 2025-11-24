package com.madtracking.app.di

import android.content.Context
import androidx.room.Room
import com.madtracking.app.data.local.MedTrackDatabase
import com.madtracking.app.data.local.dao.IntakeDao
import com.madtracking.app.data.local.dao.MedicationDao
import com.madtracking.app.data.local.dao.ProfileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMedTrackDatabase(
        @ApplicationContext context: Context
    ): MedTrackDatabase {
        return Room.databaseBuilder(
            context,
            MedTrackDatabase::class.java,
            MedTrackDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // For Phase 1, simple migration strategy
            .build()
    }

    @Provides
    @Singleton
    fun provideProfileDao(database: MedTrackDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    @Singleton
    fun provideMedicationDao(database: MedTrackDatabase): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    @Singleton
    fun provideIntakeDao(database: MedTrackDatabase): IntakeDao {
        return database.intakeDao()
    }
}

