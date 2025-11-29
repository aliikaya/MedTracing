package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.repository.IntakeRepository
import com.madtracking.app.domain.scheduler.ReminderScheduler
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

class AddMedicationUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(medication: Medication): Long {
        // İlacı kaydet
        val medicationId = medicationRepository.addMedication(medication)
        
        // Hatırlatıcılar açıksa, önümüzdeki 2 gün için Intake'leri oluştur ve alarm kur
        if (reminderScheduler.areRemindersEnabled()) {
            scheduleRemindersForMedication(
                medicationId = medicationId,
                medication = medication.copy(id = medicationId)
            )
        }
        
        return medicationId
    }

    private suspend fun scheduleRemindersForMedication(medicationId: Long, medication: Medication) {
        val today = LocalDate.now()
        val endPlanDate = today.plusDays(2)

        var currentDate = today
        while (currentDate <= endPlanDate) {
            // İlaç henüz başlamamışsa bu günü atla
            if (currentDate < medication.startDate) {
                currentDate = currentDate.plusDays(1)
                continue
            }
            
            // İlaç bitmişse dur
            if (medication.endDate != null && currentDate > medication.endDate) break

            // Her saat için Intake oluştur ve reminder planla
            for (time in medication.schedule.timesOfDay) {
                val plannedTime = LocalDateTime.of(currentDate, time)
                
                // Geçmiş zamanlar için hatırlatma kurma
                if (plannedTime <= LocalDateTime.now()) continue

                // Yeni Intake oluştur
                val intake = Intake(
                    medicationId = medicationId,
                    profileId = medication.profileId,
                    plannedTime = plannedTime,
                    status = IntakeStatus.PLANNED
                )
                val intakeId = intakeRepository.addIntake(intake)

                // Hatırlatıcı kur
                reminderScheduler.scheduleIntakeReminder(
                    intake = intake.copy(id = intakeId),
                    medicationName = medication.name
                )
            }
            
            currentDate = currentDate.plusDays(1)
        }
    }
}
