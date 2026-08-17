package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE isArchived = 0 ORDER BY createdTimestamp DESC")
    fun getAllArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isArchived = 1 ORDER BY createdTimestamp DESC")
    fun getArchivedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 AND isArchived = 0 ORDER BY createdTimestamp DESC")
    fun getFavoriteArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE category = :category AND isArchived = 0 ORDER BY createdTimestamp DESC")
    fun getArticlesByCategory(category: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Long): ArticleEntity?

    @Query("SELECT DISTINCT category FROM articles WHERE isArchived = 0")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<ArticleEntity>)

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticleById(id: Long)

    @Query("UPDATE articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE articles SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun getArticlesCount(): Int
}
