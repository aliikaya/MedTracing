package com.madtracking.app.presentation.addmedication

import com.madtracking.app.domain.model.DosageUnit
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.domain.model.MedicationForm
import com.madtracking.app.domain.model.MedicationImportance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Wizard step enumeration
 */
enum class WizardStep(val index: Int, val title: String) {
    GENERAL_INFO(0, "Genel"),
    DOSAGE(1, "Dozaj"),
    SCHEDULE(2, "Zamanlama"),
    DURATION(3, "Süre"),
    REVIEW(4, "Özet");

    companion object {
        fun fromIndex(index: Int): WizardStep = entries.first { it.index == index }
        val totalSteps = entries.size
    }
}

/**
 * Complete wizard UI state
 */
data class AddMedicationWizardState(
    // Step 1: General Info
    val name: String = "",
    val form: MedicationForm = MedicationForm.TABLET,
    
    // Step 2: Dosage
    val dosageAmount: String = "1",
    val dosageUnit: DosageUnit = DosageUnit.TABLET,
    
    // Step 3: Schedule
    val selectedTimes: List<String> = listOf("08:00", "20:00"),
    val mealRelation: MealRelation = MealRelation.IRRELEVANT,
    
    // Step 4: Duration
    val startDate: LocalDate = LocalDate.now(),
    val isIndefinite: Boolean = true,
    val durationDays: String = "7",
    
    // Step 5: Review extras
    val importance: MedicationImportance = MedicationImportance.REGULAR,
    val notes: String = "",
    
    // Wizard state
    val currentStep: WizardStep = WizardStep.GENERAL_INFO,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    // Validation for each step
    val isStep1Valid: Boolean
        get() = name.isNotBlank()
    
    val isStep2Valid: Boolean
        get() = dosageAmount.toDoubleOrNull()?.let { it > 0 } == true
    
    val isStep3Valid: Boolean
        get() = selectedTimes.isNotEmpty()
    
    val isStep4Valid: Boolean
        get() = isIndefinite || (durationDays.toIntOrNull()?.let { it > 0 } == true)
    
    val isCurrentStepValid: Boolean
        get() = when (currentStep) {
            WizardStep.GENERAL_INFO -> isStep1Valid
            WizardStep.DOSAGE -> isStep2Valid
            WizardStep.SCHEDULE -> isStep3Valid
            WizardStep.DURATION -> isStep4Valid
            WizardStep.REVIEW -> true
        }
    
    val isAllValid: Boolean
        get() = isStep1Valid && isStep2Valid && isStep3Valid && isStep4Valid

    val canGoBack: Boolean
        get() = currentStep.index > 0

    val canGoNext: Boolean
        get() = currentStep.index < WizardStep.totalSteps - 1 && isCurrentStepValid

    val isLastStep: Boolean
        get() = currentStep == WizardStep.REVIEW

    /**
     * Calculate end date from start date and duration
     */
    fun calculateEndDate(): LocalDate? {
        if (isIndefinite) return null
        val days = durationDays.toIntOrNull() ?: return null
        if (days <= 0) return null
        return startDate.plusDays(days.toLong() - 1)
    }

    /**
     * Format end date for display
     */
    fun getEndDateDisplay(): String {
        val endDate = calculateEndDate() ?: return ""
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
        return endDate.format(formatter)
    }

    /**
     * Format start date for display
     */
    fun getStartDateDisplay(): String {
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
        return startDate.format(formatter)
    }

    /**
     * Get times as comma-separated string (for legacy support)
     */
    fun getTimesAsString(): String = selectedTimes.sorted().joinToString(",")

    /**
     * Get form display name
     */
    fun getFormDisplayName(): String = when (form) {
        MedicationForm.TABLET -> "Tablet"
        MedicationForm.CAPSULE -> "Kapsül"
        MedicationForm.SYRUP -> "Şurup"
        MedicationForm.DROP -> "Damla"
        MedicationForm.INJECTION -> "Enjeksiyon"
        MedicationForm.CREAM -> "Krem"
        MedicationForm.SPRAY -> "Sprey"
        MedicationForm.POWDER -> "Toz"
        MedicationForm.OTHER -> "Diğer"
    }

    /**
     * Get form icon
     */
    fun getFormIcon(): String = when (form) {
        MedicationForm.TABLET -> "💊"
        MedicationForm.CAPSULE -> "💊"
        MedicationForm.SYRUP -> "🧴"
        MedicationForm.DROP -> "💧"
        MedicationForm.INJECTION -> "💉"
        MedicationForm.CREAM -> "🧴"
        MedicationForm.SPRAY -> "💨"
        MedicationForm.POWDER -> "🧂"
        MedicationForm.OTHER -> "💊"
    }

    /**
     * Get meal relation display text
     */
    fun getMealRelationText(): String = when (mealRelation) {
        MealRelation.BEFORE_MEAL -> "Yemekten önce"
        MealRelation.WITH_MEAL -> "Yemekle birlikte"
        MealRelation.AFTER_MEAL -> "Yemekten sonra"
        MealRelation.EMPTY_STOMACH -> "Aç karnına"
        MealRelation.IRRELEVANT -> "Farketmez"
    }

    /**
     * Get importance display text
     */
    fun getImportanceText(): String = when (importance) {
        MedicationImportance.CRITICAL -> "🔴 Kritik"
        MedicationImportance.REGULAR -> "🟡 Normal"
        MedicationImportance.OPTIONAL -> "🟢 Opsiyonel"
    }

    /**
     * Get duration display text
     */
    fun getDurationText(): String = if (isIndefinite) {
        "Süresiz kullanım"
    } else {
        val days = durationDays.toIntOrNull() ?: 0
        "$days gün"
    }

    /**
     * Get dosage display text
     */
    fun getDosageText(): String {
        val amount = dosageAmount.toDoubleOrNull() ?: 0.0
        val formattedAmount = if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }
        return "$formattedAmount ${dosageUnit.displayName}"
    }

    /**
     * Get schedule display text
     */
    fun getScheduleText(): String {
        return if (selectedTimes.size == 1) {
            "Günde 1 kez (${selectedTimes.first()})"
        } else {
            "Günde ${selectedTimes.size} kez"
        }
    }
}

