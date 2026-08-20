package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Parses and renders rich text formatting (custom fonts, sizes, colors, highlights),
 * clickable hyperlinks (URLs), and clickable hashtags (#tags).
 * Supports long-press on word or words to change the font, font size, font color,
 * highlight color and add, remove, and edit hyperlinks.
 */
@Composable
fun HyperlinkText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    hashtagColor: Color = MaterialTheme.colorScheme.secondary,
    onHashtagClick: ((String) -> Unit)? = null,
    onLongPressWord: ((String) -> Unit)? = null,
    onTextFormatted: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    val annotatedString = remember(text, linkColor, hashtagColor, style) {
        RichTextEngine.buildAnnotated(
            rawText = text,
            defaultStyle = style,
            primaryLinkColor = linkColor,
            hashtagColor = hashtagColor
        )
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var targetWord by remember { mutableStateOf("") }
    var targetUrl by remember { mutableStateOf("") }

    if (showFormatDialog) {
        RichFormatWordDialog(
            initialWord = targetWord,
            initialUrl = targetUrl,
            onDismiss = { showFormatDialog = false },
            onApplyFormatting = { formattedSpan ->
                if (targetWord.isNotBlank() && text.contains(targetWord)) {
                    val updated = text.replaceFirst(targetWord, formattedSpan)
                    onTextFormatted?.invoke(updated)
                } else if (text.isBlank()) {
                    onTextFormatted?.invoke(formattedSpan)
                } else {
                    onTextFormatted?.invoke("$text $formattedSpan")
                }
                showFormatDialog = false
            },
            onRemoveFormatting = {
                if (targetWord.isNotBlank()) {
                    val plain = targetWord.replace(Regex("\\[([^\\]]+)\\]\\{[^\\}]+\\}"), "$1")
                        .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
                    val updated = text.replace(targetWord, plain)
                    onTextFormatted?.invoke(updated)
                }
                showFormatDialog = false
            }
        )
    }

    Box(
        modifier = modifier.pointerInput(annotatedString, text) {
            detectTapGestures(
                onTap = { offset ->
                    layoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)
                        // Check URL annotation
                        annotatedString.getStringAnnotations(tag = "URL", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                openUrlInBrowser(context, annotation.item)
                                return@detectTapGestures
                            }

                        // Check Hashtag annotation
                        annotatedString.getStringAnnotations(tag = "HASHTAG", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                onHashtagClick?.invoke(annotation.item)
                                return@detectTapGestures
                            }
                    }
                },
                onLongPress = { offset ->
                    layoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)
                        // Check if long pressing an existing URL annotation
                        val existingUrl = annotatedString.getStringAnnotations(tag = "URL", start = position, end = position)
                            .firstOrNull()?.item ?: ""

                        val wordRange = getWordRangeAtOffset(annotatedString.text, position)
                        if (wordRange != null) {
                            val word = annotatedString.text.substring(wordRange.first, wordRange.second)
                            if (word.isNotBlank()) {
                                targetWord = word
                                targetUrl = existingUrl
                                onLongPressWord?.invoke(word)
                                if (onTextFormatted != null) {
                                    showFormatDialog = true
                                }
                            }
                        }
                    }
                }
            )
        }
    ) {
        Text(
            text = annotatedString,
            style = style.copy(color = MaterialTheme.colorScheme.onSurface),
            onTextLayout = { layoutResult = it }
        )
    }
}

private fun getWordRangeAtOffset(text: String, offset: Int): Pair<Int, Int>? {
    if (text.isEmpty() || offset < 0 || offset > text.length) return null
    val target = if (offset == text.length) offset - 1 else offset
    if (target < 0 || target >= text.length) return null

    // Expand left
    var start = target
    while (start > 0 && !text[start - 1].isWhitespace()) {
        start--
    }

    // Expand right
    var end = target
    while (end < text.length && !text[end].isWhitespace()) {
        end++
    }

    return if (start < end) Pair(start, end) else null
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
