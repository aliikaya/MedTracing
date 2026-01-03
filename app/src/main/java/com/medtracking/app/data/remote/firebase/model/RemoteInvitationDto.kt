package com.medtracking.app.data.remote.firebase.model

/**
 * Firestore DTO for invitation documents.
 * 
 * grantRole: The role that will be granted to the user who accepts this invitation.
 * status: PENDING, ACCEPTED, CANCELED, EXPIRED
 */
data class RemoteInvitationDto(
    val id: String? = null,
    val profileId: String = "",
    val inviterUserId: String = "",
    val oneTimeToken: String = "",
    val grantRole: String = "VIEWER", // Role to grant on acceptance
    val status: String = "PENDING",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val acceptedByUid: String? = null,
    val acceptedAt: Long? = null
)


