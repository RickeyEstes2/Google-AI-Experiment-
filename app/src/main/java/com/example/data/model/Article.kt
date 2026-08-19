package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.crypto.CryptoManager

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
    val contentEncrypted: String = "",
    val summaryEncrypted: String = "",
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
            content = CryptoManager.decrypt(contentEncrypted),
            summary = CryptoManager.decrypt(summaryEncrypted),
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
                contentEncrypted = CryptoManager.encrypt(article.content),
                summaryEncrypted = CryptoManager.encrypt(article.summary),
                readingTimeMinutes = article.readingTimeMinutes,
                isFavorite = article.isFavorite,
                isArchived = article.isArchived,
                createdTimestamp = article.createdTimestamp
            )
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
    val content: String = "",
    val summary: String = "",
    val readingTimeMinutes: Int = 1,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdTimestamp: Long = System.currentTimeMillis()
)
