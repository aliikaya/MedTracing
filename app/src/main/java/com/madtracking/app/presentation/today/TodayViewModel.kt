package com.madtracking.app.presentation.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madtracking.app.domain.usecase.GetTodayIntakesUseCase
import com.madtracking.app.domain.usecase.MarkIntakeTakenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val getTodayIntakesUseCase: GetTodayIntakesUseCase,
    private val markIntakeTakenUseCase: MarkIntakeTakenUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    private val profileId: Long? = savedStateHandle.get<String>("profileId")?.toLongOrNull()

    init {
        profileId?.let { loadTodayIntakes(it) }
    }

    private fun loadTodayIntakes(profileId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedProfileId = profileId
            )
            getTodayIntakesUseCase(profileId)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
                .collect { intakes ->
                    _uiState.value = _uiState.value.copy(
                        intakes = intakes,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun markTaken(intakeId: Long) {
        viewModelScope.launch {
            try {
                markIntakeTakenUseCase(intakeId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}

