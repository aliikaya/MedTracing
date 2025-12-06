package com.medtracking.app.presentation.history

import com.medtracking.app.domain.model.IntakeStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Günlük intake grubu UI modeli
 */
data class DailyIntakeGroupUi(
    val date: LocalDate,
    val intakes: List<DailyIntakeUi>
)

/**
 * Tek bir intake UI modeli
 */
data class DailyIntakeUi(
    val time: LocalTime,
    val status: IntakeStatus
)

/**
 * Uyum sonucu UI modeli
 */
data class AdherenceResultUi(
    val planned: Int,
    val taken: Int,
    val missed: Int,
    val adherencePercentText: String // örn. "85%" veya "--"
)

/**
 * İlaç geçmişi ekranı UI state
 */
data class MedicationHistoryUiState(
    val medicationName: String = "",
    val fromDate: LocalDate = LocalDate.now().minusDays(6),
    val toDate: LocalDate = LocalDate.now(),
    val dailyGroups: List<DailyIntakeGroupUi> = emptyList(),
    val adherenceResult: AdherenceResultUi? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

