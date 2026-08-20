package com.example.data.repository

import android.content.Context
import com.example.data.crypto.CryptoManager
import com.example.data.db.ArticleDao
import com.example.data.model.Article
import com.example.data.model.ArticleEntity
import com.example.data.model.LinkComment
import com.example.data.sync.CloudSyncManager
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
    val cloudSyncManager = CloudSyncManager(context)

    init {
        cloudSyncManager.registerSyncCallbacks(
            fetchLocalArticles = {
                articleDao.getAllEntitiesDirect()
            },
            applySyncedArticles = { entities ->
                articleDao.deleteAllArticles()
                articleDao.insertAll(entities)
            }
        )
    }

    val allArticles: Flow<List<Article>> = articleDao.getAllArticles().map { list ->
        list.map { it.toDomain() }
    }

    val favoriteArticles: Flow<List<Article>> = articleDao.getFavoriteArticles().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAllEntitiesDirect(): List<ArticleEntity> = withContext(Dispatchers.IO) {
        articleDao.getAllEntitiesDirect()
    }

    suspend fun replaceAllEntities(entities: List<ArticleEntity>) = withContext(Dispatchers.IO) {
        articleDao.deleteAllArticles()
        articleDao.insertAll(entities)
        cloudSyncManager.notifyDataChanged()
    }

    suspend fun getArticleById(id: Long): Article? = withContext(Dispatchers.IO) {
        articleDao.getArticleById(id)?.toDomain()
    }

    suspend fun getArticlesByIds(ids: List<Long>): List<Article> = withContext(Dispatchers.IO) {
        if (ids.isEmpty()) emptyList()
        else articleDao.getArticlesByIds(ids).map { it.toDomain() }
    }

    suspend fun saveArticle(
        url: String,
        title: String,
        thumbnailUrl: String = "",
        summary: String = "",
        notes: String = "",
        hashtags: List<String> = emptyList(),
        linkedPostIds: List<Long> = emptyList(),
        category: String = "General"
    ): Long = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        val cleanTitle = if (title.isNotBlank()) title.trim() else "Saved Link ($domain)"
        val readingTime = maxOf(1, ((summary.length + notes.length) / 200))
        val resolvedThumbnail = if (thumbnailUrl.isNotBlank()) thumbnailUrl.trim() else generateDefaultThumbnail(domain, url)

        val entity = ArticleEntity(
            url = url.trim(),
            title = cleanTitle,
            sourceDomain = domain,
            thumbnailUrl = resolvedThumbnail,
            category = category,
            hashtagsJson = hashtags.joinToString(","),
            contentEncrypted = CryptoManager.encrypt(notes),
            summaryEncrypted = CryptoManager.encrypt(summary),
            commentsJson = "[]",
            linkedPostIdsJson = ArticleEntity.fromDomain(
                Article(url = url, title = cleanTitle, sourceDomain = domain, linkedPostIds = linkedPostIds)
            ).linkedPostIdsJson,
            readingTimeMinutes = readingTime,
            isFavorite = false,
            createdTimestamp = System.currentTimeMillis()
        )

        val id = articleDao.insertArticle(entity)
        cloudSyncManager.notifyDataChanged()
        id
    }

    suspend fun updateArticle(article: Article) = withContext(Dispatchers.IO) {
        articleDao.updateArticle(ArticleEntity.fromDomain(article))
        cloudSyncManager.notifyDataChanged()
    }

    suspend fun updateLinkDetails(
        id: Long,
        title: String,
        url: String = "",
        thumbnailUrl: String = "",
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedPostIds: List<Long> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(id) ?: return@withContext
        val finalUrl = if (url.isNotBlank()) url.trim() else current.url
        val domain = extractDomain(finalUrl)

        val updated = current.copy(
            title = title.trim(),
            url = finalUrl,
            sourceDomain = domain,
            thumbnailUrl = thumbnailUrl.trim(),
            summaryEncrypted = CryptoManager.encrypt(summary.trim()),
            contentEncrypted = CryptoManager.encrypt(notes.trim()),
            hashtagsJson = hashtags.joinToString(","),
            linkedPostIdsJson = ArticleEntity.fromDomain(
                current.toDomain().copy(linkedPostIds = linkedPostIds)
            ).linkedPostIdsJson
        )
        articleDao.updateArticle(updated)
        cloudSyncManager.notifyDataChanged()
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
        cloudSyncManager.notifyDataChanged()
        updatedArticle
    }

    suspend fun deleteComment(articleId: Long, commentId: String): Article? = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(articleId)?.toDomain() ?: return@withContext null
        val updatedComments = current.comments.filterNot { it.id == commentId }
        val updatedArticle = current.copy(comments = updatedComments)
        articleDao.updateArticle(ArticleEntity.fromDomain(updatedArticle))
        cloudSyncManager.notifyDataChanged()
        updatedArticle
    }

    suspend fun updateCommentText(articleId: Long, commentId: String, newText: String): Article? = withContext(Dispatchers.IO) {
        val current = articleDao.getArticleById(articleId)?.toDomain() ?: return@withContext null
        val updatedComments = current.comments.map { comment ->
            if (comment.id == commentId) comment.copy(text = newText.trim()) else comment
        }
        val updatedArticle = current.copy(comments = updatedComments)
        articleDao.updateArticle(ArticleEntity.fromDomain(updatedArticle))
        cloudSyncManager.notifyDataChanged()
        updatedArticle
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) = withContext(Dispatchers.IO) {
        articleDao.setFavorite(id, !current)
        cloudSyncManager.notifyDataChanged()
    }

    suspend fun deleteArticle(id: Long) = withContext(Dispatchers.IO) {
        articleDao.deleteArticleById(id)
        cloudSyncManager.notifyDataChanged()
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
            val thumbnail = generateDefaultThumbnail(domain, url)

            saveArticle(
                url = url,
                title = title,
                thumbnailUrl = thumbnail,
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
                thumbnailUrl = "",
                summary = sharedText.take(120),
                notes = sharedText,
                hashtags = listOf("#note", "#chrome"),
                category = "Note"
            )
        }
    }

    private fun generateDefaultThumbnail(domain: String, url: String): String {
        return if (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".webp")) {
            url
        } else if (domain.isNotBlank() && !domain.contains("share")) {
            // High quality website favicon / preview provider
            "https://www.google.com/s2/favicons?domain=$domain&sz=256"
        } else {
            ""
        }
    }

    suspend fun seedInitialArticlesIfEmpty() = withContext(Dispatchers.IO) {
        if (articleDao.getArticlesCount() > 0) return@withContext

        val sampleSeeds = listOf(
            ArticleEntity(
                url = "https://www.google.com/chrome/",
                title = "Google Chrome - Fast & Secure Web Browser",
                sourceDomain = "google.com",
                thumbnailUrl = "https://www.google.com/chrome/static/images/chrome-logo.svg",
                category = "Chrome",
                hashtagsJson = "#chrome,#browser,#google,#web",
                summaryEncrypted = CryptoManager.encrypt("Google Chrome is a fast, simple, and secure web browser built for modern devices with tab sync and privacy controls."),
                contentEncrypted = CryptoManager.encrypt("Personal note: Useful link for [Chrome Extensions](https://developer.chrome.com) and cross-device sync features. [Try it now]{font=serif;size=lg;color=#2563EB;bg=#FEF08A;url=https://www.google.com/chrome/}"),
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
                linkedPostIdsJson = "[2]",
                readingTimeMinutes = 2,
                isFavorite = true
            ),
            ArticleEntity(
                url = "https://en.wikipedia.org/wiki/Web_browser",
                title = "Web browser - Wikipedia",
                sourceDomain = "wikipedia.org",
                thumbnailUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/8/80/Wikipedia-logo-v2.svg/300px-Wikipedia-logo-v2.svg.png",
                category = "Reference",
                hashtagsJson = "#wikipedia,#reference,#history,#internet",
                summaryEncrypted = CryptoManager.encrypt("Comprehensive overview of the history, architecture, and standards of modern web browsers from Mosaic to present day."),
                contentEncrypted = CryptoManager.encrypt("Great reference article for browser engine comparisons: [Blink & Gecko]{font=mono;color=#059669;bg=#BBF7D0;url=https://en.wikipedia.org/wiki/Blink_(browser_engine)}."),
                commentsJson = """
                    [
                        {
                            "id": "c3",
                            "text": "Refer back to the W3C standards at https://www.w3.org/standards for web protocols.",
                            "timestamp": ${System.currentTimeMillis() - 1000 * 3600 * 5}
                        }
                    ]
                """.trimIndent(),
                linkedPostIdsJson = "[1]",
                readingTimeMinutes = 3,
                isFavorite = false
            )
        )

        articleDao.insertAll(sampleSeeds)
        cloudSyncManager.notifyDataChanged()
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

