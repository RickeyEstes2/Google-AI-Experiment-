package com.example.data.db

import androidx.room.*
import com.example.data.model.Article
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles ORDER BY updatedAt DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: Long): Article?

    @Query("SELECT * FROM articles WHERE id IN (:ids)")
    suspend fun getArticlesByIds(ids: List<Long>): List<Article>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticleById(id: Long)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getArticleCount(): Int
}
