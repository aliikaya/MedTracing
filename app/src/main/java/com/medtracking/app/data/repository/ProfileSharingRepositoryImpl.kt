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
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.start", mapOf(
                "invitationId" to invitationId,
                "token" to token.take(20)
            ))
            // #endregion
            
            // 1. Check authentication first
            val user = authDataSource.currentUser()
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.authCheck", mapOf(
                "user" to (user?.uid ?: "null")
            ))
            // #endregion
            if (user == null) {
                return AcceptInvitationResult.Error.Unknown("Giriş yapmanız gerekiyor")
            }
            
            // 2. Read and validate invitation (pre-check before transaction)
            val invitation = remoteInvitationDataSource.getInvitation(invitationId)
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.getInvitation", mapOf(
                "invitation" to (invitation?.id ?: "null"),
                "status" to (invitation?.status ?: "null")
            ))
            // #endregion
            if (invitation == null) {
                return AcceptInvitationResult.Error.NotFound
            }

            if (invitation.oneTimeToken != token) {
                // #region agent log
                logDebug("ProfileSharingRepository.acceptInvitation.tokenMismatch", mapOf())
                // #endregion
                return AcceptInvitationResult.Error.TokenInvalid
            }

            if (invitation.status != "PENDING") {
                // #region agent log
                logDebug("ProfileSharingRepository.acceptInvitation.notPending", mapOf("status" to invitation.status))
                // #endregion
                return AcceptInvitationResult.Error.AlreadyAccepted
            }

            val now = System.currentTimeMillis()
            if (invitation.expiresAt != 0L && invitation.expiresAt < now) {
                // #region agent log
                logDebug("ProfileSharingRepository.acceptInvitation.expired", mapOf(
                    "expiresAt" to invitation.expiresAt,
                    "now" to now
                ))
                // #endregion
                return AcceptInvitationResult.Error.Expired
            }
            
            // 3. Execute transaction (this will re-validate inside transaction for safety)
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.markAccepted.start", mapOf(
                "userId" to user.uid,
                "grantRole" to invitation.grantRole
            ))
            // #endregion
            remoteInvitationDataSource.markInvitationAccepted(
                id = invitationId,
                userId = user.uid,
                grantRole = invitation.grantRole
            )
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.markAccepted.success", mapOf())
            // #endregion
            
            // 4. Immediately fetch and sync the shared profile to local DB
            // This ensures the profile appears instantly without waiting for SyncManager's snapshot listener
            try {
                val profileId = invitation.profileId
                // #region agent log
                logDebug("ProfileSharingRepository.acceptInvitation.syncProfile.start", mapOf(
                    "profileId" to profileId
                ))
                // #endregion
                if (profileId.isNotBlank()) {
                    val remoteProfile = remoteProfileDataSource.getProfileById(profileId)
                    // #region agent log
                    logDebug("ProfileSharingRepository.acceptInvitation.syncProfile.fetched", mapOf(
                        "remoteProfile" to (remoteProfile?.id ?: "null"),
                        "isDeleted" to (remoteProfile?.isDeleted ?: false)
                    ))
                    // #endregion
                    if (remoteProfile != null && !remoteProfile.isDeleted) {
                        // Check if profile already exists locally
                        val existingProfile = profileDao.getProfileByRemoteId(profileId)
                        val localId = existingProfile?.id ?: 0L
                        // #region agent log
                        logDebug("ProfileSharingRepository.acceptInvitation.syncProfile.upsert", mapOf(
                            "localId" to localId,
                            "existing" to (existingProfile != null)
                        ))
                        // #endregion
                        
                        // Convert to entity and save
                        val profileEntity = remoteProfile.toProfileEntity(
                            localId = localId,
                            existing = existingProfile
                        )
                        profileDao.upsert(profileEntity)
                        // #region agent log
                        logDebug("ProfileSharingRepository.acceptInvitation.syncProfile.success", mapOf())
                        // #endregion
                    }
                }
            } catch (e: Exception) {
                // #region agent log
                logDebug("ProfileSharingRepository.acceptInvitation.syncProfile.error", mapOf(
                    "error" to (e.message ?: "unknown")
                ))
                // #endregion
                // Log but don't fail the invitation acceptance
                // SyncManager will eventually sync the profile
            }
            
            // #region agent log
            logDebug("ProfileSharingRepository.acceptInvitation.success", mapOf())
            // #endregion
            AcceptInvitationResult.Success
        } catch (e: Exception) {
            // Handle transaction-specific errors
            val errorMessage = e.message ?: ""
            when {
                errorMessage.contains("Invitation not found", ignoreCase = true) -> {
                    AcceptInvitationResult.Error.NotFound
                }
                errorMessage.contains("already accepted or canceled", ignoreCase = true) -> {
                    AcceptInvitationResult.Error.AlreadyAccepted
                }
                errorMessage.contains("expired", ignoreCase = true) -> {
                    AcceptInvitationResult.Error.Expired
                }
                errorMessage.contains("PERMISSION_DENIED", ignoreCase = true) ||
                errorMessage.contains("Firestore API", ignoreCase = true) -> {
                    AcceptInvitationResult.Error.Unknown("Firestore API etkin değil. Lütfen Firebase Console'da etkinleştirin.")
                }
                errorMessage.contains("network", ignoreCase = true) -> {
                    AcceptInvitationResult.Error.Unknown("Ağ bağlantısı hatası. İnternet bağlantınızı kontrol edin.")
                }
                else -> {
                    AcceptInvitationResult.Error.Unknown(errorMessage.ifBlank { "Bilinmeyen hata" })
                }
            }
        }
    }
    
    /**
     * Helper function to convert RemoteProfileDto to ProfileEntity for local storage.
     */
    private fun RemoteProfileDto.toProfileEntity(
        localId: Long,
        existing: ProfileEntity?
    ): ProfileEntity {
        return ProfileEntity(
            id = localId,
            name = name.ifBlank { existing?.name ?: "" },
            avatarEmoji = existing?.avatarEmoji,
            relation = existing?.relation,
            isActive = existing?.isActive ?: !isDeleted,
            createdAt = if (createdAt != 0L) createdAt else existing?.createdAt ?: System.currentTimeMillis(),
            remoteId = id,
            ownerUserId = ownerUserId,
            isShared = isShared,
            updatedAt = updatedAt,
            isDirty = false,
            isDeleted = isDeleted,
            membersJson = members
        )
    }
    
    // #region agent log
    private fun logDebug(location: String, data: Map<String, Any?>) {
        val dataStr = data.entries.joinToString(", ") { "${it.key}=${it.value}" }
        android.util.Log.d("DebugLog", "$location: $dataStr")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val logFile = File("/Users/alikaya/Projeler/MadTracking/.cursor/debug.log")
                val logLine = "{\"timestamp\":${System.currentTimeMillis()},\"location\":\"$location\",\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"message\":\"Debug log\",\"data\":{$dataStr}}\n"
                logFile.appendText(logLine)
            } catch (e: Exception) {
                android.util.Log.e("DebugLog", "Error writing log file: ${e.message}")
            }
        }
    }
    // #endregion
}


