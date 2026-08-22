package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderFormattedMarkdown(
    content: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lines = content.lines()
    var inCodeBlock = false
    val codeBlockLines = mutableListOf<String>()
    var inLatexBlock = false
    val latexBlockLines = mutableListOf<String>()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (line in lines) {
            val trimmed = line.trim()

            // Code block handling
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    CodeBlock(codeBlockLines.joinToString("\n"))
                    codeBlockLines.clear()
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }
            if (inCodeBlock) {
                codeBlockLines.add(line)
                continue
            }

            // LaTeX math block handling ($$ ... $$)
            if (trimmed.startsWith("$$") && trimmed.endsWith("$$") && trimmed.length > 2) {
                LatexMathFormula(latex = trimmed, isBlock = true)
                continue
            }
            if (trimmed == "$$") {
                if (inLatexBlock) {
                    LatexMathFormula(latex = latexBlockLines.joinToString(" "), isBlock = true)
                    latexBlockLines.clear()
                    inLatexBlock = false
                } else {
                    inLatexBlock = true
                }
                continue
            }
            if (inLatexBlock) {
                latexBlockLines.add(line)
                continue
            }

            // Headings
            when {
                trimmed.startsWith("### ") -> {
                    RenderFormattedLine(
                        annotatedText = parseRichInlineStyles(trimmed.removePrefix("### ")),
                        defaultFontSize = 16.sp,
                        defaultFontWeight = FontWeight.Bold,
                        onUrlClick = { url -> openWebUrl(context, url) },
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    RenderFormattedLine(
                        annotatedText = parseRichInlineStyles(trimmed.removePrefix("## ")),
                        defaultFontSize = 18.sp,
                        defaultFontWeight = FontWeight.ExtraBold,
                        onUrlClick = { url -> openWebUrl(context, url) },
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    RenderFormattedLine(
                        annotatedText = parseRichInlineStyles(trimmed.removePrefix("# ")),
                        defaultFontSize = 22.sp,
                        defaultFontWeight = FontWeight.ExtraBold,
                        onUrlClick = { url -> openWebUrl(context, url) },
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                // Bulleted list
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        RenderFormattedLine(
                            annotatedText = parseRichInlineStyles(trimmed.substring(2)),
                            onUrlClick = { url -> openWebUrl(context, url) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Numbered list
                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val dotIndex = trimmed.indexOf('.')
                    val number = trimmed.substring(0, dotIndex + 1)
                    val textPart = trimmed.substring(dotIndex + 1).trim()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(number, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        RenderFormattedLine(
                            annotatedText = parseRichInlineStyles(textPart),
                            onUrlClick = { url -> openWebUrl(context, url) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Spacer
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Standard text line
                else -> {
                    RenderFormattedLine(
                        annotatedText = parseRichInlineStyles(line),
                        onUrlClick = { url -> openWebUrl(context, url) }
                    )
                }
            }
        }

        if (codeBlockLines.isNotEmpty()) {
            CodeBlock(codeBlockLines.joinToString("\n"))
        }
        if (latexBlockLines.isNotEmpty()) {
            LatexMathFormula(latex = latexBlockLines.joinToString(" "), isBlock = true)
        }
    }
}

private fun openWebUrl(context: android.content.Context, url: String) {
    try {
        val target = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(target))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

@Composable
private fun RenderFormattedLine(
    annotatedText: AnnotatedString,
    defaultFontSize: androidx.compose.ui.unit.TextUnit = 14.5.sp,
    defaultFontWeight: FontWeight = FontWeight.Normal,
    onUrlClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = Color(0xFFF1F5F9),
            fontSize = defaultFontSize,
            fontWeight = defaultFontWeight,
            lineHeight = 22.sp
        ),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onUrlClick(annotation.item)
                }
        },
        modifier = modifier
    )
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFF38BDF8),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        }
    }
}

/**
 * Advanced parser supporting Markdown, Underline, Strikethrough, Hyperlinks,
 * HTML span styles (font-family, font-size, color), and mark highlight tags.
 */
fun parseRichInlineStyles(rawText: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    val text = rawText

    while (i < text.length) {
        // 1. Hyperlink markdown [text](url)
        if (text[i] == '[') {
            val closeBracket = text.indexOf(']', i + 1)
            if (closeBracket != -1 && closeBracket + 1 < text.length && text[closeBracket + 1] == '(') {
                val closeParen = text.indexOf(')', closeBracket + 2)
                if (closeParen != -1) {
                    val linkText = text.substring(i + 1, closeBracket)
                    val linkUrl = text.substring(closeBracket + 2, closeParen)
                    pushStringAnnotation(tag = "URL", annotation = linkUrl)
                    withStyle(
                        SpanStyle(
                            color = Color(0xFF38BDF8),
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(linkText)
                    }
                    pop()
                    i = closeParen + 1
                    continue
                }
            }
        }

        // 2. HTML <u>underline</u>
        if (text.startsWith("<u>", i, ignoreCase = true)) {
            val endTag = text.indexOf("</u>", i + 3, ignoreCase = true)
            if (endTag != -1) {
                val inner = text.substring(i + 3, endTag)
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(inner)
                }
                i = endTag + 4
                continue
            }
        }

        // 3. Strikethrough ~~text~~
        if (text.startsWith("~~", i)) {
            val endTag = text.indexOf("~~", i + 2)
            if (endTag != -1) {
                val inner = text.substring(i + 2, endTag)
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                    append(inner)
                }
                i = endTag + 2
                continue
            }
        }

        // 4. HTML <mark ...>highlight</mark>
        if (text.startsWith("<mark", i, ignoreCase = true)) {
            val closeMarkOpening = text.indexOf('>', i)
            val endTag = text.indexOf("</mark>", i, ignoreCase = true)
            if (closeMarkOpening != -1 && endTag != -1 && endTag > closeMarkOpening) {
                val tagAttributes = text.substring(i, closeMarkOpening)
                val inner = text.substring(closeMarkOpening + 1, endTag)
                val highlightColor = parseColorFromTag(tagAttributes) ?: Color(0xFFFEF08A)
                withStyle(
                    SpanStyle(
                        background = highlightColor,
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(inner)
                }
                i = endTag + 7
                continue
            }
        }

        // 5. HTML <span style="...">text</span>
        if (text.startsWith("<span", i, ignoreCase = true)) {
            val closeOpening = text.indexOf('>', i)
            val endTag = text.indexOf("</span>", i, ignoreCase = true)
            if (closeOpening != -1 && endTag != -1 && endTag > closeOpening) {
                val tagAttributes = text.substring(i, closeOpening)
                val inner = text.substring(closeOpening + 1, endTag)
                val spanStyle = parseSpanStyle(tagAttributes)
                withStyle(spanStyle) {
                    append(inner)
                }
                i = endTag + 7
                continue
            }
        }

        // 6. Bold: **text**
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }

        // 7. Italic: *text*
        if (text.startsWith("*", i) && !text.startsWith("**", i)) {
            val end = text.indexOf("*", i + 1)
            if (end != -1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(text.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }

        // 8. Inline code: `code`
        if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF1E293B),
                        color = Color(0xFF38BDF8),
                        fontSize = 13.sp
                    )
                ) {
                    append(" ${text.substring(i + 1, end)} ")
                }
                i = end + 1
                continue
            }
        }

        append(text[i])
        i++
    }
}

