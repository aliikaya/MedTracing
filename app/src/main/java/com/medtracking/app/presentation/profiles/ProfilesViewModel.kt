package com.medtracking.app.presentation.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtracking.app.domain.model.Profile
import com.medtracking.app.domain.usecase.GetProfilesUseCase
import com.medtracking.app.domain.usecase.UpsertProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val getProfilesUseCase: GetProfilesUseCase,
    private val upsertProfileUseCase: UpsertProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilesUiState())
    val uiState: StateFlow<ProfilesUiState> = _uiState.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getProfilesUseCase()
                .catch { error ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = error.message) 
                    }
                }
                .collect { profiles ->
                    _uiState.update { 
                        it.copy(profiles = profiles, isLoading = false, error = null) 
                    }
                }
        }
    }

    fun showAddDialog() {
        _uiState.update { 
            it.copy(
                showAddDialog = true, 
                newProfileName = "", 
                newProfileEmoji = "👤",
                newProfileRelation = ""
            ) 
        }
    }

    fun hideAddDialog() {
        _uiState.update { it.copy(showAddDialog = false) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(newProfileName = name) }
    }

    fun onEmojiChange(emoji: String) {
        _uiState.update { it.copy(newProfileEmoji = emoji) }
    }

    fun onRelationChange(relation: String) {
        _uiState.update { it.copy(newProfileRelation = relation) }
    }

    fun saveProfile() {
        val state = _uiState.value
        if (state.newProfileName.isBlank()) return

        viewModelScope.launch {
            try {
                val profile = Profile(
                    name = state.newProfileName.trim(),
                    avatarEmoji = state.newProfileEmoji.ifBlank { "👤" },
                    relation = state.newProfileRelation.ifBlank { null },
                    isActive = true
                )
                upsertProfileUseCase(profile)
                _uiState.update { it.copy(showAddDialog = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
