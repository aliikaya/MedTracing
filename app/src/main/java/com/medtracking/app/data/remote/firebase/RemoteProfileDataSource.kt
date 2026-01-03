package com.medtracking.app.data.remote.firebase

import com.medtracking.app.data.remote.firebase.model.RemoteProfileDto
import kotlinx.coroutines.flow.Flow

interface RemoteProfileDataSource {
    suspend fun upsertProfile(profile: RemoteProfileDto): RemoteProfileDto
    suspend fun getProfilesForUser(userId: String): List<RemoteProfileDto>
    suspend fun getProfileById(profileId: String): RemoteProfileDto?
    fun observeProfilesForUser(userId: String): Flow<List<RemoteProfileDto>>
}


