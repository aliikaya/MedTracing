package com.madtracking.app.domain.repository

import com.madtracking.app.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MedicationRepository {
    suspend fun addMedication(medication: Medication): Long
    suspend fun updateMedication(medication: Medication)
    fun getMedicationsForProfile(profileId: Long): Flow<List<Medication>>
    suspend fun getMedicationsForProfileOnce(profileId: Long): List<Medication>
    suspend fun getActiveMedicationsForDate(profileId: Long, date: LocalDate): List<Medication>
    fun getMedicationById(id: Long): Flow<Medication?>
    suspend fun getMedicationByIdOnce(id: Long): Medication?
    fun getAllActiveMedications(): Flow<List<Medication>>
    suspend fun deleteMedication(medication: Medication)
}