private fun parseColorFromTag(tag: String): Color? {
    val hexRegex = Regex("#([0-9a-fA-F]{6}|[0-9a-fA-F]{8})")
    val match = hexRegex.find(tag) ?: return null
    return try {
        val hex = match.value.removePrefix("#")
        val colorLong = hex.toLong(16)
        if (hex.length == 6) {
            Color(colorLong or 0x00000000FF000000L)
        } else {
            Color(colorLong)
        }
    } catch (_: Exception) {
        null
    }
}

private fun parseSpanStyle(attributes: String): SpanStyle {
    var color: Color? = null
    var fontFamily: FontFamily? = null
    var fontSize: androidx.compose.ui.unit.TextUnit? = null
    var background: Color? = null

    // Parse font-family
    if (attributes.contains("font-family:serif", ignoreCase = true)) {
        fontFamily = FontFamily.Serif
    } else if (attributes.contains("font-family:monospace", ignoreCase = true) || attributes.contains("font-family:mono", ignoreCase = true)) {
        fontFamily = FontFamily.Monospace
    } else if (attributes.contains("font-family:cursive", ignoreCase = true)) {
        fontFamily = FontFamily.Cursive
    } else if (attributes.contains("font-family:sans", ignoreCase = true)) {
        fontFamily = FontFamily.SansSerif
    }

    // Parse font-size
    val sizeRegex = Regex("font-size:(\\d+)px")
    sizeRegex.find(attributes)?.groupValues?.get(1)?.toIntOrNull()?.let { px ->
        fontSize = px.sp
    }

    // Parse text color
    val colorRegex = Regex("(?<!background-)color:(#[0-9a-fA-F]{6})")
    colorRegex.find(attributes)?.groupValues?.get(1)?.let { hex ->
        try {
            color = Color(hex.removePrefix("#").toLong(16) or 0x00000000FF000000L)
        } catch (_: Exception) {}
    }

    // Parse background color
    val bgRegex = Regex("background(?:-color)?:(#[0-9a-fA-F]{6})")
    bgRegex.find(attributes)?.groupValues?.get(1)?.let { hex ->
        try {
            background = Color(hex.removePrefix("#").toLong(16) or 0x00000000FF000000L)
        } catch (_: Exception) {}
    }

    return SpanStyle(
        color = color ?: Color.Unspecified,
        fontFamily = fontFamily,
        fontSize = fontSize ?: androidx.compose.ui.unit.TextUnit.Unspecified,
        background = background ?: Color.Unspecified
    )
}
