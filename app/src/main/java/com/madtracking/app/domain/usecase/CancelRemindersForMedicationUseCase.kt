package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.scheduler.ReminderScheduler
import javax.inject.Inject

/**
 * Bir ilaca ait tüm hatırlatıcıları iptal eder.
 * İlaç silindiğinde veya deaktif edildiğinde kullanılır.
 */
class CancelRemindersForMedicationUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke(medicationId: Long) {
        reminderScheduler.cancelAllForMedication(medicationId)
    }
}

