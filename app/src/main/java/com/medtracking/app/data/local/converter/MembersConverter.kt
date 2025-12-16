package com.medtracking.app.data.local.converter

import androidx.room.TypeConverter
import org.json.JSONObject

/**
 * TypeConverter for storing members map (uid -> role) as JSON string in Room.
 */
class MembersConverter {

    @TypeConverter
    fun fromMembersMap(members: Map<String, String>?): String? {
        if (members.isNullOrEmpty()) return null
        val json = JSONObject()
        members.forEach { (uid, role) ->
            json.put(uid, role)
        }
        return json.toString()
    }

    @TypeConverter
    fun toMembersMap(json: String?): Map<String, String>? {
        if (json.isNullOrBlank()) return null
        return try {
            val jsonObject = JSONObject(json)
            val map = mutableMapOf<String, String>()
            jsonObject.keys().forEach { key ->
                map[key] = jsonObject.getString(key)
            }
            map
        } catch (e: Exception) {
            null
        }
    }
}

