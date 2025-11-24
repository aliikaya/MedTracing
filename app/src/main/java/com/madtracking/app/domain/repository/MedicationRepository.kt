package com.madtracking.app.domain.repository

import com.madtracking.app.domain.model.Medication
import kotlinx.coroutines.flow.Flow

interface MedicationRepository {
    suspend fun addMedication(medication: Medication): Long
    suspend fun updateMedication(medication: Medication)
    fun getMedicationsForProfile(profileId: Long): Flow<List<Medication>>
    fun getMedicationById(id: Long): Flow<Medication?>
    fun getAllActiveMedications(): Flow<List<Medication>>
    suspend fun deleteMedication(medication: Medication)
}

