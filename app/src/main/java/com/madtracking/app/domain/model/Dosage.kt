package com.madtracking.app.domain.model

data class Dosage(
    val amount: Double,
    val unit: DosageUnit
) {
    fun toDisplayString(): String {
        return "$amount ${unit.name.lowercase()}"
    }

    companion object {
        fun fromString(value: String): Dosage? {
            return try {
                val parts = value.split(" ")
                if (parts.size != 2) return null
                val amount = parts[0].toDouble()
                val unit = DosageUnit.valueOf(parts[1].uppercase())
                Dosage(amount, unit)
            } catch (e: Exception) {
                null
            }
        }
    }
}

