package com.madtracking.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarEmoji: String?,
    val relation: String?,
    val isActive: Boolean,
    val createdAt: Long
)

