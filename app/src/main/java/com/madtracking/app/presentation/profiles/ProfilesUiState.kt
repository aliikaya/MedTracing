package com.madtracking.app.presentation.profiles

import com.madtracking.app.domain.model.Profile

data class ProfilesUiState(
    val profiles: List<Profile> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

