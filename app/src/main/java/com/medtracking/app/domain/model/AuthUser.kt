package com.medtracking.app.domain.model

/**
 * Domain model representing an authenticated user.
 * This is framework-agnostic and used across all layers.
 */
data class AuthUser(
    val uid: String,
    val email: String?
)

