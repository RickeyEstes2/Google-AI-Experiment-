package com.example.data.repository

import android.content.Context
import com.example.data.crypto.CryptoManager
import com.example.data.db.ArticleDao
import com.example.data.model.Article
import com.example.data.model.ArticleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.net.URL

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

    val allCategories: Flow<List<String>> = articleDao.getAllCategories()

    suspend fun getArticleById(id: Long): Article? = withContext(Dispatchers.IO) {
        articleDao.getArticleById(id)?.toDomain()
    }

    suspend fun saveArticle(
        url: String,
        title: String,
        content: String = "",
        category: String = "General"
    ): Long = withContext(Dispatchers.IO) {
        val domain = extractDomain(url)
        val cleanTitle = if (title.isNotBlank()) title.trim() else "Saved Link ($domain)"
        val readingTime = maxOf(1, (content.split("\\s+".toRegex()).size / 200))

        val entity = ArticleEntity(
            url = url.trim(),
            title = cleanTitle,
            sourceDomain = domain,
            thumbnailUrl = "",
            category = category,
            contentEncrypted = CryptoManager.encrypt(content),
            summaryEncrypted = CryptoManager.encrypt(content.take(200)),
            readingTimeMinutes = readingTime,
            isFavorite = false,
            isArchived = false,
            createdTimestamp = System.currentTimeMillis()
        )

        articleDao.insertArticle(entity)
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
            // The text shared from Chrome usually has the page title before the URL
            val titleCandidate = sharedText.replace(url, "").trim().trim('"', '\'', '-', '|', ':')
            val title = if (titleCandidate.isNotBlank()) titleCandidate else "Shared Link from $domain"

            saveArticle(
                url = url,
                title = title,
                content = "Shared from Google Chrome on ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}",
                category = "Chrome"
            )
        } else {
            // Shared text note
            val title = sharedText.take(50).lines().firstOrNull()?.trim() ?: "Shared Text"
            saveArticle(
                url = "https://chrome.share/${System.currentTimeMillis()}",
                title = title,
                content = sharedText,
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
                contentEncrypted = CryptoManager.encrypt("Browse fast on your Android phone and tablet with Google Chrome. Pick up where you left off on your other devices with tabs sync, search by voice, and easily read articles."),
                summaryEncrypted = CryptoManager.encrypt("Browse fast on your Android phone with Google Chrome."),
                readingTimeMinutes = 1,
                isFavorite = true,
                isArchived = false
            ),
            ArticleEntity(
                url = "https://en.wikipedia.org/wiki/Web_browser",
                title = "Web browser - Wikipedia",
                sourceDomain = "wikipedia.org",
                category = "Reference",
                contentEncrypted = CryptoManager.encrypt("A web browser is application software for accessing the World Wide Web or a local website. When a user requests a web page, the browser retrieves its files from a web server and renders them."),
                summaryEncrypted = CryptoManager.encrypt("Overview of web browsers and their functionality."),
                readingTimeMinutes = 2,
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
