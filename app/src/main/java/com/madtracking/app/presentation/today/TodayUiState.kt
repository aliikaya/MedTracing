package com.madtracking.app.presentation.today

import com.madtracking.app.domain.model.Intake

data class TodayUiState(
    val intakes: List<Intake> = emptyList(),
    val selectedProfileId: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

