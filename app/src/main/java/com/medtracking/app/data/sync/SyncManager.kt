package com.medtracking.app.data.sync

import com.medtracking.app.data.local.dao.IntakeDao
import com.medtracking.app.data.local.dao.MedicationDao
import com.medtracking.app.data.local.dao.ProfileDao
import com.medtracking.app.data.local.entity.IntakeEntity
import com.medtracking.app.data.local.entity.MedicationEntity
import com.medtracking.app.data.local.entity.ProfileEntity
import com.medtracking.app.data.remote.firebase.RemoteIntakeDataSource
import com.medtracking.app.data.remote.firebase.RemoteMedicationDataSource
import com.medtracking.app.data.remote.firebase.RemoteProfileDataSource
import com.medtracking.app.data.remote.firebase.model.RemoteIntakeDto
import com.medtracking.app.data.remote.firebase.model.RemoteMedicationDto
import com.medtracking.app.data.remote.firebase.model.RemoteProfileDto
import com.medtracking.app.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncManager coordinates bidirectional sync between local Room DB and Firebase Firestore.
 * 
 * CRITICAL: Only syncs when user is authenticated.
 * - If user == null, no sync operations are performed.
 * - On logout, all active sync jobs are cancelled.
 * - On login, sync starts automatically.
 */
