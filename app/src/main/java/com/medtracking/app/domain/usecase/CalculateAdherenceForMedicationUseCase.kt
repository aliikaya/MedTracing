package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.AdherenceResult
import com.medtracking.app.domain.model.IntakeStatus
import com.medtracking.app.domain.model.Medication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

/**
 * Belirli bir ilaç için belirtilen tarih aralığındaki uyum oranını hesaplar.
 */
class CalculateAdherenceForMedicationUseCase @Inject constructor(
    private val getIntakeHistoryForMedicationUseCase: GetIntakeHistoryForMedicationUseCase
) {
    operator fun invoke(
        medication: Medication,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Flow<AdherenceResult> {
        return getIntakeHistoryForMedicationUseCase(
            medicationId = medication.id,
            fromDate = fromDate,
            toDate = toDate
        ).map { intakes ->
            // 1. Planlanan intake sayısını hesapla
            val plannedCount = calculatePlannedCount(medication, fromDate, toDate)
            
            // 2. Alınan intake sayısını hesapla
            val takenCount = intakes.count { it.status == IntakeStatus.TAKEN }
            
            // 3. Kaçırılan intake sayısını hesapla (minimum 0)
            val missedCount = maxOf(plannedCount - takenCount, 0)
            
            // 4. Uyum oranını hesapla
            val adherenceRatio = if (plannedCount > 0) {
                takenCount.toDouble() / plannedCount.toDouble()
            } else {
                null
            }
            
            AdherenceResult(
                planned = plannedCount,
                taken = takenCount,
                missed = missedCount,
                adherenceRatio = adherenceRatio
            )
        }
    }
    
    /**
     * Belirtilen tarih aralığında planlanan intake sayısını hesaplar.
     * Her gün için: eğer gün, ilacın startDate–endDate aralığındaysa,
     * plannedCount += medication.schedule.timesOfDay.size
     */
    private fun calculatePlannedCount(
        medication: Medication,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Int {
        var plannedCount = 0
        var currentDate = fromDate
        
        while (!currentDate.isAfter(toDate)) {
            if (medication.isActiveOnDate(currentDate)) {
                plannedCount += medication.schedule.timesOfDay.size
            }
            currentDate = currentDate.plusDays(1)
        }
        
        return plannedCount
    }
}

