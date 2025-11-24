package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository
) {
    suspend operator fun invoke(medication: Medication): Long {
        return medicationRepository.addMedication(medication)
    }
}

