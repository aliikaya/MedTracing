package com.medtracking.app.data.remote.firebase.model

/**
 * Firestore DTO for profile documents.
 * 
 * members: Map<uid, roleName>
 * Example: {"uid_owner": "OWNER", "uid_mother": "PATIENT_MARK_ONLY"}
 */
data class RemoteProfileDto(
    val id: String? = null,
    val name: String = "",
    val ownerUserId: String = "",
    val members: Map<String, String> = emptyMap(), // uid -> role name
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isShared: Boolean = false,
    val isDeleted: Boolean = false
)


