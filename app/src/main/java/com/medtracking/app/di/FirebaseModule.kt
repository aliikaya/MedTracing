package com.medtracking.app.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medtracking.app.data.remote.firebase.FirebaseInvitationDataSource
import com.medtracking.app.data.remote.firebase.FirebaseIntakeDataSource
import com.medtracking.app.data.remote.firebase.FirebaseMedicationDataSource
import com.medtracking.app.data.remote.firebase.FirebaseProfileDataSource
import com.medtracking.app.data.remote.firebase.RemoteInvitationDataSource
import com.medtracking.app.data.remote.firebase.RemoteIntakeDataSource
import com.medtracking.app.data.remote.firebase.RemoteMedicationDataSource
import com.medtracking.app.data.remote.firebase.RemoteProfileDataSource
import com.medtracking.app.data.remote.firebase.auth.AuthDataSource
import com.medtracking.app.data.remote.firebase.auth.FirebaseAuthDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseBindingsModule {

    @Binds
    @Singleton
    abstract fun bindAuthDataSource(impl: FirebaseAuthDataSource): AuthDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteProfileDataSource(impl: FirebaseProfileDataSource): RemoteProfileDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteMedicationDataSource(impl: FirebaseMedicationDataSource): RemoteMedicationDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteIntakeDataSource(impl: FirebaseIntakeDataSource): RemoteIntakeDataSource

    @Binds
    @Singleton
    abstract fun bindRemoteInvitationDataSource(impl: FirebaseInvitationDataSource): RemoteInvitationDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}


