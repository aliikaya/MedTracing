package com.medtracking.app.domain.repository

import com.medtracking.app.domain.model.AcceptInvitationResult
import com.medtracking.app.domain.model.InvitationLinkResult
import com.medtracking.app.domain.model.MemberRole

/**
 * Repository for profile sharing and invitation management.
 */
interface ProfileSharingRepository {
    /**
     * Creates an invitation link for a profile with specified role.
     * @param profileLocalId Local Room ID of the profile to share
     * @param grantRole Role to grant to the user who accepts this invitation
     * @return InvitationLinkResult with deep link URL or error
     */
    suspend fun createInvitation(profileLocalId: Long, grantRole: MemberRole): InvitationLinkResult
    
    /**
     * Accepts a profile invitation using one-time token.
     * @param invitationId Firestore invitation document ID
     * @param token One-time security token
     * @return AcceptInvitationResult indicating success or specific error
     */
    suspend fun acceptInvitation(invitationId: String, token: String): AcceptInvitationResult
}


