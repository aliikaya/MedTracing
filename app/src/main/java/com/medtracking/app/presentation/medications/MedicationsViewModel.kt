package com.medtracking.app.presentation.medications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtracking.app.domain.model.Medication
import com.medtracking.app.domain.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationsUiState(
    val medications: List<Medication> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MedicationsViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationsUiState())
    val uiState: StateFlow<MedicationsUiState> = _uiState.asStateFlow()

    fun loadMedications(profileId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            medicationRepository.getMedicationsForProfile(profileId)
                .catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message) 
                    }
                }
                .collect { medications ->
                    _uiState.update { 
                        it.copy(
                            medications = medications.sortedByDescending { it.isActive },
                            isLoading = false,
                            error = null
                        ) 
                    }
                }
        }
    }
}

