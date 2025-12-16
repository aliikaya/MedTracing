package com.medtracking.app.domain.model

/**
 * Sealed class representing authentication errors.
 * Framework-agnostic error types for the domain layer.
 */
sealed class AuthError {
    data object InvalidEmail : AuthError()
    data object WrongPassword : AuthError()
    data object UserNotFound : AuthError()
    data object EmailAlreadyInUse : AuthError()
    data object WeakPassword : AuthError()
    data object NetworkError : AuthError()
    data class Unknown(val message: String?) : AuthError()
}

