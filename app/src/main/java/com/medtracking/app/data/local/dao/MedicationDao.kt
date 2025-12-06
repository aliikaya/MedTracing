package com.medtracking.app.data.local.dao

import androidx.room.*
import com.medtracking.app.data.local.entity.MedicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert
    suspend fun insert(medication: MedicationEntity): Long

    @Update
    suspend fun update(medication: MedicationEntity)

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND isActive = 1 ORDER BY createdAt DESC")
    fun getMedicationsForProfile(profileId: Long): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications WHERE profileId = :profileId AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getMedicationsForProfileOnce(profileId: Long): List<MedicationEntity>

    @Query("""
        SELECT * FROM medications 
        WHERE profileId = :profileId 
        AND isActive = 1 
        AND startDate <= :date 
        AND (endDate IS NULL OR endDate >= :date)
        ORDER BY createdAt DESC
    """)
    suspend fun getActiveMedicationsForDate(profileId: Long, date: String): List<MedicationEntity>

    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Long): Flow<MedicationEntity?>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationByIdOnce(id: Long): MedicationEntity?

    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveMedications(): Flow<List<MedicationEntity>>

    @Delete
    suspend fun delete(medication: MedicationEntity)

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteById(id: Long)
}

