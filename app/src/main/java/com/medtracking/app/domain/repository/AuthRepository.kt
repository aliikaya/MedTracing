package com.medtracking.app.domain.repository

import com.medtracking.app.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level repository for authentication operations.
 * This interface is framework-agnostic and used by use cases and ViewModels.
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthUser>
    suspend fun register(email: String, password: String): Result<AuthUser>
    fun observeAuthState(): Flow<AuthUser?>
    fun currentUser(): AuthUser?
    suspend fun logout()
}

