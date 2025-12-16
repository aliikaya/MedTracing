package com.medtracking.app.data.local.dao

import androidx.room.*
import com.medtracking.app.data.local.entity.IntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(intake: IntakeEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(intakes: List<IntakeEntity>): List<Long>

    @Update
    suspend fun update(intake: IntakeEntity)

    @Query("SELECT * FROM intakes WHERE profileId = :profileId AND plannedTime LIKE :datePattern AND isDeleted = 0 ORDER BY plannedTime ASC")
    fun getIntakesForDate(profileId: Long, datePattern: String): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE profileId = :profileId AND plannedTime LIKE :datePattern AND isDeleted = 0 ORDER BY plannedTime ASC")
    suspend fun getIntakesForDateOnce(profileId: Long, datePattern: String): List<IntakeEntity>

    @Query("SELECT * FROM intakes WHERE medicationId = :medicationId AND isDeleted = 0 ORDER BY plannedTime DESC")
    fun getIntakesForMedication(medicationId: Long): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE id = :id")
    suspend fun getIntakeById(id: Long): IntakeEntity?

    @Query("SELECT * FROM intakes WHERE medicationId = :medicationId AND plannedTime = :plannedTime LIMIT 1")
    suspend fun getIntakeByMedicationAndTime(medicationId: Long, plannedTime: String): IntakeEntity?

    @Query("UPDATE intakes SET status = :status, takenTime = :takenTime, updatedAt = :updatedAt, isDirty = 1 WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, takenTime: String?, updatedAt: Long)

    @Query("UPDATE intakes SET isDeleted = 1, isDirty = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDeleteById(id: Long, updatedAt: Long)

    @Query("UPDATE intakes SET isDeleted = 1, isDirty = 1, updatedAt = :updatedAt WHERE medicationId = :medicationId")
    suspend fun softDeleteForMedication(medicationId: Long, updatedAt: Long)

    @Query("""
        SELECT * FROM intakes 
        WHERE medicationId = :medicationId 
          AND plannedTime >= :fromDate 
          AND plannedTime < :toDate 
          AND isDeleted = 0
        ORDER BY plannedTime ASC
    """)
    fun getIntakesForMedicationAndDateRange(
        medicationId: Long,
        fromDate: String,
        toDate: String
    ): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getIntakeByRemoteId(remoteId: String): IntakeEntity?

    @Query("SELECT * FROM intakes WHERE isDirty = 1")
    suspend fun getDirtyIntakes(): List<IntakeEntity>
}
