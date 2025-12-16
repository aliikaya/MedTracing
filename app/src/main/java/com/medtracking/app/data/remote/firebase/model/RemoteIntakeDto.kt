package com.medtracking.app.data.remote.firebase.model

data class RemoteIntakeDto(
    val id: String? = null,
    val profileId: String = "",
    val medicationId: String = "",
    val plannedTime: String = "",
    val takenTime: String? = null,
    val status: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val createdByUserId: String = "",
    val isDeleted: Boolean = false
)


