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
    version = 8,
    exportSchema = false
)
@TypeConverters(DateTimeConverters::class, com.medtracking.app.data.local.converter.MembersConverter::class)
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

        // Migration from version 6 to 7: add cloud sync columns
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE profiles ADD COLUMN ownerUserId TEXT")
                db.execSQL("ALTER TABLE profiles ADD COLUMN isShared INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE profiles ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE profiles ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE profiles ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE medications ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE medications ADD COLUMN profileRemoteId TEXT")
                db.execSQL("ALTER TABLE medications ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medications ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE medications ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE intakes ADD COLUMN remoteId TEXT")
                db.execSQL("ALTER TABLE intakes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intakes ADD COLUMN isDirty INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intakes ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Migration from version 7 to 8: add membersJson for RBAC
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE profiles ADD COLUMN membersJson TEXT")
            }
        }

        fun getMigrations(): Array<Migration> = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )
    }
}
