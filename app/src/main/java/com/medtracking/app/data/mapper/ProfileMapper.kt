package com.medtracking.app.data.mapper

import com.medtracking.app.data.local.entity.ProfileEntity
import com.medtracking.app.domain.model.MemberRole
import com.medtracking.app.domain.model.Profile

/**
 * Maps ProfileEntity to domain Profile.
 * @param currentUserId Current authenticated user ID to compute myRole
 */
fun ProfileEntity.toDomain(currentUserId: String? = null): Profile {
    val membersMap = membersJson?.mapValues { (_, roleName) ->
        MemberRole.fromString(roleName)
    } ?: emptyMap()
    
    // Compute user's role
    val myRole = if (currentUserId != null) {
        // First check members map
        membersMap[currentUserId]
            // If not in members map but is the owner, grant OWNER role
            ?: if (currentUserId == ownerUserId) MemberRole.OWNER else null
    } else {
        null
    }
    
    return Profile(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        relation = relation,
        isActive = isActive,
        createdAt = createdAt,
        remoteId = remoteId,
        ownerUserId = ownerUserId,
        isShared = isShared,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted,
        members = membersMap,
        myRole = myRole
    )
}

fun Profile.toEntity(): ProfileEntity {
    val membersJson = if (members.isNotEmpty()) {
        members.mapValues { (_, role) -> role.name }
    } else {
        null
    }
    
    return ProfileEntity(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        relation = relation,
        isActive = isActive,
        createdAt = createdAt,
        remoteId = remoteId,
        ownerUserId = ownerUserId,
        isShared = isShared,
        updatedAt = updatedAt,
        isDirty = isDirty,
        isDeleted = isDeleted,
        membersJson = membersJson
    )
}

