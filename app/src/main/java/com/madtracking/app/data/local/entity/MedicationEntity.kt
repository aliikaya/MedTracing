package com.madtracking.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val profileId: Long,
    val name: String,
    val form: String, // MedicationForm enum as String
    val dosageAmount: Double,
    val dosageUnit: String, // DosageUnit enum as String
    val scheduleTimes: String, // Comma-separated LocalTime strings (e.g., "08:00,14:00,20:00")
    val scheduleDays: String?, // Comma-separated DayOfWeek ordinals or null for every day
    val startDate: String, // LocalDate as ISO string
    val endDate: String?, // LocalDate as ISO string or null
    val isActive: Boolean,
    val importance: String, // MedicationImportance enum as String
    val notes: String?,
    val stockCount: Int?,
    val createdAt: Long
)

