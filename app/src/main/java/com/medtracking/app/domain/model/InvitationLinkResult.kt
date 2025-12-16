package com.medtracking.app.domain.model

/**
 * Result of creating an invitation link.
 */
sealed class InvitationLinkResult {
    data class Success(val invitationUrl: String) : InvitationLinkResult()
    data class Error(val reason: String) : InvitationLinkResult()
}


