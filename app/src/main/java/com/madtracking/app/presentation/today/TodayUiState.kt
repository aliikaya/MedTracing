package com.madtracking.app.presentation.today

import com.madtracking.app.domain.model.IntakeStatus
import com.madtracking.app.domain.model.MealRelation
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
    val status: IntakeStatus,
    val remainingDays: Int?, // null = süresiz
    val isExpired: Boolean = false,
    val mealRelation: MealRelation = MealRelation.IRRELEVANT
) {
    /**
     * Kalan gün bilgisini kullanıcı dostu formatta döndürür.
     */
    fun getRemainingDaysDisplay(): String? {
        return when {
            isExpired -> "Tedavi bitti"
            remainingDays == null -> null // Süresiz
            remainingDays == 0 -> "Son gün"
            remainingDays == 1 -> "1 gün kaldı"
            else -> "$remainingDays gün kaldı"
        }
    }
}

data class TodayUiState(
    val items: List<TodayIntakeUi> = emptyList(),
    val profileId: Long? = null,
    val profileName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
