package com.medtracking.app.data.repository

import com.medtracking.app.data.local.dao.ProfileDao
import com.medtracking.app.data.local.entity.ProfileEntity
import com.medtracking.app.data.remote.firebase.RemoteInvitationDataSource
import com.medtracking.app.data.remote.firebase.RemoteProfileDataSource
import com.medtracking.app.data.remote.firebase.auth.AuthDataSource
import com.medtracking.app.data.remote.firebase.model.RemoteProfileDto
import com.medtracking.app.domain.model.AcceptInvitationResult
import com.medtracking.app.domain.model.InvitationLinkResult
import com.medtracking.app.domain.model.MemberRole
import com.medtracking.app.domain.repository.ProfileSharingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileSharingRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val profileDao: ProfileDao,
    private val remoteInvitationDataSource: RemoteInvitationDataSource,
    private val remoteProfileDataSource: RemoteProfileDataSource
) : ProfileSharingRepository {

    override suspend fun createInvitation(
        profileLocalId: Long,
        grantRole: MemberRole
    ): InvitationLinkResult {
        return try {
            var profile = profileDao.getProfileByIdOnce(profileLocalId)
                ?: return InvitationLinkResult.Error("Profile not found")

            val user = authDataSource.currentUser()
                ?: return InvitationLinkResult.Error("User must be authenticated to create invitation")
            
            // Security: Don't allow granting OWNER role via invitation
            if (grantRole == MemberRole.OWNER) {
                return InvitationLinkResult.Error("Cannot grant OWNER role via invitation")
            }
            
            // If profile is not synced yet, push it to Firestore first
            var remoteId = profile.remoteId
            if (remoteId == null) {
                val remoteDto = profile.toRemoteDto(user.uid)
                val remoteResult = remoteProfileDataSource.upsertProfile(remoteDto)
                remoteId = remoteResult.id
                
                // Update local profile with remoteId
                if (remoteId != null) {
                    profile = profile.copy(
                        remoteId = remoteId,
                        isDirty = false
                    )
                    profileDao.upsert(profile)
                } else {
                    return InvitationLinkResult.Error("Failed to sync profile to cloud")
                }
            }
            
            val invitation = remoteInvitationDataSource.createInvitation(
                profileRemoteId = remoteId,
                inviterUserId = user.uid,
                grantRole = grantRole.name
            )
            
            val url = "https://medtrack.app/invite?invitationId=${invitation.id}&token=${invitation.oneTimeToken}"
            InvitationLinkResult.Success(invitationUrl = url)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                e.message?.contains("Firestore API", ignoreCase = true) == true ||
                e.message?.contains("has not been used", ignoreCase = true) == true -> {
                    "Firestore API etkin değil. Lütfen Firebase Console'da Firestore API'yi etkinleştirin."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "Ağ bağlantısı hatası. Lütfen internet bağlantınızı kontrol edin."
                }
                else -> {
                    "Davet oluşturulamadı: ${e.message ?: "Bilinmeyen hata"}"
                }
            }
            InvitationLinkResult.Error(errorMessage)
        }
    }
    
    /**
     * Converts ProfileEntity to RemoteProfileDto for Firestore sync.
     */
    private fun ProfileEntity.toRemoteDto(currentUserId: String): RemoteProfileDto {
        val owner = ownerUserId ?: currentUserId
        
        // Build members map: ensure owner is always OWNER
        val membersMap = if (membersJson != null && membersJson.isNotEmpty()) {
            membersJson.toMutableMap().apply {
                // Ensure owner has OWNER role
                if (!containsKey(owner)) {
                    put(owner, "OWNER")
                }
            }
        } else {
            // New profile: owner gets OWNER role
            mapOf(owner to "OWNER")
        }
        
        return RemoteProfileDto(
            id = remoteId,
            name = name,
            ownerUserId = owner,
            members = membersMap,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isShared = isShared,
            isDeleted = isDeleted
        )
    }

    override suspend fun acceptInvitation(invitationId: String, token: String): AcceptInvitationResult {
        return try {
            val invitation = remoteInvitationDataSource.getInvitation(invitationId)
                ?: return AcceptInvitationResult.Error.NotFound

            if (invitation.oneTimeToken != token) {
                return AcceptInvitationResult.Error.TokenInvalid
            }

            if (invitation.status != "PENDING") {
                return AcceptInvitationResult.Error.AlreadyAccepted
            }

            val now = System.currentTimeMillis()
            if (invitation.expiresAt != 0L && invitation.expiresAt < now) {
                return AcceptInvitationResult.Error.Expired
            }

            val user = authDataSource.currentUser()
                ?: return AcceptInvitationResult.Error.Unknown("Giriş yapmanız gerekiyor")
            
            remoteInvitationDataSource.markInvitationAccepted(
                id = invitationId,
                userId = user.uid,
                grantRole = invitation.grantRole
            )
            
            AcceptInvitationResult.Success
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ||
                e.message?.contains("Firestore API", ignoreCase = true) == true -> {
                    "Firestore API etkin değil. Lütfen Firebase Console'da etkinleştirin."
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    "Ağ bağlantısı hatası. İnternet bağlantınızı kontrol edin."
                }
                else -> {
                    e.message ?: "Bilinmeyen hata"
                }
            }
            AcceptInvitationResult.Error.Unknown(errorMessage)
        }
    }
}


