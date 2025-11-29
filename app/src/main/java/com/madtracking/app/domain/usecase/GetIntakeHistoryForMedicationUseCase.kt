package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.repository.IntakeRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Belirli bir ilaç için belirtilen tarih aralığındaki tüm Intake'leri getirir.
 */
class GetIntakeHistoryForMedicationUseCase @Inject constructor(
    private val intakeRepository: IntakeRepository
) {
    operator fun invoke(
        medicationId: Long,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Flow<List<Intake>> {
        return intakeRepository.getIntakesForMedicationAndDateRange(
            medicationId = medicationId,
            fromDate = fromDate,
            toDate = toDate
        )
    }
}

