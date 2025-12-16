package com.medtracking.app.data.remote.firebase.model

data class RemoteMedicationDto(
    val id: String? = null,
    val profileId: String = "",
    val name: String = "",
    val form: String = "",
    val dosageAmount: Double = 0.0,
    val dosageUnit: String = "",
    val scheduleTimes: List<String> = emptyList(),
    val startDate: String = "",
    val endDate: String? = null,
    val durationInDays: Int? = null,
    val importance: String = "",
    val mealRelation: String = "",
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val createdByUserId: String = "",
    val isDeleted: Boolean = false
)


