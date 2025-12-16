package com.medtracking.app.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.medtracking.app.data.remote.firebase.model.RemoteMedicationDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMedicationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : RemoteMedicationDataSource {

    private fun collection(profileRemoteId: String) =
        firestore.collection(COLLECTION_PROFILES)
            .document(profileRemoteId)
            .collection(COLLECTION_MEDICATIONS)

    override suspend fun upsertMedication(
        profileRemoteId: String,
        medication: RemoteMedicationDto
    ): RemoteMedicationDto {
        val docRef = if (medication.id != null) {
            collection(profileRemoteId).document(medication.id)
        } else {
            collection(profileRemoteId).document()
        }

        val payload = mapOf(
            "profileId" to profileRemoteId,
            "name" to medication.name,
            "form" to medication.form,
            "dosageAmount" to medication.dosageAmount,
            "dosageUnit" to medication.dosageUnit,
            "scheduleTimes" to medication.scheduleTimes,
            "startDate" to medication.startDate,
            "endDate" to medication.endDate,
            "durationInDays" to medication.durationInDays,
            "importance" to medication.importance,
            "mealRelation" to medication.mealRelation,
            "isActive" to medication.isActive,
            "notes" to medication.notes,
            "createdAt" to medication.createdAt,
            "updatedAt" to medication.updatedAt,
            "createdByUserId" to medication.createdByUserId,
            "isDeleted" to medication.isDeleted
        )

        docRef.set(payload, SetOptions.merge()).await()
        return medication.copy(id = docRef.id)
    }

    override suspend fun getMedications(profileRemoteId: String): List<RemoteMedicationDto> {
        val snapshot = collection(profileRemoteId).get().await()
        return snapshot.toMedicationDtos(profileRemoteId)
    }

    override fun observeMedications(profileRemoteId: String): Flow<List<RemoteMedicationDto>> = callbackFlow {
        val registration = collection(profileRemoteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.toMedicationDtos(profileRemoteId))
                }
            }

        awaitClose { registration.remove() }
    }

    private fun QuerySnapshot.toMedicationDtos(profileRemoteId: String): List<RemoteMedicationDto> {
        return documents.map { doc ->
            RemoteMedicationDto(
                id = doc.id,
                profileId = profileRemoteId,
                name = doc.getString("name").orEmpty(),
                form = doc.getString("form").orEmpty(),
                dosageAmount = doc.getDouble("dosageAmount") ?: 0.0,
                dosageUnit = doc.getString("dosageUnit").orEmpty(),
                scheduleTimes = doc.get("scheduleTimes") as? List<String> ?: emptyList(),
                startDate = doc.getString("startDate").orEmpty(),
                endDate = doc.getString("endDate"),
                durationInDays = (doc.getLong("durationInDays") ?: 0L).let { if (it == 0L) null else it.toInt() },
                importance = doc.getString("importance").orEmpty(),
                mealRelation = doc.getString("mealRelation").orEmpty(),
                isActive = doc.getBoolean("isActive") ?: true,
                notes = doc.getString("notes"),
                createdAt = doc.getLong("createdAt") ?: 0L,
                updatedAt = doc.getLong("updatedAt") ?: 0L,
                createdByUserId = doc.getString("createdByUserId").orEmpty(),
                isDeleted = doc.getBoolean("isDeleted") ?: false
            )
        }
    }

    companion object {
        private const val COLLECTION_PROFILES = "profiles"
        private const val COLLECTION_MEDICATIONS = "medications"
    }
}


