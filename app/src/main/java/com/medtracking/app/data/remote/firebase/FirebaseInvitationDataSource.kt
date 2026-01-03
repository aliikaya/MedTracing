package com.medtracking.app.data.remote.firebase

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.medtracking.app.data.remote.firebase.model.RemoteInvitationDto
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseInvitationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteInvitationDataSource {

    private val collection get() = firestore.collection(COLLECTION_INVITATIONS)
    private val profiles get() = firestore.collection(COLLECTION_PROFILES)

    override suspend fun createInvitation(
        profileRemoteId: String,
        inviterUserId: String,
        grantRole: String
    ): RemoteInvitationDto {
        val docRef = collection.document()
        val now = System.currentTimeMillis()
        val dto = RemoteInvitationDto(
            id = docRef.id,
            profileId = profileRemoteId,
            inviterUserId = inviterUserId,
            oneTimeToken = "${UUID.randomUUID()}-${System.currentTimeMillis()}", // More secure token
            grantRole = grantRole,
            status = "PENDING",
            createdAt = now,
            expiresAt = now + DEFAULT_EXPIRATION_MS
        )

        docRef.set(dto.toMap(), SetOptions.merge()).await()
        return dto
    }

    override suspend fun getInvitation(id: String): RemoteInvitationDto? {
        val snapshot = collection.document(id).get().await()
        if (!snapshot.exists()) return null

        return RemoteInvitationDto(
            id = snapshot.id,
            profileId = snapshot.getString("profileId").orEmpty(),
            inviterUserId = snapshot.getString("inviterUserId").orEmpty(),
            oneTimeToken = snapshot.getString("oneTimeToken").orEmpty(),
            grantRole = snapshot.getString("grantRole") ?: "VIEWER",
            status = snapshot.getString("status").orEmpty(),
            createdAt = snapshot.getLong("createdAt") ?: 0L,
            expiresAt = snapshot.getLong("expiresAt") ?: 0L
        )
    }

    override suspend fun markInvitationAccepted(id: String, userId: String, grantRole: String) {
        firestore.runTransaction { transaction ->
            // 1. Read invitation document
            val invitationRef = collection.document(id)
            val invitationSnapshot = transaction.get(invitationRef)
            
            if (!invitationSnapshot.exists()) {
                throw IllegalStateException("Invitation not found")
            }
            
            // 2. Validate invitation
            val status = invitationSnapshot.getString("status") ?: ""
            if (status != "PENDING") {
                throw IllegalStateException("Invitation already accepted or canceled")
            }
            
            val expiresAt = invitationSnapshot.getLong("expiresAt") ?: 0L
            val now = System.currentTimeMillis()
            if (expiresAt > 0 && expiresAt < now) {
                throw IllegalStateException("Invitation expired")
            }
            
            val profileId = invitationSnapshot.getString("profileId")
            if (profileId.isNullOrBlank()) {
                throw IllegalStateException("Invalid invitation: missing profileId")
            }
            
            // 3. Update invitation document
            transaction.update(
                invitationRef,
                mapOf(
                    "status" to "ACCEPTED",
                    "acceptedByUid" to userId,
                    "acceptedAt" to now
                )
            )
            
            // 4. Update profile document - add user to members and update timestamp
            val profileRef = profiles.document(profileId)
            transaction.update(
                profileRef,
                mapOf(
                    "members.$userId" to grantRole,
                    "updatedAt" to now
                )
            )
            
            null // Transaction returns null on success
        }.await()
    }

    override suspend fun cancelInvitation(id: String) {
        collection.document(id).update("status", "CANCELED").await()
    }

    private fun RemoteInvitationDto.toMap(): Map<String, Any?> = mapOf(
        "profileId" to profileId,
        "inviterUserId" to inviterUserId,
        "oneTimeToken" to oneTimeToken,
        "grantRole" to grantRole,
        "status" to status,
        "createdAt" to createdAt,
        "expiresAt" to expiresAt
    )

    companion object {
        private const val COLLECTION_INVITATIONS = "invitations"
        private const val COLLECTION_PROFILES = "profiles"
        private const val DEFAULT_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L
    }
}


