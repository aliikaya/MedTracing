package com.madtracking.app.data.repository

import com.madtracking.app.data.local.dao.MedicationDao
import com.madtracking.app.data.mapper.toDomain
import com.madtracking.app.data.mapper.toEntity
import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MedicationRepositoryImpl @Inject constructor(
    private val medicationDao: MedicationDao
) : MedicationRepository {

    override suspend fun addMedication(medication: Medication): Long {
        return medicationDao.insert(medication.toEntity())
    }

    override suspend fun updateMedication(medication: Medication) {
        medicationDao.update(medication.toEntity())
    }

    override fun getMedicationsForProfile(profileId: Long): Flow<List<Medication>> {
        return medicationDao.getMedicationsForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMedicationById(id: Long): Flow<Medication?> {
        return medicationDao.getMedicationById(id).map { it?.toDomain() }
    }

    override fun getAllActiveMedications(): Flow<List<Medication>> {
        return medicationDao.getAllActiveMedications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteMedication(medication: Medication) {
        medicationDao.delete(medication.toEntity())
    }
}

