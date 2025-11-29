package com.madtracking.app.data.mapper

import com.madtracking.app.data.local.entity.MedicationEntity
import com.madtracking.app.domain.model.*
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
        isActive = isActive,
        importance = MedicationImportance.valueOf(importance),
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt
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
        isActive = isActive,
        importance = importance.name,
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt
    )
}
