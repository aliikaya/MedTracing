package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMedicationsForProfileUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {
    operator fun invoke(profileId: Long): Flow<List<Medication>> {
        return medicationRepository.getMedicationsForProfile(profileId)
    }
}

