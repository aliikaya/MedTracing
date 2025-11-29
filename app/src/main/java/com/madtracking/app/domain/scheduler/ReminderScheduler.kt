package com.madtracking.app.domain.scheduler

import com.madtracking.app.domain.model.Intake

/**
 * İlaç hatırlatıcılarını planlamak için soyut arayüz.
 * Android'e özgü implementasyon data/scheduler katmanında olacak.
 */
interface ReminderScheduler {
    
    /**
     * Belirli bir Intake için hatırlatma kurar.
     * plannedTime'a göre alarm ayarlar.
     */
    fun scheduleIntakeReminder(intake: Intake, medicationName: String)
    
    /**
     * Belirli bir Intake'in hatırlatıcısını iptal eder.
     */
    fun cancelIntakeReminder(intakeId: Long)
    
    /**
     * Bir ilaca ait tüm gelecek hatırlatıcıları iptal eder.
     * İlaç silindiğinde veya deaktif edildiğinde kullanılır.
     */
    fun cancelAllForMedication(medicationId: Long)
    
    /**
     * Hatırlatıcıların etkin olup olmadığını kontrol eder.
     */
    fun areRemindersEnabled(): Boolean
}

