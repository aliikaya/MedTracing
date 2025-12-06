package com.medtracking.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.medtracking.app.data.local.converter.DateTimeConverters
import com.medtracking.app.data.local.dao.IntakeDao
import com.medtracking.app.data.local.dao.MedicationDao
import com.medtracking.app.data.local.dao.ProfileDao
import com.medtracking.app.data.local.entity.IntakeEntity
import com.medtracking.app.data.local.entity.MedicationEntity
import com.medtracking.app.data.local.entity.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        MedicationEntity::class,
        IntakeEntity::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun medicationDao(): MedicationDao
    abstract fun intakeDao(): IntakeDao

    companion object {
        const val DATABASE_NAME = "med_track_database"

        // Migration from version 1 to 2 (if needed)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Placeholder for any v1 to v2 changes
            }
        }

        // Migration from version 2 to 3: Add durationInDays column
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN durationInDays INTEGER")
            }
        }

        // Migration from version 5 to 6: Add mealRelation column
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE medications ADD COLUMN mealRelation TEXT NOT NULL DEFAULT 'IRRELEVANT'")
            }
        }

        fun getMigrations(): Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_5_6
        )
    }
}
