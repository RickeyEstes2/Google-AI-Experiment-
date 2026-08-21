package com.example.data.repository

import com.example.data.db.ArticleDao
import com.example.data.model.Article
import com.example.util.LinkMetadataFetcher
import kotlinx.coroutines.flow.Flow
import java.net.URI

class ArticleRepository(private val articleDao: ArticleDao) {

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles()

    suspend fun getArticleById(id: Long): Article? = articleDao.getArticleById(id)

    suspend fun getArticlesByIds(ids: List<Long>): List<Article> = articleDao.getArticlesByIds(ids)

    suspend fun insertArticle(
        url: String,
        title: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedIds: List<Long> = emptyList()
    ): Long {
        val domain = extractDomain(url)
        
        // Auto-fetch image and metadata if thumbnail is blank and URL is provided
        var resolvedThumbnail = thumbnailUrl.trim()
        var resolvedTitle = title.trim()
        var resolvedSummary = summary.trim()

        if (resolvedThumbnail.isBlank() && url.isNotBlank()) {
            try {
                val meta = LinkMetadataFetcher.fetchMetadata(url)
                if (!meta.imageUrl.isNullOrBlank()) {
                    resolvedThumbnail = meta.imageUrl
                }
                if (resolvedTitle.isBlank() && !meta.title.isNullOrBlank()) {
                    resolvedTitle = meta.title
                }
                if (resolvedSummary.isBlank() && !meta.description.isNullOrBlank()) {
                    resolvedSummary = meta.description
                }
            } catch (_: Exception) {}
        }

        val article = Article(
            url = url.trim(),
            title = resolvedTitle.ifBlank { "Untitled Note" }.trim(),
            domain = domain,
            thumbnailUrl = resolvedThumbnail,
            summary = resolvedSummary,
            notes = notes.trim(),
            hashtags = hashtags.map { it.trim() }.filter { it.isNotBlank() },
            linkedArticleIds = linkedIds,
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return articleDao.insertArticle(article)
    }

    suspend fun updateArticle(article: Article) {
        var finalThumb = article.thumbnailUrl.trim()
        if (finalThumb.isBlank() && article.url.isNotBlank()) {
            try {
                val meta = LinkMetadataFetcher.fetchMetadata(article.url)
                if (!meta.imageUrl.isNullOrBlank()) {
                    finalThumb = meta.imageUrl
                }
            } catch (_: Exception) {}
        }

        val updated = article.copy(
            domain = extractDomain(article.url),
            thumbnailUrl = finalThumb,
            updatedAt = System.currentTimeMillis()
        )
        articleDao.updateArticle(updated)
    }

    suspend fun toggleFavorite(article: Article) {
        val updated = article.copy(
            isFavorite = !article.isFavorite,
            updatedAt = System.currentTimeMillis()
        )
        articleDao.updateArticle(updated)
    }

    suspend fun updateHashtags(articleId: Long, hashtags: List<String>) {
        val existing = articleDao.getArticleById(articleId) ?: return
        val updated = existing.copy(
            hashtags = hashtags.map { it.trim() }.filter { it.isNotBlank() },
            updatedAt = System.currentTimeMillis()
        )
        articleDao.updateArticle(updated)
    }

    suspend fun deleteArticle(article: Article) {
        articleDao.deleteArticle(article)
    }

    suspend fun seedSampleDataIfEmpty() {
        val count = articleDao.getArticleCount()
        if (count == 0) {
            insertArticle(
                url = "https://material.io/design",
                title = "Material Design 3 Principles & Ergonomics",
                thumbnailUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=600&auto=format&fit=crop&q=80",
                summary = "Design system guidelines for accessible, colorful, and responsive interfaces.",
                notes = """
                    ### Color & Typography Roles
                    1. **Primary / Secondary**: Brand accentuation and structural grouping.
                    2. **Surface & Background**: High contrast readability and neutral elevation.
                    3. **Touch Targets**: Minimum 48dp interactive boundaries.
                """.trimIndent(),
                hashtags = listOf("#Design", "#Ideas", "#Work")
            )

            insertArticle(
                url = "https://developer.android.com/jetpack/compose",
                title = "Jetpack Compose Modern UI Guide",
                thumbnailUrl = "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=600&auto=format&fit=crop&q=80",
                summary = "Jetpack Compose is Android's recommended modern toolkit for building native UI.",
                notes = """
                    ## Key Advantages of Compose
                    - **Declarative**: Describe what your UI should look like for given states.
                    - **Less Code**: Accelerate development with reusable components.
                    - **Kotlin-First**: Native language features and coroutines support.
                    
                    ```kotlin
                    @Composable
                    fun Greeting(name: String) {
                        Text(text = "Hello " + name)
                    }
                    ```
                """.trimIndent(),
                hashtags = listOf("#Tech", "#AI", "#Tutorial")
            )

            insertArticle(
                url = "https://arxiv.org/abs/2303.08774",
                title = "Advanced LLM Architectures & Mathematical Foundations",
                thumbnailUrl = "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?w=600&auto=format&fit=crop&q=80",
                summary = "Theoretical foundations of attention mechanisms and transformer scaling laws.",
                notes = """
                    ## Attention Equation
                    $$\text{Attention}(Q, K, V) = \text{softmax}\left(\frac{QK^T}{\sqrt{d_k}}\right)V$$
                    
                    ## Multi-Head Formulation
                    $$\text{MultiHead}(Q, K, V) = \text{Concat}(\text{head}_1, \dots, \text{head}_h)W^O$$
                """.trimIndent(),
                hashtags = listOf("#Research", "#AI", "#Finance")
            )
        }
    }

    private fun extractDomain(rawUrl: String): String {
        return try {
            val uri = URI(rawUrl)
            val host = uri.host ?: ""
            host.removePrefix("www.")
        } catch (_: Exception) {
            "local.note"
        }
    }
}
