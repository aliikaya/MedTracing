package com.madtracking.app.presentation.addmedication

import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddMedicationUiState(
    val name: String = "",
    val form: MedicationForm = MedicationForm.TABLET,
    val dosageAmount: String = "1",
    val dosageUnit: DosageUnit = DosageUnit.TABLET,
    val timesInput: String = "08:00,20:00", // Virgülle ayrılmış saatler
    val importance: MedicationImportance = MedicationImportance.REGULAR,
    val mealRelation: MealRelation = MealRelation.IRRELEVANT, // Kullanım talimatı
    val notes: String = "",
    // Duration fields
    val isIndefinite: Boolean = true, // Süresiz mi?
    val durationDays: String = "7", // Gün sayısı (String olarak form girişi için)
    // State
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() {
            val nameValid = name.isNotBlank()
            val dosageValid = dosageAmount.toDoubleOrNull()?.let { it > 0 } == true
            val timesValid = timesInput.isNotBlank()
            val durationValid = isIndefinite || (durationDays.toIntOrNull()?.let { it > 0 } == true)
            return nameValid && dosageValid && timesValid && durationValid
        }

    /**
     * Hesaplanan bitiş tarihini gösterir.
     * Bugün başlayarak X gün kullanılırsa bitiş tarihi.
     */
    fun calculateEndDate(): LocalDate? {
        if (isIndefinite) return null
        val days = durationDays.toIntOrNull() ?: return null
        if (days <= 0) return null
        return LocalDate.now().plusDays(days.toLong() - 1)
    }

    /**
     * Bitiş tarihini okunabilir formatta döndürür.
     */
    fun getEndDateDisplay(): String {
        val endDate = calculateEndDate() ?: return ""
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        return endDate.format(formatter)
    }
}
