package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Addendum entry representing timestamped chronological notes or updates appended to an Article.
 */
data class Addendum(
    val id: String = UUID.randomUUID().toString(),
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Core entity representing a saved link, article, video, or knowledge note in Database Mastermind.
 * Completely local and offline-first.
 */
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String = "",
    val title: String = "",
    val domain: String = "",
    val thumbnailUrl: String = "",
    val summary: String = "",
    val notes: String = "",
    val hashtags: List<String> = emptyList(),
    val linkedArticleIds: List<Long> = emptyList(),
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    // Video integration fields
    val videoUrl: String = "",
    val videoStartSeconds: Int = 0,
    val videoEndSeconds: Int = 0,
    val videoAutostart: Boolean = false,
    // Addendums history
    val addendums: List<Addendum> = emptyList()
)
