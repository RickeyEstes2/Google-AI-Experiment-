package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import java.util.regex.Pattern

/**
 * Parses and renders text with clickable hyperlinks (URLs) and clickable hashtags (#tags).
 */
@Composable
fun HyperlinkText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    hashtagColor: Color = MaterialTheme.colorScheme.secondary,
    onHashtagClick: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Regex for URLs and Hashtags
    val urlPattern = Pattern.compile(
        "(https?://[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s]*)?)|(www\\.[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[^\\s]*)?)",
        Pattern.CASE_INSENSITIVE
    )
    val hashtagPattern = Pattern.compile("#(\\w+)")

    val annotatedString = buildAnnotatedString {
        append(text)

        // Find URLs
        val urlMatcher = urlPattern.matcher(text)
        while (urlMatcher.find()) {
            val start = urlMatcher.start()
            val end = urlMatcher.end()
            val rawUrl = urlMatcher.group()
            val cleanUrl = if (rawUrl.startsWith("http://") || rawUrl.startsWith("https://")) {
                rawUrl
            } else {
                "https://$rawUrl"
            }

            addStyle(
                style = SpanStyle(
                    color = linkColor,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline
                ),
                start = start,
                end = end
            )
            addStringAnnotation(
                tag = "URL",
                annotation = cleanUrl,
                start = start,
                end = end
            )
        }

        // Find Hashtags
        val hashtagMatcher = hashtagPattern.matcher(text)
        while (hashtagMatcher.find()) {
            val start = hashtagMatcher.start()
            val end = hashtagMatcher.end()
            val tag = hashtagMatcher.group()

            addStyle(
                style = SpanStyle(
                    color = hashtagColor,
                    fontWeight = FontWeight.Bold
                ),
                start = start,
                end = end
            )
            addStringAnnotation(
                tag = "HASHTAG",
                annotation = tag,
                start = start,
                end = end
            )
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style.copy(color = MaterialTheme.colorScheme.onSurface),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    openUrlInBrowser(context, annotation.item)
                    return@ClickableText
                }

            annotatedString.getStringAnnotations(tag = "HASHTAG", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onHashtagClick?.invoke(annotation.item)
                    return@ClickableText
                }
        }
    )
}

private fun openUrlInBrowser(context: Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open link: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
