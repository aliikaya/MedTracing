package com.madtracking.app.domain.usecase

import com.madtracking.app.domain.scheduler.ReminderScheduler
import javax.inject.Inject

/**
 * Belirli bir Intake'in hatırlatıcısını iptal eder.
 * Intake "Alındı" olarak işaretlendiğinde kullanılır.
 */
class CancelIntakeReminderUseCase @Inject constructor(
    private val reminderScheduler: ReminderScheduler
) {
    operator fun invoke(intakeId: Long) {
        reminderScheduler.cancelIntakeReminder(intakeId)
    }
}

