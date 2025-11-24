package com.madtracking.app.data.mapper

import com.madtracking.app.data.local.entity.MedicationEntity
import com.madtracking.app.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

fun MedicationEntity.toDomain(): Medication {
    val times = scheduleTimes.split(",").mapNotNull { 
        runCatching { LocalTime.parse(it.trim()) }.getOrNull() 
    }
    
    val days = scheduleDays?.split(",")?.mapNotNull {
        runCatching { DayOfWeek.of(it.trim().toInt()) }.getOrNull()
    }
    
    return Medication(
        id = id,
        profileId = profileId,
        name = name,
        form = MedicationForm.valueOf(form),
        dosage = Dosage(dosageAmount, DosageUnit.valueOf(dosageUnit)),
        schedule = MedicationSchedule(times, days),
        startDate = LocalDate.parse(startDate),
        endDate = endDate?.let { LocalDate.parse(it) },
        isActive = isActive,
        importance = MedicationImportance.valueOf(importance),
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt
    )
}

fun Medication.toEntity(): MedicationEntity {
    val timesStr = schedule.times.joinToString(",") { it.toString() }
    val daysStr = schedule.daysOfWeek?.joinToString(",") { it.value.toString() }
    
    return MedicationEntity(
        id = id,
        profileId = profileId,
        name = name,
        form = form.name,
        dosageAmount = dosage.amount,
        dosageUnit = dosage.unit.name,
        scheduleTimes = timesStr,
        scheduleDays = daysStr,
        startDate = startDate.toString(),
        endDate = endDate?.toString(),
        isActive = isActive,
        importance = importance.name,
        notes = notes,
        stockCount = stockCount,
        createdAt = createdAt
    )
}

