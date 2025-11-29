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
class AddMedicationViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

    private var profileId: Long = 0L

    fun setProfileId(id: Long) {
        profileId = id
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onFormChange(form: MedicationForm) {
        _uiState.update { it.copy(form = form) }
    }

    fun onDosageAmountChange(amount: String) {
        _uiState.update { it.copy(dosageAmount = amount) }
    }

    fun onDosageUnitChange(unit: DosageUnit) {
        _uiState.update { it.copy(dosageUnit = unit) }
    }

    fun onTimesInputChange(times: String) {
        _uiState.update { it.copy(timesInput = times) }
    }

    fun onImportanceChange(importance: MedicationImportance) {
        _uiState.update { it.copy(importance = importance) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onMealRelationChange(mealRelation: MealRelation) {
        _uiState.update { it.copy(mealRelation = mealRelation) }
    }

    fun onIndefiniteChange(isIndefinite: Boolean) {
        _uiState.update { it.copy(isIndefinite = isIndefinite) }
    }

    fun onDurationDaysChange(days: String) {
        // Sadece sayıları kabul et
        val filtered = days.filter { it.isDigit() }
        _uiState.update { it.copy(durationDays = filtered) }
    }

    fun onSave() {
        val state = _uiState.value
        if (!state.isValid) {
            _uiState.update { it.copy(error = "Lütfen tüm alanları doğru doldurun") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val times = parseTimesInput(state.timesInput)
                if (times.isEmpty()) {
                    _uiState.update { 
                        it.copy(isLoading = false, error = "Geçerli saat formatı girin (örn: 08:00,20:00)") 
                    }
                    return@launch
                }

                // Duration hesapla
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
                    startDate = LocalDate.now(),
                    endDate = null, // endDateOrNull() ile hesaplanacak
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

    private fun parseTimesInput(input: String): List<LocalTime> {
        return input.split(",")
            .mapNotNull { timeStr ->
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
            }
            .sortedBy { it }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
