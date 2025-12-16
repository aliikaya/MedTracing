package com.medtracking.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.medtracking.app.data.local.converter.MembersConverter

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String?,
    val relation: String?,
    val isActive: Boolean,
    val createdAt: Long,
    val remoteId: String? = null,
    val ownerUserId: String? = null,
    val isShared: Boolean = false,
    val updatedAt: Long = createdAt,
    val isDirty: Boolean = false,
    val isDeleted: Boolean = false,
    // Members map stored as JSON: {"uid1": "OWNER", "uid2": "PATIENT_MARK_ONLY"}
    @TypeConverters(MembersConverter::class)
    val membersJson: Map<String, String>? = null
)

