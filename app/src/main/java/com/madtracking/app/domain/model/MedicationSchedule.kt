package com.madtracking.app.domain.model

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Faz 2: Basit zamanlama - sadece günlük saatler.
 * Her gün aynı saatlerde alınacak.
 */
data class MedicationSchedule(
    val timesOfDay: List<LocalTime> // örn. [08:00, 13:00, 20:00]
) {
    fun toDisplayString(): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return timesOfDay.joinToString(", ") { it.format(formatter) }
    }

    fun toStorageString(): String {
        return timesOfDay.joinToString(",") { it.toString() }
    }

    companion object {
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun fromStorageString(value: String): MedicationSchedule {
            if (value.isBlank()) return MedicationSchedule(emptyList())
            val times = value.split(",").mapNotNull { timeStr ->
                try {
                    LocalTime.parse(timeStr.trim())
                } catch (e: Exception) {
                    try {
                        LocalTime.parse(timeStr.trim(), timeFormatter)
                    } catch (e2: Exception) {
                        null
                    }
                }
            }
            return MedicationSchedule(times)
        }

        fun everyDayAt(vararg times: LocalTime): MedicationSchedule {
            return MedicationSchedule(times.toList())
        }

        fun fromTimeStrings(timeStrings: String): MedicationSchedule {
            return fromStorageString(timeStrings)
        }
    }
}
