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
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val ownerUserId: String? = null,
    val isShared: Boolean = false,
    val updatedAt: Long = createdAt,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
    // Members map: uid -> role
    val members: Map<String, MemberRole> = emptyMap(),
    // Current user's role in this profile (computed from members + current auth user)
    val myRole: MemberRole? = null
)

