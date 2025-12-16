package com.medtracking.app.data.local.entity

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
    val dosage: String, // "amount|unit" format (e.g., "1.0|TABLET")
    val scheduleTimes: String, // Comma-separated LocalTime strings (e.g., "08:00,14:00,20:00")
    val startDate: String, // LocalDate as ISO string
    val endDate: String?, // LocalDate as ISO string or null
    val durationInDays: Int?, // Tedavi süresi (gün), null = süresiz
    val isActive: Boolean,
    val importance: String, // MedicationImportance enum as String
    val mealRelation: String = "IRRELEVANT", // MealRelation enum as String
    val notes: String?,
    val stockCount: Int?,
    val createdAt: Long,
    val remoteId: String? = null,
    val profileRemoteId: String? = null,
    val updatedAt: Long = createdAt,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false
)
