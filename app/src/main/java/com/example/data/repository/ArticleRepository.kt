package com.example.data.repository

import android.content.Context
import com.example.data.crypto.CryptoManager
import com.example.data.db.ArticleDao
import com.example.data.model.Article
import com.example.data.model.ArticleEntity
import com.example.data.model.LinkComment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val context: Context
) {
    val allArticles: Flow<List<Article>> = articleDao.getAllArticles().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteArticles: Flow<List<Article>> = articleDao.getFavoriteArticles().map { list ->
        list.map { it.toDomain() }
    }

    val archivedArticles: Flow<List<Article>> = articleDao.getArchivedArticles().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getArticleById(id: Long): Article? = withContext(Dispatchers.IO) {
        articleDao.getArticleById(id)?.toDomain()
    }

    suspend fun saveArticle(
        url: String,
        title: String,
        summary: String = "",
        notes: String = "",
        hashtags: List<String> = emptyList(),
        category: String = "General"
    ): Long = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        val cleanTitle = if (title.isNotBlank()) title.trim() else "Saved Link ($domain)"
        val readingTime = maxOf(1, ((summary.length + notes.length) / 200))

        val entity = ArticleEntity(
            url = url.trim(),
            title = cleanTitle,
            sourceDomain = domain,
            thumbnailUrl = "",
            category = category,
            hashtagsJson = hashtags.joinToString(","),
            contentEncrypted = CryptoManager.encrypt(notes),
            summaryEncrypted = CryptoManager.encrypt(summary),
            commentsJson = "[]",
            readingTimeMinutes = readingTime,
            isFavorite = false,
            isArchived = false,
            createdTimestamp = System.currentTimeMillis()
        )

        articleDao.insertArticle(entity)
    }

    suspend fun updateArticle(article: Article) = withContext(Dispatchers.IO) {
        articleDao.updateArticle(ArticleEntity.fromDomain(article))
    }

    suspend fun updateLinkDetails(
        id: Long,
        title: String,
        summary: String,
        notes: String,
        hashtags: List<String>
    ) = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(id) ?: return@withContext
        val updated = current.copy(
            title = title.trim(),
            summaryEncrypted = CryptoManager.encrypt(summary.trim()),
            contentEncrypted = CryptoManager.encrypt(notes.trim()),
            hashtagsJson = hashtags.joinToString(",")
        )
        articleDao.updateArticle(updated)
    }

    suspend fun addComment(articleId: Long, commentText: String): Article? = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(articleId)?.toDomain() ?: return@withContext null
        val newComment = LinkComment(
            id = UUID.randomUUID().toString(),
            text = commentText.trim(),
            timestamp = System.currentTimeMillis()
        )
        val updatedComments = current.comments + newComment
        val updatedArticle = current.copy(comments = updatedComments)
        articleDao.updateArticle(ArticleEntity.fromDomain(updatedArticle))
        updatedArticle
    }

    suspend fun deleteComment(articleId: Long, commentId: String): Article? = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(articleId)?.toDomain() ?: return@withContext null
        val updatedComments = current.comments.filterNot { it.id == commentId }
        val updatedArticle = current.copy(comments = updatedComments)
        articleDao.updateArticle(ArticleEntity.fromDomain(updatedArticle))
        updatedArticle
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = withContext(Dispatchers.IO) {
        articleDao.setFavorite(id, !current)
    }

    suspend fun toggleArchive(id: Long, current: Boolean) = withContext(Dispatchers.IO) {
        articleDao.setArchived(id, !current)
    }

    suspend fun deleteArticle(id: Long) = withContext(Dispatchers.IO) {
        articleDao.deleteArticleById(id)
    }

    /**
     * Ingests a shared URL or text directly from Google Chrome Share Sheet.
     */
    suspend fun ingestSharedContent(sharedText: String): Long = withContext(Dispatchers.IO) {
        val urlRegex = Regex("https?://[^\\s]+")
        val matchedUrl = urlRegex.find(sharedText)?.value

        if (matchedUrl != null) {
            val url = matchedUrl.trim()
            val domain = extractDomain(url)
            val titleCandidate = sharedText.replace(url, "").trim().trim('"', '\'', '-', '|', ':')
            val title = if (titleCandidate.isNotBlank()) titleCandidate else "Shared from $domain"
            
            // Auto generate initial hashtags based on domain and words
            val autoTags = mutableListOf<String>()
            val domainTag = domain.substringBefore(".").take(15)
            if (domainTag.isNotBlank()) {
                autoTags.add("#$domainTag")
            }
            autoTags.add("#chrome")
            autoTags.add("#reading")

            val defaultSummary = "Shared webpage captured directly from Google Chrome ($domain)."
            val defaultNotes = "Saved on ${SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())}"

            saveArticle(
                url = url,
                title = title,
                summary = defaultSummary,
                notes = defaultNotes,
                hashtags = autoTags,
                category = "Chrome"
            )
        } else {
            // Shared text note
            val title = sharedText.take(50).lines().firstOrNull()?.trim() ?: "Shared Chrome Note"
            saveArticle(
                url = "https://chrome.share/${System.currentTimeMillis()}",
                title = title,
                summary = sharedText.take(120),
                notes = sharedText,
                hashtags = listOf("#note", "#chrome"),
                category = "Note"
            )
        }
    }

    suspend fun seedInitialArticlesIfEmpty() = withContext(Dispatchers.IO) {
        if (articleDao.getArticlesCount() > 0) return@withContext

        val sampleSeeds = listOf(
            ArticleEntity(
                url = "https://www.google.com/chrome/",
                title = "Google Chrome - Fast & Secure Web Browser",
                sourceDomain = "google.com",
                category = "Chrome",
                hashtagsJson = "#chrome,#browser,#google,#web",
                summaryEncrypted = CryptoManager.encrypt("Google Chrome is a fast, simple, and secure web browser built for modern devices with tab sync and privacy controls."),
                contentEncrypted = CryptoManager.encrypt("Personal note: Useful link for Chrome extensions and cross-device sync features."),
                commentsJson = """
                    [
                        {
                            "id": "c1",
                            "text": "Check out the official documentation at https://developer.chrome.com for extension APIs!",
                            "timestamp": ${System.currentTimeMillis() - 1000 * 3600 * 24}
                        },
                        {
                            "id": "c2",
                            "text": "Also see https://chromium.org for open source architecture updates #chromium #tech",
                            "timestamp": ${System.currentTimeMillis() - 1000 * 3600 * 2}
                        }
                    ]
                """.trimIndent(),
                readingTimeMinutes = 2,
                isFavorite = true,
                isArchived = false
            ),
            ArticleEntity(
                url = "https://en.wikipedia.org/wiki/Web_browser",
                title = "Web browser - Wikipedia",
                sourceDomain = "wikipedia.org",
                category = "Reference",
                hashtagsJson = "#wikipedia,#reference,#history,#internet",
                summaryEncrypted = CryptoManager.encrypt("Comprehensive overview of the history, architecture, and standards of modern web browsers from Mosaic to present day."),
                contentEncrypted = CryptoManager.encrypt("Great reference article for browser engine comparisons (Blink, Gecko, WebKit)."),
                commentsJson = """
                    [
                        {
                            "id": "c3",
                            "text": "Refer back to the W3C standards at https://www.w3.org/standards for web protocols.",
                            "timestamp": ${System.currentTimeMillis() - 1000 * 3600 * 5}
                        }
                    ]
                """.trimIndent(),
                readingTimeMinutes = 3,
                isFavorite = false,
                isArchived = false
            )
        )

        articleDao.insertAll(sampleSeeds)
    }

    private fun extractDomain(urlStr: String): String {
        return try {
            val url = URL(urlStr)
            url.host.removePrefix("www.")
        } catch (e: Exception) {
            if (urlStr.startsWith("http")) {
                urlStr.substringAfter("://").substringBefore("/").removePrefix("www.")
            } else {
                "link"
            }
        }
    }
}
