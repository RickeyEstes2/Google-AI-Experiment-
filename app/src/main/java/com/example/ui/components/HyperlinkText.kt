package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

private val URL_REGEX = Regex("(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+)")

@Composable
fun HyperlinkText(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        val matches = URL_REGEX.findAll(text)

        for (match in matches) {
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val url = match.value
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.secondary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(url)
            }
            pop()

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                }
        }
    )
}
