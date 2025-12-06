package com.medtracking.app.data.repository

import com.medtracking.app.data.local.dao.ProfileDao
import com.medtracking.app.data.mapper.toDomain
import com.medtracking.app.data.mapper.toEntity
import com.medtracking.app.domain.model.Profile
import com.medtracking.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {

    override suspend fun upsertProfile(profile: Profile): Long {
        return profileDao.upsert(profile.toEntity())
    }

    override fun getProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getProfileById(id: Long): Flow<Profile?> {
        return profileDao.getProfileById(id).map { it?.toDomain() }
    }

    override suspend fun deleteProfile(profile: Profile) {
        profileDao.delete(profile.toEntity())
    }
}

