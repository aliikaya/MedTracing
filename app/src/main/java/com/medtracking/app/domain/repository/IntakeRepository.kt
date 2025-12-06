package com.medtracking.app.domain.repository

import com.medtracking.app.domain.model.Intake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface IntakeRepository {
    fun getIntakesForDate(profileId: Long, date: LocalDate): Flow<List<Intake>>
    suspend fun getIntakesForDateOnce(profileId: Long, date: LocalDate): List<Intake>
    fun getIntakesForMedication(medicationId: Long): Flow<List<Intake>>
    fun getIntakesForMedicationAndDateRange(
        medicationId: Long,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Flow<List<Intake>>
    suspend fun addIntake(intake: Intake): Long
    suspend fun addIntakes(intakes: List<Intake>)
    suspend fun getIntakeByMedicationAndTime(medicationId: Long, plannedTime: LocalDateTime): Intake?
    suspend fun markIntakeTaken(intakeId: Long, takenTime: LocalDateTime)
    suspend fun markIntakeMissed(intakeId: Long)
    suspend fun deleteIntake(intakeId: Long)
    suspend fun deleteIntakesForMedication(medicationId: Long)
}
