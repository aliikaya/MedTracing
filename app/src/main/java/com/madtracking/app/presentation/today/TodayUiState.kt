package com.madtracking.app.presentation.today

import com.madtracking.app.domain.model.IntakeStatus
import java.time.LocalTime

/**
 * UI'da gösterilecek zenginleştirilmiş Intake modeli
 */
data class TodayIntakeUi(
    val intakeId: Long,
    val medicationId: Long,
    val medicationName: String,
    val dosageDisplay: String,
    val time: LocalTime,
    val status: IntakeStatus
)

data class TodayUiState(
    val items: List<TodayIntakeUi> = emptyList(),
    val profileId: Long? = null,
    val profileName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
