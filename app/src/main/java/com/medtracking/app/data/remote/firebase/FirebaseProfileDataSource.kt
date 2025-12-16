package com.medtracking.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.medtracking.app.data.remote.firebase.model.RemoteProfileDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseProfileDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteProfileDataSource {

    private val collection get() = firestore.collection(COLLECTION_PROFILES)

    override suspend fun upsertProfile(profile: RemoteProfileDto): RemoteProfileDto {
        val docRef = if (profile.id != null) {
            collection.document(profile.id)
        } else {
            collection.document()
        }

        // Ensure owner is in members map with OWNER role
        val membersMap = if (profile.members.isEmpty() && profile.ownerUserId.isNotBlank()) {
            mapOf(profile.ownerUserId to "OWNER")
        } else {
            profile.members.toMutableMap().apply {
                // Ensure owner always has OWNER role
                if (profile.ownerUserId.isNotBlank() && !containsKey(profile.ownerUserId)) {
                    put(profile.ownerUserId, "OWNER")
                }
            }
        }

        val payload = mapOf(
            "name" to profile.name,
            "ownerUserId" to profile.ownerUserId,
            "members" to membersMap,
            "createdAt" to profile.createdAt,
            "updatedAt" to profile.updatedAt,
            "isShared" to profile.isShared,
            "isDeleted" to profile.isDeleted
        )

        docRef.set(payload, SetOptions.merge()).await()
        return profile.copy(id = docRef.id)
    }

    override suspend fun getProfilesForUser(userId: String): List<RemoteProfileDto> {
        // Query profiles where userId is in members map (as a key)
        // Firestore Map field query: where("members.userId", "!=", null)
        val snapshot = collection
            .whereNotEqualTo("members.$userId", null)
            .get()
            .await()
        return snapshot.toProfileDtos().filter { profile ->
            // Additional client-side filter to ensure userId is in members
            profile.members.containsKey(userId) || profile.ownerUserId == userId
        }
    }

    override fun observeProfilesForUser(userId: String): Flow<List<RemoteProfileDto>> = callbackFlow {
        val registration = collection
            .whereNotEqualTo("members.$userId", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val profiles = snapshot.toProfileDtos().filter { profile ->
                        // Additional client-side filter
                        profile.members.containsKey(userId) || profile.ownerUserId == userId
                    }
                    trySend(profiles)
                }
            }

        awaitClose { registration.remove() }
    }

    private fun QuerySnapshot.toProfileDtos(): List<RemoteProfileDto> {
        return documents.map { doc ->
            // Parse members map from Firestore
            val membersData = doc.get("members")
            val membersMap = when (membersData) {
                is Map<*, *> -> {
                    @Suppress("UNCHECKED_CAST")
                    (membersData as Map<String, Any?>).mapValues { (_, value) ->
                        value?.toString() ?: ""
                    }
                }
                is List<*> -> {
                    // Backward compatibility: convert old List format to Map
                    @Suppress("UNCHECKED_CAST")
                    (membersData as List<String>).associateWith { "VIEWER" }
                }
                else -> emptyMap<String, String>()
            }
            
            RemoteProfileDto(
                id = doc.id,
                name = doc.getString("name").orEmpty(),
                ownerUserId = doc.getString("ownerUserId").orEmpty(),
                members = membersMap,
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                isShared = doc.getBoolean("isShared") ?: false,
                isDeleted = doc.getBoolean("isDeleted") ?: false
            )
        }
    }

    companion object {
        private const val COLLECTION_PROFILES = "profiles"
    }
}


