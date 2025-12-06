package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.Profile
import com.medtracking.app.domain.repository.ProfileRepository
import javax.inject.Inject

class UpsertProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): Long {
        return profileRepository.upsertProfile(profile)
    }
}

