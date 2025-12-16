package com.medtracking.app.data.repository

import com.medtracking.app.data.remote.firebase.auth.AuthDataSource
import com.medtracking.app.domain.model.AuthUser
import com.medtracking.app.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementation of AuthRepository that delegates to AuthDataSource.
 * Acts as a bridge between domain and data layers.
 */
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthUser> {
        return authDataSource.signIn(email, password)
    }

    override suspend fun register(email: String, password: String): Result<AuthUser> {
        return authDataSource.signUp(email, password)
    }

    override fun observeAuthState(): Flow<AuthUser?> {
        return authDataSource.observeAuthState()
    }

    override fun currentUser(): AuthUser? {
        return authDataSource.currentUser()
    }

    override suspend fun logout() {
        authDataSource.signOut()
    }
}

