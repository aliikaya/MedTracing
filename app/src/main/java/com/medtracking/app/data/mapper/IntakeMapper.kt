package com.medtracking.app.data.mapper

import com.medtracking.app.data.local.entity.IntakeEntity
import com.medtracking.app.domain.model.Intake
import com.medtracking.app.domain.model.IntakeStatus
import java.time.LocalDateTime

fun IntakeEntity.toDomain(): Intake {
    return Intake(
        id = id,
        medicationId = medicationId,
        profileId = profileId,
        plannedTime = LocalDateTime.parse(plannedTime),
        takenTime = takenTime?.let { LocalDateTime.parse(it) },
        status = IntakeStatus.valueOf(status),
        notes = notes,
        createdAt = createdAt,
        remoteId = remoteId,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted
    )
}

fun Intake.toEntity(): IntakeEntity {
    return IntakeEntity(
        id = id,
        medicationId = medicationId,
        profileId = profileId,
        plannedTime = plannedTime.toString(),
        takenTime = takenTime?.toString(),
        status = status.name,
        notes = notes,
        createdAt = createdAt,
        remoteId = remoteId,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted
    )
}

