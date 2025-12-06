package com.medtracking.app.domain.usecase

import com.medtracking.app.domain.model.Medication
import com.medtracking.app.domain.repository.MedicationRepository
import com.medtracking.app.domain.repository.IntakeRepository
import com.medtracking.app.domain.scheduler.ReminderScheduler
import com.medtracking.app.domain.model.Intake
import com.medtracking.app.domain.model.IntakeStatus
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
        
        // Hatırlatıcılar açıksa, Intake'leri oluştur ve alarm kur
        // Eğer tedavi süresi belirtilmişse ve 30 günden kısaysa, tüm süre için planla
        // Aksi halde sadece yakın günler için planla (2 gün)
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
        
        // İlacın bitiş tarihini hesapla (durationInDays varsa onu kullan)
        val calculatedEndDate = medication.endDateOrNull()
        
        // Planlama bitiş tarihini belirle:
        // - Eğer calculatedEndDate varsa ve yakınsa (max 30 gün), ona kadar planla
        // - Yoksa veya çok uzaksa, sadece yakın günler için planla (2 gün)
        val endPlanDate = if (calculatedEndDate != null && calculatedEndDate <= today.plusDays(30)) {
            calculatedEndDate
        } else {
            today.plusDays(2) // Varsayılan: yakın günler
        }

        var currentDate = today
        while (currentDate <= endPlanDate) {
            // İlaç henüz başlamamışsa bu günü atla
            if (currentDate < medication.startDate) {
                currentDate = currentDate.plusDays(1)
                continue
            }
            
            // İlaç bitmişse dur (durationInDays veya endDate kontrolü)
            if (calculatedEndDate != null && currentDate > calculatedEndDate) break

            // Domain helper ile aktif mi kontrol et
            if (!medication.isActiveOnDate(currentDate)) {
                currentDate = currentDate.plusDays(1)
                continue
            }

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
                    medicationName = medication.name,
                    mealRelation = medication.mealRelation
                )
            }
            
            currentDate = currentDate.plusDays(1)
        }
    }
}
