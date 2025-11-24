package com.madtracking.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.madtracking.app.data.local.converter.DateTimeConverters
import com.madtracking.app.data.local.dao.IntakeDao
import com.madtracking.app.data.local.dao.MedicationDao
import com.madtracking.app.data.local.dao.ProfileDao
import com.madtracking.app.data.local.entity.IntakeEntity
import com.madtracking.app.data.local.entity.MedicationEntity
import com.madtracking.app.data.local.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        MedicationEntity::class,
        IntakeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeDao(): IntakeDao

    companion object {
        const val DATABASE_NAME = "med_track_database"
    }
}

