package com.medtracking.app.data.local.dao

import androidx.room.*
import com.medtracking.app.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: ProfileEntity): Long

    @Query("SELECT * FROM profiles WHERE isActive = 1 AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id AND isDeleted = 0")
    fun getProfileById(id: Long): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileByIdOnce(id: Long): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getProfileByRemoteId(remoteId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE ownerUserId = :ownerUserId AND name = :name AND isDeleted = 0 ORDER BY createdAt ASC LIMIT 1")
    suspend fun getProfileByOwnerAndName(ownerUserId: String, name: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE isDirty = 1")
    suspend fun getDirtyProfiles(): List<ProfileEntity>
    
    @Query("DELETE FROM profiles WHERE id IN (SELECT id FROM profiles WHERE ownerUserId = :ownerUserId AND name = :name AND isDeleted = 0 ORDER BY createdAt ASC LIMIT -1 OFFSET 1)")
    suspend fun deleteDuplicateProfiles(ownerUserId: String, name: String): Int

    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}

