package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Profile
import com.madtracking.app.domain.repository.ProfileRepository
import javax.inject.Inject

class UpsertProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: Profile): Long {
        return profileRepository.upsertProfile(profile)
    }
}

