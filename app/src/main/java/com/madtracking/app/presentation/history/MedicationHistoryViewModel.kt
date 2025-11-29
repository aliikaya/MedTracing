package com.madtracking.app.presentation.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madtracking.app.domain.model.Intake
import com.madtracking.app.domain.model.Medication
import com.madtracking.app.domain.repository.MedicationRepository
import com.madtracking.app.domain.usecase.CalculateAdherenceForMedicationUseCase
import com.madtracking.app.domain.usecase.GetIntakeHistoryForMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MedicationHistoryViewModel @Inject constructor(
    private val getIntakeHistoryForMedicationUseCase: GetIntakeHistoryForMedicationUseCase,
    private val calculateAdherenceForMedicationUseCase: CalculateAdherenceForMedicationUseCase,
    private val medicationRepository: MedicationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationHistoryUiState())
    val uiState: StateFlow<MedicationHistoryUiState> = _uiState.asStateFlow()

    private val medicationId: Long? = savedStateHandle.get<Long>("medicationId")

    // Varsayılan tarih aralığı: son 7 gün
    private val toDate: LocalDate = LocalDate.now()
    private val fromDate: LocalDate = toDate.minusDays(6)

    init {
        medicationId?.let { id ->
            loadMedicationHistory(id)
        } ?: run {
            _uiState.update { it.copy(errorMessage = "İlaç bulunamadı") }
        }
    }

    private fun loadMedicationHistory(medicationId: Long) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true, 
                    fromDate = fromDate, 
                    toDate = toDate
                ) 
            }

            try {
                // 1. İlaç bilgisini al
                val medication = medicationRepository.getMedicationByIdOnce(medicationId)
                
                if (medication == null) {
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = "İlaç bulunamadı") 
                    }
                    return@launch
                }

                _uiState.update { it.copy(medicationName = medication.name) }

                // 2. Intake geçmişini ve adherence'ı paralel olarak dinle
                combine(
                    getIntakeHistoryForMedicationUseCase(medicationId, fromDate, toDate),
                    calculateAdherenceForMedicationUseCase(medication, fromDate, toDate)
                ) { intakes, adherenceResult ->
                    Pair(intakes, adherenceResult)
                }.catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, errorMessage = error.message) 
                    }
                }.collect { (intakes, adherenceResult) ->
                    val dailyGroups = mapToDailyGroups(intakes)
                    val adherenceUi = mapToAdherenceUi(adherenceResult)
                    
                    _uiState.update { 
                        it.copy(
                            dailyGroups = dailyGroups,
                            adherenceResult = adherenceUi,
                            isLoading = false,
                            errorMessage = null
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, errorMessage = e.message) 
                }
            }
        }
    }

    private fun mapToDailyGroups(intakes: List<Intake>): List<DailyIntakeGroupUi> {
        return intakes
            .groupBy { it.plannedTime.toLocalDate() }
            .map { (date, dayIntakes) ->
                DailyIntakeGroupUi(
                    date = date,
                    intakes = dayIntakes
                        .sortedBy { it.plannedTime }
                        .map { intake ->
                            DailyIntakeUi(
                                time = intake.plannedTime.toLocalTime(),
                                status = intake.status
                            )
                        }
                )
            }
            .sortedByDescending { it.date } // En yeni gün en üstte
    }

    private fun mapToAdherenceUi(result: com.madtracking.app.domain.model.AdherenceResult): AdherenceResultUi {
        val percentText = result.adherenceRatio?.let { ratio ->
            "${(ratio * 100).toInt()}%"
        } ?: "--"
        
        return AdherenceResultUi(
            planned = result.planned,
            taken = result.taken,
            missed = result.missed,
            adherencePercentText = percentText
        )
    }

    fun refresh() {
        medicationId?.let { loadMedicationHistory(it) }
    }
}

