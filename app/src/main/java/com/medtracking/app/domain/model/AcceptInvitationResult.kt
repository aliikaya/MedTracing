package com.medtracking.app.domain.model

/**
 * Result of accepting a profile invitation.
 */
sealed class AcceptInvitationResult {
    data object Success : AcceptInvitationResult()
    
    sealed class Error(val message: String) : AcceptInvitationResult() {
        data object TokenInvalid : Error("Invalid invitation token")
        data object Expired : Error("Invitation has expired")
        data object AlreadyAccepted : Error("Invitation already accepted")
        data object NotFound : Error("Invitation not found")
        data class Unknown(val reason: String) : Error(reason)
    }
}


