package com.madtracking.app.di

import com.madtracking.app.data.repository.IntakeRepositoryImpl
import com.madtracking.app.data.repository.MedicationRepositoryImpl
import com.madtracking.app.data.repository.ProfileRepositoryImpl
import com.madtracking.app.domain.repository.IntakeRepository
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.repository.ProfileRepository
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

