package com.medtracking.app.domain.model

/**
 * Result of accepting a profile invitation.
 */
sealed class AcceptInvitationResult {
    data object Success : AcceptInvitationResult()
    
    sealed class Error(val message: String) : AcceptInvitationResult() {
        data object TokenInvalid : Error("Geçersiz davet kodu")
        data object Expired : Error("Davet süresi dolmuş")
        data object AlreadyAccepted : Error("Bu davet zaten kabul edilmiş")
        data object NotFound : Error("Davet bulunamadı")
        data class Unknown(val reason: String) : Error("Davet kabul edilemedi: $reason")
    }
}


