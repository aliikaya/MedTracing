package com.medtracking.app.data.remote.firebase

import com.medtracking.app.data.remote.firebase.model.RemoteIntakeDto
import kotlinx.coroutines.flow.Flow

interface RemoteIntakeDataSource {
    suspend fun upsertIntake(profileRemoteId: String, intake: RemoteIntakeDto): RemoteIntakeDto
    suspend fun getIntakes(profileRemoteId: String): List<RemoteIntakeDto>
    fun observeIntakes(profileRemoteId: String): Flow<List<RemoteIntakeDto>>
}


