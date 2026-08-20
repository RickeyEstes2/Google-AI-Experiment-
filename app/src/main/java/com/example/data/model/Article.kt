package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.crypto.CryptoManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Data class for hyperlinkable comments on a shared link.
 */
data class LinkComment(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Room entity representing a saved link or shared web page from Chrome.
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String = "",
    val category: String = "General",
    val hashtagsJson: String = "",
    val contentEncrypted: String = "", // Notes
    val summaryEncrypted: String = "", // Summary
    val commentsJson: String = "", // Comments JSON array
    val readingTimeMinutes: Int = 1,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val equationsJsonEncrypted: String = "",
    val nlpJsonEncrypted: String = ""
) {
    fun toDomain(): Article {
        return Article(
            id = id,
            url = url,
            title = title,
            sourceDomain = sourceDomain,
            thumbnailUrl = thumbnailUrl,
            category = category,
            hashtags = parseHashtags(hashtagsJson),
            notes = CryptoManager.decrypt(contentEncrypted),
            summary = CryptoManager.decrypt(summaryEncrypted),
            comments = parseComments(commentsJson),
            readingTimeMinutes = readingTimeMinutes,
            isFavorite = isFavorite,
            isArchived = isArchived,
            createdTimestamp = createdTimestamp
        )
    }

    companion object {
        fun fromDomain(article: Article): ArticleEntity {
            return ArticleEntity(
                id = article.id,
                url = article.url,
                title = article.title,
                sourceDomain = article.sourceDomain,
                thumbnailUrl = article.thumbnailUrl,
                category = article.category,
                hashtagsJson = article.hashtags.joinToString(","),
                contentEncrypted = CryptoManager.encrypt(article.notes),
                summaryEncrypted = CryptoManager.encrypt(article.summary),
                commentsJson = serializeComments(article.comments),
                readingTimeMinutes = article.readingTimeMinutes,
                isFavorite = article.isFavorite,
                isArchived = article.isArchived,
                createdTimestamp = article.createdTimestamp
            )
        }

        private fun parseHashtags(raw: String): List<String> {
            if (raw.isBlank()) return emptyList()
            return raw.split(",")
                .map { it.trim().removePrefix("#") }
                .filter { it.isNotBlank() }
                .map { "#$it" }
        }

        private fun parseComments(raw: String): List<LinkComment> {
            if (raw.isBlank()) return emptyList()
            val list = mutableListOf<LinkComment>()
            try {
                val array = JSONArray(raw)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        LinkComment(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            text = obj.optString("text", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                // Return empty if parsing failed
            }
            return list
        }

        private fun serializeComments(comments: List<LinkComment>): String {
            val array = JSONArray()
            comments.forEach { c ->
                val obj = JSONObject()
                obj.put("id", c.id)
                obj.put("text", c.text)
                obj.put("timestamp", c.timestamp)
                array.put(obj)
            }
            return array.toString()
        }
    }
}

/**
 * Clean domain representation of a saved web link for UI.
 */
data class Article(
    val id: Long = 0,
    val url: String,
    val title: String,
    val sourceDomain: String,
    val thumbnailUrl: String = "",
    val category: String = "General",
    val hashtags: List<String> = emptyList(),
    val notes: String = "",
    val summary: String = "",
    val comments: List<LinkComment> = emptyList(),
    val readingTimeMinutes: Int = 1,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
