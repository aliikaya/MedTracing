package com.medtracking.app.presentation.profiles

import com.medtracking.app.domain.model.Profile

data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val newProfileName: String = "",
    val newProfileEmoji: String = "👤",
    val newProfileRelation: String = ""
)
