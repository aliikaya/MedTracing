package com.madtracking.app.domain.model

enum class DosageUnit(val displayName: String) {
    TABLET("tablet"),
    CAPSULE("kapsül"),
    ML("ml"),
    SPOON("kaşık"),
    DROP("damla");

    companion object {
        fun fromString(value: String): DosageUnit {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: TABLET
        }
    }
}
