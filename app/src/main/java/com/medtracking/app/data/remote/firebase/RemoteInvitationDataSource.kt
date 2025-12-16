package com.medtracking.app.data.remote.firebase

import com.medtracking.app.data.remote.firebase.model.RemoteInvitationDto

interface RemoteInvitationDataSource {
    suspend fun createInvitation(
        profileRemoteId: String,
        inviterUserId: String,
        grantRole: String
    ): RemoteInvitationDto
    
    suspend fun getInvitation(id: String): RemoteInvitationDto?
    
    suspend fun markInvitationAccepted(
        id: String,
        userId: String,
        grantRole: String
    )
    
    suspend fun cancelInvitation(id: String)
}


