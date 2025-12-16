package com.medtracking.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.medtracking.app.data.remote.firebase.model.RemoteIntakeDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseIntakeDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteIntakeDataSource {

    private fun collection(profileRemoteId: String) =
        firestore.collection(COLLECTION_PROFILES)
            .document(profileRemoteId)
            .collection(COLLECTION_INTAKES)

    override suspend fun upsertIntake(
        profileRemoteId: String,
        intake: RemoteIntakeDto
    ): RemoteIntakeDto {
        val docRef = if (intake.id != null) {
            collection(profileRemoteId).document(intake.id)
        } else {
            collection(profileRemoteId).document()
        }

        val payload = mapOf(
            "profileId" to profileRemoteId,
            "medicationId" to intake.medicationId,
            "plannedTime" to intake.plannedTime,
            "takenTime" to intake.takenTime,
            "status" to intake.status,
            "createdAt" to intake.createdAt,
            "updatedAt" to intake.updatedAt,
            "createdByUserId" to intake.createdByUserId,
            "isDeleted" to intake.isDeleted
        )

        docRef.set(payload, SetOptions.merge()).await()
        return intake.copy(id = docRef.id)
    }

    override suspend fun getIntakes(profileRemoteId: String): List<RemoteIntakeDto> {
        val snapshot = collection(profileRemoteId).get().await()
        return snapshot.toIntakeDtos(profileRemoteId)
    }

    override fun observeIntakes(profileRemoteId: String): Flow<List<RemoteIntakeDto>> = callbackFlow {
        val registration = collection(profileRemoteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toIntakeDtos(profileRemoteId))
                }
            }

        awaitClose { registration.remove() }
    }

    private fun QuerySnapshot.toIntakeDtos(profileRemoteId: String): List<RemoteIntakeDto> {
        return documents.map { doc ->
            RemoteIntakeDto(
                id = doc.id,
                profileId = profileRemoteId,
                medicationId = doc.getString("medicationId").orEmpty(),
                plannedTime = doc.getString("plannedTime").orEmpty(),
                takenTime = doc.getString("takenTime"),
                status = doc.getString("status").orEmpty(),
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                createdByUserId = doc.getString("createdByUserId").orEmpty(),
                isDeleted = doc.getBoolean("isDeleted") ?: false
            )
        }
    }

    companion object {
        private const val COLLECTION_PROFILES = "profiles"
        private const val COLLECTION_INTAKES = "intakes"
    }
}


