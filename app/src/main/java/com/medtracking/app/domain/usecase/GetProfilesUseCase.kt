package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.Profile
import com.medtracking.app.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfilesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<List<Profile>> {
        return profileRepository.getProfiles()
    }
}

