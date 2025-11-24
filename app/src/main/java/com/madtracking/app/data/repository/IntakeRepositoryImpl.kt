package com.madtracking.app.data.repository

import com.madtracking.app.data.local.dao.IntakeDao
import com.madtracking.app.data.mapper.toDomain
import com.madtracking.app.data.mapper.toEntity
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class IntakeRepositoryImpl @Inject constructor(
    private val intakeDao: IntakeDao
) : IntakeRepository {

    override fun getIntakesForDate(profileId: Long, date: LocalDate): Flow<List<Intake>> {
        // Query pattern: "2024-01-15%" to match all times on that date
        val datePattern = "$date%"
        return intakeDao.getIntakesForDate(profileId, datePattern).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getIntakesForMedication(medicationId: Long): Flow<List<Intake>> {
        return intakeDao.getIntakesForMedication(medicationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addIntake(intake: Intake): Long {
        return intakeDao.insert(intake.toEntity())
    }

    override suspend fun markIntakeTaken(intakeId: Long, takenTime: LocalDateTime) {
        intakeDao.updateStatus(
            id = intakeId,
            status = IntakeStatus.TAKEN.name,
            takenTime = takenTime.toString()
        )
    }

    override suspend fun markIntakeMissed(intakeId: Long) {
        intakeDao.updateStatus(
            id = intakeId,
            status = IntakeStatus.MISSED.name,
            takenTime = null
        )
    }

    override suspend fun deleteIntake(intakeId: Long) {
        intakeDao.deleteById(intakeId)
    }

    override suspend fun deleteIntakesForMedication(medicationId: Long) {
        intakeDao.deleteForMedication(medicationId)
    }
}

