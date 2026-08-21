package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core entity representing a saved link, article, or knowledge note in Database Mastermind.
 * Completely local and offline-first.
 */
@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val domain: String,
    val thumbnailUrl: String = "",
    val summary: String = "",
    val notes: String = "",
    val hashtags: List<String> = emptyList(),
    val linkedArticleIds: List<Long> = emptyList(),
    val isFavorite: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
