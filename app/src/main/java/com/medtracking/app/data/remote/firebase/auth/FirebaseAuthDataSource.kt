package com.medtracking.app.data.remote.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.medtracking.app.domain.model.AuthError
import com.medtracking.app.domain.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase implementation of AuthDataSource using Email/Password authentication.
 * Maps Firebase exceptions to domain-level AuthError.
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthDataSource {

    override suspend fun signIn(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user.toAuthUser())
            } else {
                Result.failure(Exception("Sign in succeeded but user is null"))
            }
        } catch (e: FirebaseAuthException) {
            Result.failure(e.toAuthError())
        } catch (e: Exception) {
            Result.failure(Exception(AuthError.Unknown(e.message).toString()))
        }
    }

    override suspend fun signUp(email: String, password: String): Result<AuthUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user.toAuthUser())
            } else {
                Result.failure(Exception("Sign up succeeded but user is null"))
            }
        } catch (e: FirebaseAuthException) {
            Result.failure(e.toAuthError())
        } catch (e: Exception) {
            Result.failure(Exception(AuthError.Unknown(e.message).toString()))
        }
    }

    override fun currentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toAuthUser()
    }

    override fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        
        // Emit current state immediately
        trySend(firebaseAuth.currentUser?.toAuthUser())
        
        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun com.google.firebase.auth.FirebaseUser.toAuthUser(): AuthUser {
        return AuthUser(
            uid = uid,
            email = email
        )
    }

    private fun FirebaseAuthException.toAuthError(): Exception {
        val authError = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> AuthError.InvalidEmail
            "ERROR_WRONG_PASSWORD" -> AuthError.WrongPassword
            "ERROR_USER_NOT_FOUND" -> AuthError.UserNotFound
            "ERROR_EMAIL_ALREADY_IN_USE" -> AuthError.EmailAlreadyInUse
            "ERROR_WEAK_PASSWORD" -> AuthError.WeakPassword
            "ERROR_NETWORK_REQUEST_FAILED" -> AuthError.NetworkError
            else -> AuthError.Unknown(message)
        }
        return Exception(authError.toString())
    }
}


