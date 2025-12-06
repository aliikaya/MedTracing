package com.medtracking.app.domain.scheduler

import com.medtracking.app.domain.model.Intake
import com.medtracking.app.domain.model.MealRelation

/**
 * İlaç hatırlatıcılarını planlamak için soyut arayüz.
 * Android'e özgü implementasyon data/scheduler katmanında olacak.
 */
interface ReminderScheduler {
    
    /**
     * Belirli bir Intake için hatırlatma kurar.
     * plannedTime'a göre alarm ayarlar.
     */
    fun scheduleIntakeReminder(
        intake: Intake, 
        medicationName: String,
        mealRelation: MealRelation = MealRelation.IRRELEVANT
    )
    
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

