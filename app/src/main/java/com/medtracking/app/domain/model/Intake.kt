package com.medtracking.app.domain.model

import java.time.LocalDateTime

/**
 * Represents a single planned or actual intake of a medication.
 * Each medication's schedule generates multiple Intake records.
 */
data class Intake(
    val id: Long = 0,
    val medicationId: Long,
    val profileId: Long,
    val plannedTime: LocalDateTime,
    val takenTime: LocalDateTime? = null,
    val status: IntakeStatus = IntakeStatus.PLANNED,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val updatedAt: Long = createdAt,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false
)

