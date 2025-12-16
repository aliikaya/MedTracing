package com.medtracking.app.data.remote.firebase.auth

import com.medtracking.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for authentication operations.
 * Implementations handle Firebase Auth with Email/Password.
 */
interface AuthDataSource {
    suspend fun signIn(email: String, password: String): Result<AuthUser>
    suspend fun signUp(email: String, password: String): Result<AuthUser>
    fun currentUser(): AuthUser?
    fun observeAuthState(): Flow<AuthUser?>
    suspend fun signOut()
}


