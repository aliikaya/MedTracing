package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.Medication
import com.medtracking.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMedicationsForProfileUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {
    operator fun invoke(profileId: Long): Flow<List<Medication>> {
        return medicationRepository.getMedicationsForProfile(profileId)
    }
}

