package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.repository.IntakeRepository
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.scheduler.ReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Bir ilaç için önümüzdeki 2 gün boyunca Intake'ler oluşturur
 * ve her biri için hatırlatıcı planlar.
 * Duration kontrolü yaparak süresi dolmuş ilaçlar için reminder kurmaz.
 */
class ScheduleIntakeRemindersUseCase @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(medicationId: Long) {
        // Hatırlatıcılar kapalıysa çık
        if (!reminderScheduler.areRemindersEnabled()) return

        // İlacı al
        val medication = medicationRepository.getMedicationByIdOnce(medicationId) ?: return
        
        // İlaç aktif değilse çık
        if (!medication.isActive) return

        val today = LocalDate.now()
        val endPlanDate = today.plusDays(2) // Bugün + yarın + öbür gün

        // İlacın bitiş tarihini hesapla (durationInDays varsa onu kullan)
        val calculatedEndDate = medication.endDateOrNull()
        
        // Tarih aralığını kontrol et
        if (calculatedEndDate != null && calculatedEndDate < today) return

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

                // Mevcut Intake var mı kontrol et
                var intake = intakeRepository.getIntakeByMedicationAndTime(medicationId, plannedTime)
                
                if (intake == null) {
                    // Yeni Intake oluştur
                    intake = Intake(
                        medicationId = medicationId,
                        profileId = medication.profileId,
                        plannedTime = plannedTime,
                        status = IntakeStatus.PLANNED
                    )
                    val newId = intakeRepository.addIntake(intake)
                    intake = intake.copy(id = newId)
                }

                // Sadece PLANNED durumundaki Intake'ler için hatırlatma kur
                if (intake.status == IntakeStatus.PLANNED) {
                    reminderScheduler.scheduleIntakeReminder(
                        intake = intake, 
                        medicationName = medication.name,
                        mealRelation = medication.mealRelation
                    )
                }
            }
            
            currentDate = currentDate.plusDays(1)
        }
    }
}
