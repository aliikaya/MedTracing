package com.medtracking.app.di

import com.medtracking.app.data.repository.IntakeRepositoryImpl
import com.medtracking.app.data.repository.MedicationRepositoryImpl
import com.medtracking.app.data.repository.ProfileRepositoryImpl
import com.medtracking.app.domain.repository.IntakeRepository
import com.medtracking.app.domain.repository.MedicationRepository
import com.medtracking.app.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindMedicationRepository(
        medicationRepositoryImpl: MedicationRepositoryImpl
    ): MedicationRepository

    @Binds
    @Singleton
    abstract fun bindIntakeRepository(
        intakeRepositoryImpl: IntakeRepositoryImpl
    ): IntakeRepository
}

