package com.medtracking.app.domain.repository

import com.medtracking.app.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun upsertProfile(profile: Profile): Long
    fun getProfiles(): Flow<List<Profile>>
    fun getProfileById(id: Long): Flow<Profile?>
    suspend fun deleteProfile(profile: Profile)
}

