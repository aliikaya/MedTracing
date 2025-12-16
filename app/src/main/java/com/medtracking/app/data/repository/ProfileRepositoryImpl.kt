package com.medtracking.app.data.repository

import com.medtracking.app.data.local.dao.ProfileDao
import com.medtracking.app.data.mapper.toDomain
import com.medtracking.app.data.mapper.toEntity
import com.medtracking.app.data.remote.firebase.auth.AuthDataSource
import com.medtracking.app.domain.model.Profile
import com.medtracking.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao,
    private val authDataSource: AuthDataSource
) : ProfileRepository {

    override suspend fun upsertProfile(profile: Profile): Long {
        val now = System.currentTimeMillis()
        val currentUser = authDataSource.currentUser()
        
        // Set ownerUserId if not already set (new profile)
        val profileWithOwner = if (profile.ownerUserId.isNullOrBlank() && currentUser != null) {
            profile.copy(
                ownerUserId = currentUser.uid,
                // Initialize members map with owner as OWNER
                members = mapOf(currentUser.uid to com.medtracking.app.domain.model.MemberRole.OWNER),
                updatedAt = now,
                isDirty = true,
                isDeleted = false
            )
        } else {
            profile.copy(
                updatedAt = now,
                isDirty = true,
                isDeleted = false
            )
        }
        
        return profileDao.upsert(profileWithOwner.toEntity())
    }

    override fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            val currentUserId = authDataSource.currentUser()?.uid
            entities.map { it.toDomain(currentUserId) }
        }
    }

    override fun getProfileById(id: Long): Flow<Profile?> {
        return profileDao.getProfileById(id).map { entity ->
            val currentUserId = authDataSource.currentUser()?.uid
            entity?.toDomain(currentUserId)
        }
    }

    override suspend fun deleteProfile(profile: Profile) {
        val now = System.currentTimeMillis()
        profileDao.update(
            profile.copy(
                isDeleted = true,
                isDirty = true,
                updatedAt = now
            ).toEntity()
        )
    }
}

