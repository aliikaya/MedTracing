package com.medtracking.app.data.remote.firebase

import com.medtracking.app.data.remote.firebase.model.RemoteMedicationDto
import kotlinx.coroutines.flow.Flow

interface RemoteMedicationDataSource {
    suspend fun upsertMedication(profileRemoteId: String, medication: RemoteMedicationDto): RemoteMedicationDto
    suspend fun getMedications(profileRemoteId: String): List<RemoteMedicationDto>
    fun observeMedications(profileRemoteId: String): Flow<List<RemoteMedicationDto>>
}


