package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.Addendum
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return ""
        val jsonArray = JSONArray()
        for (item in value) {
            jsonArray.put(item)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (_: Exception) {
            // fallback if comma-separated
            return value.split(",").map { it.trim() }.filter { it.isNotBlank() }
        }
        return list
    }

    @TypeConverter
    fun fromAddendumList(value: List<Addendum>?): String {
        if (value == null || value.isEmpty()) return ""
        val jsonArray = JSONArray()
        for (addendum in value) {
            val obj = JSONObject()
            obj.put("id", addendum.id)
            obj.put("content", addendum.content)
            obj.put("timestamp", addendum.timestamp)
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toAddendumList(value: String?): List<Addendum> {
        if (value.isNullOrBlank()) return emptyList()
        val list = mutableListOf<Addendum>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optString("id", java.util.UUID.randomUUID().toString())
                val content = obj.optString("content", "")
                val timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                if (content.isNotBlank()) {
                    list.add(Addendum(id = id, content = content, timestamp = timestamp))
                }
            }
        } catch (_: Exception) {
            return emptyList()
        }
        return list
    }
}
