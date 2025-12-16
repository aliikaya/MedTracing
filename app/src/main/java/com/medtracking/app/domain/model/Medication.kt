package com.medtracking.app.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Represents a medication or supplement that needs to be tracked.
 * 
 * Tedavi süresi mantığı:
 * - durationInDays == null && endDate == null → Süresiz kullanım
 * - durationInDays != null → startDate + (durationInDays - 1) gün = bitiş tarihi
 * - endDate != null → Doğrudan belirtilmiş bitiş tarihi
 * 
 * Öncelik: durationInDays > endDate (durationInDays varsa endDate hesaplanır)
 */
data class Medication(
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val form: MedicationForm,
    val dosage: Dosage,
    val schedule: MedicationSchedule,
    val startDate: LocalDate,
    val endDate: LocalDate? = null, // Doğrudan belirtilen bitiş tarihi
    val durationInDays: Int? = null, // Tedavi süresi (gün), null = süresiz
    val isActive: Boolean = true,
    val importance: MedicationImportance = MedicationImportance.REGULAR,
    val mealRelation: MealRelation = MealRelation.IRRELEVANT, // Kullanım talimatı
    val notes: String? = null,
    val stockCount: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val profileRemoteId: String? = null,
    val updatedAt: Long = createdAt,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false
) {
    /**
     * Hesaplanmış bitiş tarihini döndürür.
     * - durationInDays varsa: startDate + (durationInDays - 1) gün
     * - Yoksa endDate'i kullanır
     * - İkisi de yoksa null (süresiz)
     */
    fun endDateOrNull(): LocalDate? {
        return when {
            durationInDays != null && durationInDays > 0 -> {
                startDate.plusDays(durationInDays.toLong() - 1)
            }
            else -> endDate
        }
    }

    /**
     * Belirli bir tarihte bu ilacın aktif olup olmadığını kontrol eder.
     */
    fun isActiveOnDate(date: LocalDate): Boolean {
        if (!isActive) return false
        if (date < startDate) return false
        
        val calculatedEndDate = endDateOrNull()
        if (calculatedEndDate != null && date > calculatedEndDate) return false
        
        return true
    }

    /**
     * Bugün itibariyle kalan gün sayısını hesaplar.
     * - Süresiz ise null döner
     * - Süre bitmişse 0 döner
     */
    fun remainingDays(today: LocalDate = LocalDate.now()): Int? {
        val calculatedEndDate = endDateOrNull() ?: return null
        
        return if (today > calculatedEndDate) {
            0
        } else {
            ChronoUnit.DAYS.between(today, calculatedEndDate).toInt() + 1
        }
    }

    /**
     * Tedavi süresinin bitip bitmediğini kontrol eder.
     */
    fun isExpired(today: LocalDate = LocalDate.now()): Boolean {
        val calculatedEndDate = endDateOrNull() ?: return false
        return today > calculatedEndDate
    }

    /**
     * Süresiz kullanım mı kontrol eder.
     */
    fun isIndefinite(): Boolean {
        return durationInDays == null && endDate == null
    }
}
