package com.medtracking.app.data.mapper

import com.medtracking.app.data.local.entity.ProfileEntity
import com.medtracking.app.domain.model.Profile

fun ProfileEntity.toDomain(): Profile {
    return Profile(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        relation = relation,
        isActive = isActive,
        createdAt = createdAt
    )
}

fun Profile.toEntity(): ProfileEntity {
    return ProfileEntity(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        relation = relation,
        isActive = isActive,
        createdAt = createdAt
    )
}

