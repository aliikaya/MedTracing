package com.medtracking.app.domain.model

/**
 * Represents a person whose medications are being tracked.
 * Examples: "Me", "Mom", "My Child", etc.
 */
data class Profile(
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String? = null, // Simple emoji avatar for Phase 1
    val relation: String? = null, // e.g., "Self", "Parent", "Child"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

