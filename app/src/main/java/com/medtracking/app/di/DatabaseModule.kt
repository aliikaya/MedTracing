package com.medtracking.app.di

import android.content.Context
import androidx.room.Room
import com.medtracking.app.data.local.MedTrackDatabase
import com.medtracking.app.data.local.dao.IntakeDao
import com.medtracking.app.data.local.dao.MedicationDao
import com.medtracking.app.data.local.dao.ProfileDao
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
            .addMigrations(*MedTrackDatabase.getMigrations())
            .fallbackToDestructiveMigration() // Fallback if migration fails
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
