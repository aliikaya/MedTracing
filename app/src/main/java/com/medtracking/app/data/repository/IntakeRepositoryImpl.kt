package com.medtracking.app.data.repository

import com.medtracking.app.data.local.dao.IntakeDao
import com.medtracking.app.data.mapper.toDomain
import com.medtracking.app.data.mapper.toEntity
import com.medtracking.app.domain.model.Intake
import com.medtracking.app.domain.model.IntakeStatus
import com.medtracking.app.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class IntakeRepositoryImpl @Inject constructor(
    private val intakeDao: IntakeDao
) : IntakeRepository {

    override fun getIntakesForDate(profileId: Long, date: LocalDate): Flow<List<Intake>> {
        val datePattern = "$date%"
        return intakeDao.getIntakesForDate(profileId, datePattern).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIntakesForDateOnce(profileId: Long, date: LocalDate): List<Intake> {
        val datePattern = "$date%"
        return intakeDao.getIntakesForDateOnce(profileId, datePattern).map { it.toDomain() }
    }

    override fun getIntakesForMedication(medicationId: Long): Flow<List<Intake>> {
        return intakeDao.getIntakesForMedication(medicationId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getIntakesForMedicationAndDateRange(
        medicationId: Long,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Flow<List<Intake>> {
        // Room'da tarih karşılaştırması için ISO format kullan
        // fromDate 00:00'dan başlar, toDate+1 00:00'a kadar (exclusive)
        val fromDateTime = "${fromDate}T00:00:00"
        val toDateTime = "${toDate.plusDays(1)}T00:00:00"
        return intakeDao.getIntakesForMedicationAndDateRange(
            medicationId = medicationId,
            fromDate = fromDateTime,
            toDate = toDateTime
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addIntake(intake: Intake): Long {
        val now = System.currentTimeMillis()
        return intakeDao.insert(
            intake.copy(
                updatedAt = now,
                isDirty = true,
                isDeleted = false
            ).toEntity()
        )
    }

    override suspend fun addIntakes(intakes: List<Intake>) {
        val now = System.currentTimeMillis()
        intakeDao.insertAll(
            intakes.map {
                it.copy(
                    updatedAt = now,
                    isDirty = true,
                    isDeleted = false
                ).toEntity()
            }
        )
    }

    override suspend fun getIntakeByMedicationAndTime(
        medicationId: Long,
        plannedTime: LocalDateTime
    ): Intake? {
        return intakeDao.getIntakeByMedicationAndTime(
            medicationId = medicationId,
            plannedTime = plannedTime.toString()
        )?.toDomain()
    }

    override suspend fun markIntakeTaken(intakeId: Long, takenTime: LocalDateTime) {
        intakeDao.updateStatus(
            id = intakeId,
            status = IntakeStatus.TAKEN.name,
            takenTime = takenTime.toString(),
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun markIntakeMissed(intakeId: Long) {
        intakeDao.updateStatus(
            id = intakeId,
            status = IntakeStatus.MISSED.name,
            takenTime = null,
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteIntake(intakeId: Long) {
        intakeDao.softDeleteById(intakeId, System.currentTimeMillis())
    }

    override suspend fun deleteIntakesForMedication(medicationId: Long) {
        intakeDao.softDeleteForMedication(medicationId, System.currentTimeMillis())
    }
}
