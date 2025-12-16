package com.medtracking.app.data.mapper

import com.medtracking.app.data.local.entity.MedicationEntity
import com.medtracking.app.domain.model.*
import java.time.LocalDate

fun MedicationEntity.toDomain(): Medication {
    return Medication(
        id = id,
        profileId = profileId,
        name = name,
        form = MedicationForm.valueOf(form),
        dosage = Dosage.fromStorageString(dosage),
        schedule = MedicationSchedule.fromStorageString(scheduleTimes),
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        durationInDays = durationInDays,
        isActive = isActive,
        importance = MedicationImportance.valueOf(importance),
        mealRelation = MealRelation.fromString(mealRelation),
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt,
        remoteId = remoteId,
        profileRemoteId = profileRemoteId,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted
    )
}

fun Medication.toEntity(): MedicationEntity {
    return MedicationEntity(
        id = id,
        profileId = profileId,
        name = name,
        form = form.name,
        dosage = dosage.toStorageString(),
        scheduleTimes = schedule.toStorageString(),
        startDate = startDate.toString(),
        endDate = endDate?.toString(),
        durationInDays = durationInDays,
        isActive = isActive,
        importance = importance.name,
        mealRelation = mealRelation.name,
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt,
        remoteId = remoteId,
        profileRemoteId = profileRemoteId,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted
    )
}
