package com.madtracking.app.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.MealRelation
import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.repository.ProfileRepository
import com.madtracking.app.domain.scheduler.ReminderScheduler
import com.madtracking.app.domain.usecase.GetTodayIntakesUseCase
import com.madtracking.app.domain.usecase.MarkIntakeMissedUseCase
import com.madtracking.app.domain.usecase.MarkIntakeTakenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayIntakesUseCase: GetTodayIntakesUseCase,
    private val markIntakeTakenUseCase: MarkIntakeTakenUseCase,
    private val markIntakeMissedUseCase: MarkIntakeMissedUseCase,
    private val medicationRepository: MedicationRepository,
    private val profileRepository: ProfileRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private var currentProfileId: Long? = null

    // İlaç bilgilerini cache'lemek için
    private val medicationCache = mutableMapOf<Long, Medication>()

    fun loadIntakes(profileId: Long) {
        if (currentProfileId == profileId) return // Already loaded
        currentProfileId = profileId
        
        loadProfileName(profileId)
        loadTodayIntakes(profileId)
    }

    private fun loadProfileName(profileId: Long) {
        viewModelScope.launch {
            profileRepository.getProfileById(profileId)
                .filterNotNull()
                .collect { profile ->
                    _uiState.update { it.copy(profileName = profile.name) }
                }
        }
    }

    private fun loadTodayIntakes(profileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, profileId = profileId) }
            
            getTodayIntakesUseCase(profileId)
                .catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message) 
                    }
                }
                .collect { intakes ->
                    val uiItems = intakes.map { intake ->
                        mapToUiModel(intake)
                    }.sortedBy { it.time }
                    
                    _uiState.update { 
                        it.copy(items = uiItems, isLoading = false, error = null) 
                    }
                }
        }
    }

    private suspend fun mapToUiModel(intake: Intake): TodayIntakeUi {
        val medication = getMedication(intake.medicationId)
        val today = LocalDate.now()
        
        return TodayIntakeUi(
            intakeId = intake.id,
            medicationId = intake.medicationId,
            medicationName = medication?.name ?: "Bilinmeyen İlaç",
            dosageDisplay = medication?.dosage?.toDisplayString() ?: "",
            time = intake.plannedTime.toLocalTime(),
            status = intake.status,
            remainingDays = medication?.remainingDays(today),
            isExpired = medication?.isExpired(today) ?: false,
            mealRelation = medication?.mealRelation ?: MealRelation.IRRELEVANT
        )
    }

    private suspend fun getMedication(medicationId: Long): Medication? {
        return medicationCache.getOrPut(medicationId) {
            medicationRepository.getMedicationByIdOnce(medicationId) ?: return null
        }
    }

    fun onMarkTaken(intakeId: Long) {
        viewModelScope.launch {
            try {
                // İlacı alındı olarak işaretle
                markIntakeTakenUseCase(intakeId)
                
                // Bu Intake için hatırlatıcıyı iptal et
                reminderScheduler.cancelIntakeReminder(intakeId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onMarkMissed(intakeId: Long) {
        viewModelScope.launch {
            try {
                // İlacı kaçırıldı olarak işaretle
                markIntakeMissedUseCase(intakeId)
                
                // Bu Intake için hatırlatıcıyı iptal et
                reminderScheduler.cancelIntakeReminder(intakeId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun refresh() {
        currentProfileId?.let { 
            currentProfileId = null // Force reload
            loadIntakes(it) 
        }
    }
}
