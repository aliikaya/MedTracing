package com.madtracking.app.presentation.addmedication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madtracking.app.domain.model.*
import com.madtracking.app.domain.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddMedicationWizardViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationWizardState())
    val uiState: StateFlow<AddMedicationWizardState> = _uiState.asStateFlow()

    private var profileId: Long = 0L

    fun setProfileId(id: Long) {
        profileId = id
    }

    // ==================== Navigation ====================

    fun goToNextStep() {
        val currentState = _uiState.value
        if (currentState.canGoNext) {
            val nextStep = WizardStep.fromIndex(currentState.currentStep.index + 1)
            _uiState.update { it.copy(currentStep = nextStep) }
        }
    }

    fun goToPreviousStep() {
        val currentState = _uiState.value
        if (currentState.canGoBack) {
            val previousStep = WizardStep.fromIndex(currentState.currentStep.index - 1)
            _uiState.update { it.copy(currentStep = previousStep) }
        }
    }

    fun goToStep(step: WizardStep) {
        // Only allow going to completed steps or current step
        val currentState = _uiState.value
        if (step.index <= currentState.currentStep.index) {
            _uiState.update { it.copy(currentStep = step) }
        }
    }

    // ==================== Step 1: General Info ====================

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onFormChange(form: MedicationForm) {
        // Also update dosage unit based on form
        val suggestedUnit = when (form) {
            MedicationForm.TABLET, MedicationForm.CAPSULE -> DosageUnit.TABLET
            MedicationForm.SYRUP -> DosageUnit.ML
            MedicationForm.DROP -> DosageUnit.DROP
            else -> DosageUnit.TABLET
        }
        _uiState.update { 
            it.copy(form = form, dosageUnit = suggestedUnit) 
        }
    }

    // ==================== Step 2: Dosage ====================

    fun onDosageAmountChange(amount: String) {
        // Allow only valid decimal input
        val filtered = amount.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(',', '.')
        _uiState.update { it.copy(dosageAmount = filtered) }
    }

    fun onDosageUnitChange(unit: DosageUnit) {
        _uiState.update { it.copy(dosageUnit = unit) }
    }

    // ==================== Step 3: Schedule ====================

    fun addTime(time: String) {
        val currentState = _uiState.value
        if (time.isNotBlank() && !currentState.selectedTimes.contains(time)) {
            val newTimes = (currentState.selectedTimes + time).sorted()
            _uiState.update { it.copy(selectedTimes = newTimes) }
        }
    }

    fun removeTime(time: String) {
        val currentState = _uiState.value
        val newTimes = currentState.selectedTimes.filter { it != time }
        _uiState.update { it.copy(selectedTimes = newTimes) }
    }

    fun toggleTime(time: String) {
        val currentState = _uiState.value
        if (currentState.selectedTimes.contains(time)) {
            removeTime(time)
        } else {
            addTime(time)
        }
    }

    fun onMealRelationChange(mealRelation: MealRelation) {
        _uiState.update { it.copy(mealRelation = mealRelation) }
    }

    // ==================== Step 4: Duration ====================

    fun onStartDateChange(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onIndefiniteChange(isIndefinite: Boolean) {
        _uiState.update { it.copy(isIndefinite = isIndefinite) }
    }

    fun onDurationDaysChange(days: String) {
        val filtered = days.filter { it.isDigit() }
        _uiState.update { it.copy(durationDays = filtered) }
    }

    // ==================== Step 5: Review ====================

    fun onImportanceChange(importance: MedicationImportance) {
        _uiState.update { it.copy(importance = importance) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    // ==================== Save ====================

    fun onSave() {
        val state = _uiState.value
        if (!state.isAllValid) {
            _uiState.update { it.copy(error = "Lütfen tüm alanları doğru doldurun") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val times = parseTimesInput(state.selectedTimes)
                if (times.isEmpty()) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Geçerli saat formatı girin") 
                    }
                    return@launch
                }

                val durationInDays: Int? = if (state.isIndefinite) {
                    null
                } else {
                    state.durationDays.toIntOrNull()?.takeIf { it > 0 }
                }

                val medication = Medication(
                    profileId = profileId,
                    name = state.name.trim(),
                    form = state.form,
                    dosage = Dosage(
                        amount = state.dosageAmount.toDouble(),
                        unit = state.dosageUnit
                    ),
                    schedule = MedicationSchedule(timesOfDay = times),
                    startDate = state.startDate,
                    endDate = null,
                    durationInDays = durationInDays,
                    isActive = true,
                    importance = state.importance,
                    mealRelation = state.mealRelation,
                    notes = state.notes.ifBlank { null }
                )

                addMedicationUseCase(medication)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, error = e.message ?: "Bir hata oluştu") 
                }
            }
        }
    }

    private fun parseTimesInput(times: List<String>): List<LocalTime> {
        return times.mapNotNull { timeStr ->
            try {
                val parts = timeStr.trim().split(":")
                if (parts.size == 2) {
                    LocalTime.of(parts[0].toInt(), parts[1].toInt())
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

