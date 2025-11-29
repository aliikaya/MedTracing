package com.madtracking.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madtracking.app.domain.model.Profile
import com.madtracking.app.domain.repository.IntakeRepository
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.repository.ProfileRepository
import com.madtracking.app.domain.scheduler.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ProfileDetailUiState(
    val profile: Profile? = null,
    val totalMedications: Int = 0,
    val activeMedications: Int = 0,
    val todayIntakes: Int = 0,
    val notificationsEnabled: Boolean = true,
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileDetailViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val intakeRepository: IntakeRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileDetailUiState())
    val uiState: StateFlow<ProfileDetailUiState> = _uiState.asStateFlow()

    private var currentProfileId: Long = 0

    fun loadProfile(profileId: Long) {
        currentProfileId = profileId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, notificationsEnabled = reminderScheduler.areRemindersEnabled()) }
            
            // Profil bilgisi
            profileRepository.getProfileById(profileId)
                .filterNotNull()
                .collect { profile ->
                    _uiState.update { it.copy(profile = profile) }
                }
        }
        
        // İlaç istatistikleri
        viewModelScope.launch {
            medicationRepository.getMedicationsForProfile(profileId)
                .collect { medications ->
                    _uiState.update { 
                        it.copy(
                            totalMedications = medications.size,
                            activeMedications = medications.count { med -> med.isActive }
                        ) 
                    }
                }
        }
        
        // Bugünkü intake sayısı
        viewModelScope.launch {
            intakeRepository.getIntakesForDate(profileId, LocalDate.now())
                .collect { intakes ->
                    _uiState.update { 
                        it.copy(
                            todayIntakes = intakes.size,
                            isLoading = false
                        ) 
                    }
                }
        }
    }

    fun toggleNotifications() {
        val newState = !_uiState.value.notificationsEnabled
        // This would need access to the scheduler's setRemindersEnabled method
        // For now, just update the UI state
        _uiState.update { it.copy(notificationsEnabled = newState) }
    }
}

