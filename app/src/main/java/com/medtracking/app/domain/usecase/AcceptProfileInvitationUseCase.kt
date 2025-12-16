package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.AcceptInvitationResult
import com.medtracking.app.domain.repository.ProfileSharingRepository
import javax.inject.Inject

class AcceptProfileInvitationUseCase @Inject constructor(
    private val repository: ProfileSharingRepository
) {
    suspend operator fun invoke(invitationId: String, token: String): AcceptInvitationResult {
        return repository.acceptInvitation(invitationId, token)
    }
}


