package com.madtracking.app.data.local.dao

import androidx.room.*
import com.madtracking.app.data.local.entity.IntakeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IntakeDao {
    @Insert
    suspend fun insert(intake: IntakeEntity): Long

    @Update
    suspend fun update(intake: IntakeEntity)

    @Query("SELECT * FROM intakes WHERE profileId = :profileId AND plannedTime LIKE :datePattern ORDER BY plannedTime ASC")
    fun getIntakesForDate(profileId: Long, datePattern: String): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE medicationId = :medicationId ORDER BY plannedTime DESC")
    fun getIntakesForMedication(medicationId: Long): Flow<List<IntakeEntity>>

    @Query("SELECT * FROM intakes WHERE id = :id")
    suspend fun getIntakeById(id: Long): IntakeEntity?

    @Query("UPDATE intakes SET status = :status, takenTime = :takenTime WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, takenTime: String?)

    @Query("DELETE FROM intakes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM intakes WHERE medicationId = :medicationId")
    suspend fun deleteForMedication(medicationId: Long)
}

