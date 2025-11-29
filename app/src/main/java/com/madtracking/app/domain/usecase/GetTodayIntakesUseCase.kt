package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.repository.IntakeRepository
import com.madtracking.app.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.emitAll
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Bugüne ait tüm Intake'leri getirir.
 * Eğer bir ilaç için bugüne ait Intake yoksa, schedule'a göre oluşturur.
 */
class GetTodayIntakesUseCase @Inject constructor(
    private val intakeRepository: IntakeRepository,
    private val medicationRepository: MedicationRepository
) {
    operator fun invoke(profileId: Long, date: LocalDate = LocalDate.now()): Flow<List<Intake>> = flow {
        // 1. Bu profile ait, bugünü kapsayan aktif ilaçları al
        val medications = medicationRepository.getActiveMedicationsForDate(profileId, date)

        // 2. Her ilaç için schedule'a göre Intake oluştur (yoksa)
        val newIntakes = mutableListOf<Intake>()
        
        for (medication in medications) {
            for (time in medication.schedule.timesOfDay) {
                val plannedTime = LocalDateTime.of(date, time)
                
                // Bu ilaç ve zaman için mevcut Intake var mı?
                val existingIntake = intakeRepository.getIntakeByMedicationAndTime(
                    medicationId = medication.id,
                    plannedTime = plannedTime
                )
                
                if (existingIntake == null) {
                    // Yeni Intake oluştur
                    newIntakes.add(
                        Intake(
                            medicationId = medication.id,
                            profileId = profileId,
                            plannedTime = plannedTime,
                            status = IntakeStatus.PLANNED
                        )
                    )
                }
            }
        }

        // 3. Yeni Intake'leri kaydet
        if (newIntakes.isNotEmpty()) {
            intakeRepository.addIntakes(newIntakes)
        }

        // 4. Tüm Intake'leri Flow olarak döndür
        emitAll(intakeRepository.getIntakesForDate(profileId, date))
    }
}
