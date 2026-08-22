package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.Addendum

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.joinToString(";;;") ?: ""
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;;").filter { it.isNotBlank() }
    }

    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toLongList(value: String?): List<Long> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }

    @TypeConverter
    fun fromAddendumList(value: List<Addendum>?): String {
        if (value.isNullOrEmpty()) return ""
        return value.joinToString("<AD_REC>") { addendum ->
            "${addendum.id}<AD_FLD>${addendum.timestamp}<AD_FLD>${encodeContent(addendum.content)}"
        }
    }

    @TypeConverter
    fun toAddendumList(value: String?): List<Addendum> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split("<AD_REC>").mapNotNull { record ->
            val parts = record.split("<AD_FLD>")
            if (parts.size >= 3) {
                Addendum(
                    id = parts[0],
                    timestamp = parts[1].toLongOrNull() ?: System.currentTimeMillis(),
                    content = decodeContent(parts.subList(2, parts.size).joinToString("<AD_FLD>"))
                )
            } else null
        }
    }

    private fun encodeContent(content: String): String {
        return android.util.Base64.encodeToString(content.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
    }

    private fun decodeContent(encoded: String): String {
        return try {
            String(android.util.Base64.decode(encoded, android.util.Base64.NO_WRAP), Charsets.UTF_8)
        } catch (_: Exception) {
            encoded
        }
    }
}
