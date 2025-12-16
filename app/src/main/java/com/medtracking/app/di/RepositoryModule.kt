package com.medtracking.app.di

import com.medtracking.app.data.repository.AuthRepositoryImpl
import com.medtracking.app.data.repository.IntakeRepositoryImpl
import com.medtracking.app.data.repository.MedicationRepositoryImpl
import com.medtracking.app.data.repository.ProfileRepositoryImpl
import com.medtracking.app.data.repository.ProfileSharingRepositoryImpl
import com.medtracking.app.domain.repository.AuthRepository
import com.medtracking.app.domain.repository.IntakeRepository
import com.medtracking.app.domain.repository.MedicationRepository
import com.medtracking.app.domain.repository.ProfileRepository
import com.medtracking.app.domain.repository.ProfileSharingRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

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

    @Binds
    @Singleton
    abstract fun bindProfileSharingRepository(
        profileSharingRepositoryImpl: ProfileSharingRepositoryImpl
    ): ProfileSharingRepository
}

