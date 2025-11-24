package com.madtracking.app.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Represents when and how often a medication should be taken.
 * For Phase 1, we keep it simple with times and optional days.
 */
data class MedicationSchedule(
    val times: List<LocalTime>, // e.g., [08:00, 14:00, 20:00]
    val daysOfWeek: List<DayOfWeek>? = null // null means every day
) {
    fun toDisplayString(): String {
        val timesStr = times.joinToString(", ") { it.toString() }
        val daysStr = daysOfWeek?.joinToString(", ") { it.name } ?: "Every day"
        return "$daysStr at $timesStr"
    }

    companion object {
        fun everyDayAt(vararg times: LocalTime): MedicationSchedule {
            return MedicationSchedule(times = times.toList(), daysOfWeek = null)
        }

        fun specificDaysAt(times: List<LocalTime>, days: List<DayOfWeek>): MedicationSchedule {
            return MedicationSchedule(times = times, daysOfWeek = days)
        }
    }
}

