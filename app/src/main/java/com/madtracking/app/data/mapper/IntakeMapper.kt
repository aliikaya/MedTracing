package com.madtracking.app.data.mapper

import com.madtracking.app.data.local.entity.IntakeEntity
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
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
        createdAt = createdAt
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
        createdAt = createdAt
    )
}

