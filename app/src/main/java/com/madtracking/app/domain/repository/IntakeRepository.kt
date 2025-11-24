package com.madtracking.app.domain.repository

import com.madtracking.app.domain.model.Intake
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface IntakeRepository {
    fun getIntakesForDate(profileId: Long, date: LocalDate): Flow<List<Intake>>
    fun getIntakesForMedication(medicationId: Long): Flow<List<Intake>>
    suspend fun addIntake(intake: Intake): Long
    suspend fun markIntakeTaken(intakeId: Long, takenTime: LocalDateTime)
    suspend fun markIntakeMissed(intakeId: Long)
    suspend fun deleteIntake(intakeId: Long)
    suspend fun deleteIntakesForMedication(medicationId: Long)
}

