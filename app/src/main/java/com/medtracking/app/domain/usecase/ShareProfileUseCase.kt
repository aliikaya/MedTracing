package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.InvitationLinkResult
import com.medtracking.app.domain.model.MemberRole
import com.medtracking.app.domain.repository.ProfileSharingRepository
import javax.inject.Inject

/**
 * Use case for creating a profile invitation with specified role.
 */
class ShareProfileUseCase @Inject constructor(
    private val profileSharingRepository: ProfileSharingRepository
) {
    suspend operator fun invoke(profileId: Long, grantRole: MemberRole): InvitationLinkResult {
        return profileSharingRepository.createInvitation(profileId, grantRole)
    }
}
