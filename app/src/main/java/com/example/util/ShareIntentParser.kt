package com.example.util

import android.content.Intent

data class SharedWebData(
    val url: String = "",
    val title: String = "",
    val notes: String = ""
)

object ShareIntentParser {
    private val URL_REGEX = Regex("""(?i)\b(?:https?://|www\.)[^\s()<>]+(?:\([\w\d]+\)|(?:[^`!@#$%^&*(){}\[\]:;"'<>,.?\s]|/))""")

    /**
     * Parses incoming Intent from Chrome, Android browsers, or other apps sharing links.
     */
    fun parse(intent: Intent?): SharedWebData? {
        if (intent == null || intent.action != Intent.ACTION_SEND) return null
        val type = intent.type
        if (type == null || (!type.startsWith("text/") && type != "*/*")) return null

        val extraSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)?.trim() ?: ""
        val extraText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: ""

        if (extraText.isBlank() && extraSubject.isBlank()) return null

        val urlMatch = URL_REGEX.find(extraText)
        val extractedUrl = urlMatch?.value ?: ""

        val remainingText = if (urlMatch != null) {
            extraText.replace(urlMatch.value, "").trim()
        } else {
            extraText
        }

        val title = when {
            extraSubject.isNotBlank() -> extraSubject
            remainingText.isNotBlank() -> {
                val lines = remainingText.lines().filter { it.isNotBlank() }
                lines.firstOrNull() ?: ""
            }
            extractedUrl.isNotBlank() -> {
                try {
                    val uri = java.net.URI(if (!extractedUrl.startsWith("http")) "https://$extractedUrl" else extractedUrl)
                    uri.host?.removePrefix("www.")?.replaceFirstChar { it.uppercase() } ?: "Shared Link"
                } catch (_: Exception) {
                    "Shared Link"
                }
            }
            else -> "Shared Note"
        }

        val notes = if (remainingText.isNotBlank() && remainingText != title) {
            remainingText
        } else {
            ""
        }

        val normalizedUrl = if (extractedUrl.isNotBlank() && !extractedUrl.startsWith("http://") && !extractedUrl.startsWith("https://")) {
            "https://$extractedUrl"
        } else {
            extractedUrl
        }

        return SharedWebData(
            url = normalizedUrl,
            title = title,
            notes = notes
        )
    }
}
