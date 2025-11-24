package com.madtracking.app.data.mapper

import com.madtracking.app.data.local.entity.ProfileEntity
import com.madtracking.app.domain.model.Profile

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

