package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

data class LinkMetadata(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val domain: String? = null
)

object LinkMetadataFetcher {

    private val OG_IMAGE_REGEX = Regex("""(?i)<meta\s+[^>]*?(?:property|name)=["'](?:og:image|twitter:image|twitter:image:src)["'][^>]*?content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val OG_IMAGE_REGEX_ALT = Regex("""(?i)<meta\s+[^>]*?content=["']([^"']+)["'][^>]*?(?:property|name)=["'](?:og:image|twitter:image|twitter:image:src)["']""", RegexOption.IGNORE_CASE)
    private val LINK_IMAGE_SRC_REGEX = Regex("""(?i)<link\s+[^>]*?rel=["'](?:image_src|apple-touch-icon|icon)["'][^>]*?href=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val OG_TITLE_REGEX = Regex("""(?i)<meta\s+[^>]*?(?:property|name)=["'](?:og:title|twitter:title)["'][^>]*?content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val OG_DESC_REGEX = Regex("""(?i)<meta\s+[^>]*?(?:property|name)=["'](?:og:description|description|twitter:description)["'][^>]*?content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
    private val TITLE_TAG_REGEX = Regex("""(?i)<title[^>]*>([^<]+)</title>""", RegexOption.IGNORE_CASE)

    suspend fun fetchMetadata(rawUrl: String): LinkMetadata = withContext(Dispatchers.IO) {
        if (rawUrl.isBlank()) return@withContext LinkMetadata()

        val normalizedUrl = if (!rawUrl.startsWith("http://") && !rawUrl.startsWith("https://")) {
            "https://$rawUrl"
        } else {
            rawUrl
        }

        val domain = try {
            val uri = URI(normalizedUrl)
            (uri.host ?: "").removePrefix("www.")
        } catch (_: Exception) {
            ""
        }

        // Fast path for YouTube
        val youtubeImage = extractYouTubeThumbnail(normalizedUrl)
        if (youtubeImage != null) {
            return@withContext LinkMetadata(
                imageUrl = youtubeImage,
                domain = domain.ifBlank { "youtube.com" }
            )
        }

        var connection: HttpURLConnection? = null
        try {
            val url = URL(normalizedUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/*,*/*;q=0.8")
            }

            val contentType = connection.contentType ?: ""
            // If the URL is directly an image, use it!
            if (contentType.startsWith("image/")) {
                return@withContext LinkMetadata(
                    imageUrl = normalizedUrl,
                    domain = domain
                )
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..399) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val sb = StringBuilder()
                var line: String?
                var readBytes = 0
                val maxBytes = 64 * 1024 // Read first 64KB (usually contains the full <head>)

                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                    readBytes += (line?.length ?: 0)
                    if (readBytes > maxBytes || (line?.contains("</head>", ignoreCase = true) == true)) {
                        break
                    }
                }
                reader.close()
                val html = sb.toString()

                // Extract image
                var imageUrl = OG_IMAGE_REGEX.find(html)?.groupValues?.get(1)
                    ?: OG_IMAGE_REGEX_ALT.find(html)?.groupValues?.get(1)
                    ?: LINK_IMAGE_SRC_REGEX.find(html)?.groupValues?.get(1)

                if (imageUrl != null) {
                    imageUrl = resolveUrl(normalizedUrl, imageUrl.trim())
                }

                // If still no image, use high-resolution domain favicon / logo preview
                if (imageUrl.isNullOrBlank() && domain.isNotBlank()) {
                    imageUrl = "https://www.google.com/s2/favicons?sz=256&domain_url=$normalizedUrl"
                }

                // Extract title
                val title = OG_TITLE_REGEX.find(html)?.groupValues?.get(1)
                    ?: TITLE_TAG_REGEX.find(html)?.groupValues?.get(1)

                // Extract description
                val description = OG_DESC_REGEX.find(html)?.groupValues?.get(1)

                return@withContext LinkMetadata(
                    title = title?.trim()?.unescapeHtml(),
                    description = description?.trim()?.unescapeHtml(),
                    imageUrl = imageUrl,
                    domain = domain
                )
            }
        } catch (_: Exception) {
            // Fallback for offline or unreachable hosts
        } finally {
            connection?.disconnect()
        }

        // Fallback: domain favicon if domain is known
        val fallbackImage = if (domain.isNotBlank()) "https://www.google.com/s2/favicons?sz=256&domain_url=$normalizedUrl" else null
        LinkMetadata(
            imageUrl = fallbackImage,
            domain = domain
        )
    }

    private fun extractYouTubeThumbnail(url: String): String? {
        val ytRegex = Regex("""(?:v=|youtu\.be/|embed/|shorts/)([a-zA-Z0-9_-]{11})""")
        val match = ytRegex.find(url)
        val id = match?.groupValues?.get(1)
        return if (id != null) "https://img.youtube.com/vi/$id/hqdefault.jpg" else null
    }

    private fun resolveUrl(baseUrl: String, relativeUrl: String): String {
        return try {
            if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
                relativeUrl
            } else if (relativeUrl.startsWith("//")) {
                "https:$relativeUrl"
            } else {
                val base = URI(baseUrl)
                base.resolve(relativeUrl).toString()
            }
        } catch (_: Exception) {
            relativeUrl
        }
    }

    private fun String.unescapeHtml(): String {
        return this
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }
}
