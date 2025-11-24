package com.madtracking.app.domain.model

import java.time.LocalDate

/**
 * Represents a medication or supplement that needs to be tracked.
 */
data class Medication(
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val form: MedicationForm,
    val dosage: Dosage,
    val schedule: MedicationSchedule,
    val startDate: LocalDate,
    val endDate: LocalDate? = null, // null for long-term/indefinite medications
    val isActive: Boolean = true,
    val importance: MedicationImportance = MedicationImportance.REGULAR,
    val notes: String? = null,
    val stockCount: Int? = null, // For future stock tracking
    val createdAt: Long = System.currentTimeMillis()
)

