package com.madtracking.app.domain.model

data class Dosage(
    val amount: Double,
    val unit: DosageUnit
) {
    fun toDisplayString(): String {
        val amountStr = if (amount == amount.toLong().toDouble()) {
            amount.toLong().toString()
        } else {
            amount.toString()
        }
        return "$amountStr ${unit.displayName}"
    }

    fun toStorageString(): String {
        return "$amount|${unit.name}"
    }

    companion object {
        fun fromStorageString(value: String): Dosage {
            return try {
                val parts = value.split("|")
                if (parts.size != 2) return Dosage(1.0, DosageUnit.TABLET)
                val amount = parts[0].toDouble()
                val unit = DosageUnit.fromString(parts[1])
                Dosage(amount, unit)
            } catch (e: Exception) {
                Dosage(1.0, DosageUnit.TABLET)
            }
        }
    }
}