@Singleton
class SyncManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val remoteProfileDataSource: RemoteProfileDataSource,
    private val remoteMedicationDataSource: RemoteMedicationDataSource,
    private val remoteIntakeDataSource: RemoteIntakeDataSource,
    private val profileDao: ProfileDao,
    private val medicationDao: MedicationDao,
    private val intakeDao: IntakeDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    private var syncJob: Job? = null
    private val observedProfileRemoteIds = mutableSetOf<String>()

    /**
     * Start observing auth state and sync accordingly.
     * Should be called once from Application.onCreate.
     */
    fun start() {
        scope.launch {
            authRepository.observeAuthState().collectLatest { authUser ->
                if (authUser != null) {
                    // User logged in - start sync
                    startSync(authUser.uid)
                } else {
                    // User logged out - stop sync
                    stopSync()
                }
            }
        }
    }

    private fun startSync(userId: String) {
        // Cancel any existing sync job
        syncJob?.cancel()
        observedProfileRemoteIds.clear()

        syncJob = scope.launch {
            // Clean up any existing duplicates in local database
            cleanupDuplicateProfiles()
            
            // Launch observe job
            launch { observeRemote(userId) }
            
            // Launch periodic push job
            launch {
                while (true) {
                    pushDirty(userId)
                    delay(PUSH_INTERVAL_MS)
                }
            }
        }
    }
    
    private suspend fun cleanupDuplicateProfiles() {
        try {
            // Get all profiles grouped by ownerUserId + name
            val profileList = profileDao.getAllProfiles().first()
            
            profileList.groupBy { "${it.ownerUserId}_${it.name}" }.forEach { (_, group) ->
                if (group.size > 1) {
                    // Keep the one with remoteId if exists, otherwise keep the oldest
                    val toKeep = group.firstOrNull { it.remoteId != null } 
                        ?: group.minByOrNull { it.createdAt }
                    
                    // Delete duplicates
                    group.filterNot { it.id == toKeep?.id }.forEach { duplicate ->
                        profileDao.deleteById(duplicate.id)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore cleanup errors silently
        }
    }

    private fun stopSync() {
        syncJob?.cancel()
        syncJob = null
        observedProfileRemoteIds.clear()
    }

    private suspend fun observeRemote(userId: String) {
        remoteProfileDataSource.observeProfilesForUser(userId).collectLatest { remoteProfiles ->
            // Deduplicate remote profiles by ID to prevent duplicate creation
            val uniqueRemoteProfiles = remoteProfiles.distinctBy { it.id }
            
            uniqueRemoteProfiles.forEach { remoteProfile ->
                // First try to find by remoteId
                var localProfile = remoteProfile.id?.let { profileDao.getProfileByRemoteId(it) }
                
                // If not found by remoteId, try to find by ownerUserId + name
                // This handles the case where profile was created locally but not yet synced
                if (localProfile == null && remoteProfile.ownerUserId.isNotBlank() && remoteProfile.name.isNotBlank()) {
                    localProfile = profileDao.getProfileByOwnerAndName(remoteProfile.ownerUserId, remoteProfile.name)
                    
                    // If we found a match, clean up any duplicates in local DB
                    if (localProfile != null) {
                        profileDao.deleteDuplicateProfiles(remoteProfile.ownerUserId, remoteProfile.name)
                    }
                }
                
                if (remoteProfile.isDeleted) {
                    if (localProfile != null) {
                        profileDao.upsert(
                            localProfile.copy(
                                isDeleted = true,
                                isDirty = false,
                                updatedAt = remoteProfile.updatedAt
                            )
                        )
                    }
                    return@forEach
                }

                val shouldApplyRemote = localProfile == null || remoteProfile.updatedAt > (localProfile.updatedAt)
                val localProfileId = localProfile?.id ?: 0L

                if (shouldApplyRemote) {
                    val entity = remoteProfile.toEntity(
                        localId = localProfileId,
                        existing = localProfile
                    )
                    val insertedId = profileDao.upsert(entity)
                    
                    // If this was a new insert (localProfileId was 0), use the returned ID
                    if (localProfileId == 0L && localProfile == null) {
                        // Clean up any duplicates that might have been created
                        profileDao.deleteDuplicateProfiles(remoteProfile.ownerUserId, remoteProfile.name)
                    }
                } else if (localProfile != null && localProfile.updatedAt > remoteProfile.updatedAt && localProfile.isDirty) {
                    // Local is newer, push back to remote
                    pushProfile(localProfile, userId)
                }

                val profileIdForChildren = remoteProfile.id ?: localProfile?.remoteId
                if (profileIdForChildren != null && observedProfileRemoteIds.add(profileIdForChildren)) {
                    observeMedications(profileIdForChildren, userId)
                    observeIntakes(profileIdForChildren, userId)
                }
            }
        }
    }

    private fun observeMedications(profileRemoteId: String, currentUserId: String) {
        scope.launch {
            val profile = profileDao.getProfileByRemoteId(profileRemoteId) ?: return@launch
            remoteMedicationDataSource.observeMedications(profileRemoteId).collectLatest { remotes ->
                remotes.forEach { remoteMedication ->
                    val localMedication = remoteMedication.id?.let { medicationDao.getMedicationByRemoteId(it) }
                    val shouldApplyRemote = localMedication == null || remoteMedication.updatedAt > (localMedication.updatedAt)
                    val localId = localMedication?.id ?: 0L

                    if (shouldApplyRemote) {
                        val entity = remoteMedication.toEntity(
                            localId = localId,
                            profileLocalId = profile.id,
                            existing = localMedication
                        )
                        medicationDao.insert(entity)
                    } else if (localMedication != null && localMedication.updatedAt > remoteMedication.updatedAt && localMedication.isDirty) {
                        val profileRemote = profile.remoteId
                        if (profileRemote != null) {
                            pushMedication(localMedication, profileRemote, currentUserId)
                        }
                    }
                }
            }
        }
    }

    private fun observeIntakes(profileRemoteId: String, currentUserId: String) {
        scope.launch {
            val profile = profileDao.getProfileByRemoteId(profileRemoteId) ?: return@launch
            remoteIntakeDataSource.observeIntakes(profileRemoteId).collectLatest { remotes ->
                remotes.forEach { remoteIntake ->
                    val localIntake = remoteIntake.id?.let { intakeDao.getIntakeByRemoteId(it) }
                    val shouldApplyRemote = localIntake == null || remoteIntake.updatedAt > (localIntake.updatedAt)
                    val localId = localIntake?.id ?: 0L
                    val medicationLocalId = medicationDao.getMedicationByRemoteId(remoteIntake.medicationId)?.id
                    if (medicationLocalId == null) return@forEach

                    if (shouldApplyRemote) {
                        val entity = remoteIntake.toEntity(
                            localId = localId,
                            profileLocalId = profile.id,
                            medicationLocalId = medicationLocalId,
                            existing = localIntake
                        )
                        intakeDao.insert(entity)
                    } else if (localIntake != null && localIntake.updatedAt > remoteIntake.updatedAt && localIntake.isDirty) {
                        val profileRemote = profile.remoteId
                        if (profileRemote != null) {
                            pushIntake(localIntake, profileRemote, currentUserId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun pushDirty(userId: String) {
        val dirtyProfiles = profileDao.getDirtyProfiles()
        dirtyProfiles.forEach { pushProfile(it, userId) }

        val dirtyMedications = medicationDao.getDirtyMedications()
        dirtyMedications.forEach { medication ->
            val profile = profileDao.getProfileByIdOnce(medication.profileId)
            val profileRemoteId = profile?.remoteId ?: return@forEach
            pushMedication(medication, profileRemoteId, userId)
        }

        val dirtyIntakes = intakeDao.getDirtyIntakes()
        dirtyIntakes.forEach { intake ->
            val profile = profileDao.getProfileByIdOnce(intake.profileId)
            val profileRemoteId = profile?.remoteId ?: return@forEach
            pushIntake(intake, profileRemoteId, userId)
        }
    }

    private suspend fun pushProfile(profile: ProfileEntity, currentUserId: String) {
        val remote = profile.toRemoteDto(currentUserId)
        val remoteResult = remoteProfileDataSource.upsertProfile(remote)
        profileDao.upsert(
            profile.copy(
                remoteId = remoteResult.id ?: profile.remoteId,
                isDirty = false
            )
        )
    }

    private suspend fun pushMedication(
        medication: MedicationEntity,
        profileRemoteId: String,
        currentUserId: String
    ) {
        val remote = medication.toRemoteDto(profileRemoteId, currentUserId)
        val remoteResult = remoteMedicationDataSource.upsertMedication(profileRemoteId, remote)
        medicationDao.insert(
            medication.copy(
                remoteId = remoteResult.id ?: medication.remoteId,
                profileRemoteId = profileRemoteId,
                isDirty = false
            )
        )
    }

    private suspend fun pushIntake(
        intake: IntakeEntity,
        profileRemoteId: String,
        currentUserId: String
    ) {
        val medicationRemoteId = medicationDao.getMedicationByIdOnce(intake.medicationId)?.remoteId ?: return
        val remote = intake.toRemoteDto(profileRemoteId, medicationRemoteId, currentUserId)
        val remoteResult = remoteIntakeDataSource.upsertIntake(profileRemoteId, remote)
        intakeDao.insert(
            intake.copy(
                remoteId = remoteResult.id ?: intake.remoteId,
                isDirty = false
            )
        )
    }

    private fun RemoteProfileDto.toEntity(
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
            membersJson = members // Map<String, String> directly from Firestore
        )
    }

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

    private fun RemoteMedicationDto.toEntity(
        localId: Long,
        profileLocalId: Long,
        existing: MedicationEntity? = null
    ): MedicationEntity {
        val dosageString = "${dosageAmount}|${dosageUnit}"
        val scheduleString = scheduleTimes.joinToString(",")
        return MedicationEntity(
            id = localId,
            profileId = profileLocalId,
            name = name,
            form = form,
            dosage = dosageString,
            scheduleTimes = scheduleString,
            startDate = startDate,
            endDate = endDate,
            durationInDays = durationInDays,
            isActive = if (isDeleted) false else isActive,
            importance = importance,
            mealRelation = mealRelation,
            notes = notes ?: existing?.notes,
            stockCount = existing?.stockCount,
            createdAt = createdAt,
            remoteId = id,
            profileRemoteId = profileId,
            updatedAt = updatedAt,
            isDirty = false,
            isDeleted = isDeleted
        )
    }

    private fun MedicationEntity.toRemoteDto(
        profileRemoteId: String,
        currentUserId: String
    ): RemoteMedicationDto {
        val dosageParts = dosage.split("|")
        val amount = dosageParts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val unit = dosageParts.getOrNull(1).orEmpty()
        val scheduleList = scheduleTimes.split(",").filter { it.isNotBlank() }
        return RemoteMedicationDto(
            id = remoteId,
            profileId = profileRemoteId,
            name = name,
            form = form,
            dosageAmount = amount,
            dosageUnit = unit,
            scheduleTimes = scheduleList,
            startDate = startDate,
            endDate = endDate,
            durationInDays = durationInDays,
            importance = importance,
            mealRelation = mealRelation,
            isActive = isActive,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdByUserId = currentUserId,
            isDeleted = isDeleted
        )
    }

    private fun RemoteIntakeDto.toEntity(
        localId: Long,
        profileLocalId: Long,
        medicationLocalId: Long,
        existing: IntakeEntity? = null
    ): IntakeEntity {
        return IntakeEntity(
            id = localId,
            medicationId = medicationLocalId,
            profileId = profileLocalId,
            plannedTime = plannedTime,
            takenTime = takenTime,
            status = status,
            notes = existing?.notes,
            createdAt = createdAt,
            remoteId = id,
            updatedAt = updatedAt,
            isDirty = false,
            isDeleted = isDeleted
        )
    }

    private fun IntakeEntity.toRemoteDto(
        profileRemoteId: String,
        medicationRemoteId: String,
        currentUserId: String
    ): RemoteIntakeDto {
        return RemoteIntakeDto(
            id = remoteId,
            profileId = profileRemoteId,
            medicationId = medicationRemoteId,
            plannedTime = plannedTime,
            takenTime = takenTime,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            createdByUserId = currentUserId,
            isDeleted = isDeleted
        )
    }

    companion object {
        private const val PUSH_INTERVAL_MS = 30_000L
    }
}


