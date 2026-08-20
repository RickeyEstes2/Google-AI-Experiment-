package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.util.regex.Pattern

/**
 * Supported Font Families
 */
enum class RichFontFamily(val displayName: String, val composeFont: FontFamily) {
    DEFAULT("System", FontFamily.Default),
    SERIF("Serif", FontFamily.Serif),
    SANS("Sans", FontFamily.SansSerif),
    MONO("Mono", FontFamily.Monospace),
    CURSIVE("Cursive", FontFamily.Cursive)
}

/**
 * Supported Font Sizes
 */
enum class RichFontSize(val displayName: String, val size: TextUnit, val spValue: Int) {
    SMALL("Small", 12.sp, 12),
    NORMAL("Normal", 14.sp, 14),
    MEDIUM("Medium", 16.sp, 16),
    LARGE("Large", 20.sp, 20),
    HUGE("Huge", 24.sp, 24)
}

data class ColorOption(val name: String, val color: Color, val hex: String)

val AVAILABLE_TEXT_COLORS = listOf(
    ColorOption("Default", Color.Unspecified, ""),
    ColorOption("Black", Color(0xFF000000), "#000000"),
    ColorOption("Dark Slate", Color(0xFF1F2937), "#1F2937"),
    ColorOption("Medium Grey", Color(0xFF4B5563), "#4B5563"),
    ColorOption("Emerald", Color(0xFF059669), "#059669"),
    ColorOption("Rose", Color(0xFFE11D48), "#E11D48"),
    ColorOption("Amber", Color(0xFFD97706), "#D97706"),
    ColorOption("Purple", Color(0xFF7C3AED), "#7C3AED")
)

val AVAILABLE_HIGHLIGHT_COLORS = listOf(
    ColorOption("None", Color.Transparent, ""),
    ColorOption("Light Grey", Color(0xFFE5E7EB), "#E5E7EB"),
    ColorOption("Yellow", Color(0xFFFEF08A), "#FEF08A"),
    ColorOption("Light Green", Color(0xFFBBF7D0), "#BBF7D0"),
    ColorOption("Light Pink", Color(0xFFFECDD3), "#FECDD3"),
    ColorOption("Light Purple", Color(0xFFE9D5FF), "#E9D5FF"),
    ColorOption("Light Orange", Color(0xFFFFEDD5), "#FFEDD5")
)

/**
 * RichTextEngine formats and parses markdown-like spans:
 * Syntax: [word]{font=serif;size=16;color=#E11D48;bg=#FEF08A;url=https://example.com}
 * or standard markdown [text](url)
 */
object RichTextEngine {

    private val RICH_SPAN_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\{([^\\}]+)\\}")
    private val MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\)")
    private val RAW_URL_PATTERN = Pattern.compile("(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+)")
    private val HASHTAG_PATTERN = Pattern.compile("#([\\w\\d_]+)")

    fun buildAnnotated(
        rawText: String,
        defaultStyle: TextStyle = TextStyle.Default,
        primaryLinkColor: Color = Color(0xFF111827),
        hashtagColor: Color = Color(0xFF374151)
    ): AnnotatedString {
        return buildAnnotatedString {
            // First replace rich spans and standard links
            var currentIndex = 0

            // We parse rich tags [text]{properties} and standard [text](url)
            val combinedPattern = Pattern.compile("(\\[([^\\]]+)\\]\\{([^\\}]+)\\})|(\\[([^\\]]+)\\]\\((https?://[^\\)]+)\\))|(https?://[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]+)|(#[\\w\\d_]+)")
            val matcher = combinedPattern.matcher(rawText)

            while (matcher.find()) {
                val start = matcher.start()
                val end = matcher.end()

                // Append any preceding plain text
                if (start > currentIndex) {
                    append(rawText.substring(currentIndex, start))
                }

                val richSpanMatch = matcher.group(1)
                val markdownLinkMatch = matcher.group(4)
                val rawUrlMatch = matcher.group(7)
                val hashtagMatch = matcher.group(8)

                when {
                    richSpanMatch != null -> {
                        val word = matcher.group(2) ?: ""
                        val propsString = matcher.group(3) ?: ""
                        val props = parseProperties(propsString)

                        val spanStart = length
                        append(word)
                        val spanEnd = length

                        val spanStyle = SpanStyle(
                            fontFamily = props.fontFamily,
                            fontSize = props.fontSize ?: TextUnit.Unspecified,
                            color = props.textColor ?: Color.Unspecified,
                            background = props.highlightColor ?: Color.Transparent,
                            textDecoration = if (props.url != null) TextDecoration.Underline else null,
                            fontWeight = if (props.url != null) FontWeight.Bold else null
                        )
                        addStyle(spanStyle, spanStart, spanEnd)

                        if (!props.url.isNullOrBlank()) {
                            addStringAnnotation(
                                tag = "URL",
                                annotation = props.url,
                                start = spanStart,
                                end = spanEnd
                            )
                        }
                    }
                    markdownLinkMatch != null -> {
                        val linkText = matcher.group(5) ?: ""
                        val url = matcher.group(6) ?: ""

                        val spanStart = length
                        append(linkText)
                        val spanEnd = length

                        addStyle(
                            SpanStyle(
                                color = primaryLinkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Bold
                            ),
                            spanStart,
                            spanEnd
                        )
                        addStringAnnotation(
                            tag = "URL",
                            annotation = url,
                            start = spanStart,
                            end = spanEnd
                        )
                    }
                    rawUrlMatch != null -> {
                        val url = rawUrlMatch
                        val spanStart = length
                        append(url)
                        val spanEnd = length

                        addStyle(
                            SpanStyle(
                                color = primaryLinkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium
                            ),
                            spanStart,
                            spanEnd
                        )
                        addStringAnnotation(
                            tag = "URL",
                            annotation = url,
                            start = spanStart,
                            end = spanEnd
                        )
                    }
                    hashtagMatch != null -> {
                        val hashtag = hashtagMatch
                        val spanStart = length
                        append(hashtag)
                        val spanEnd = length

                        addStyle(
                            SpanStyle(
                                color = hashtagColor,
                                fontWeight = FontWeight.Bold
                            ),
                            spanStart,
                            spanEnd
                        )
                        addStringAnnotation(
                            tag = "HASHTAG",
                            annotation = hashtag,
                            start = spanStart,
                            end = spanEnd
                        )
                    }
                }

                currentIndex = end
            }

            // Append any remaining text
            if (currentIndex < rawText.length) {
                append(rawText.substring(currentIndex))
            }
        }
    }

    private data class ParsedProps(
        val fontFamily: FontFamily? = null,
        val fontSize: TextUnit? = null,
        val textColor: Color? = null,
        val highlightColor: Color? = null,
        val url: String? = null
    )

    private fun parseProperties(raw: String): ParsedProps {
        var fontFamily: FontFamily? = null
        var fontSize: TextUnit? = null
        var textColor: Color? = null
        var highlightColor: Color? = null
        var url: String? = null

        val pairs = raw.split(";", ",")
        for (pair in pairs) {
            val parts = pair.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].trim()

                when (key) {
                    "font" -> {
                        fontFamily = when (value.lowercase()) {
                            "serif" -> FontFamily.Serif
                            "sans" -> FontFamily.SansSerif
                            "mono", "monospace" -> FontFamily.Monospace
                            "cursive" -> FontFamily.Cursive
                            else -> FontFamily.Default
                        }
                    }
                    "size" -> {
                        fontSize = when (value.lowercase()) {
                            "sm", "small", "12" -> 12.sp
                            "normal", "14" -> 14.sp
                            "md", "medium", "16" -> 16.sp
                            "lg", "large", "20" -> 20.sp
                            "huge", "xl", "24" -> 24.sp
                            else -> value.toIntOrNull()?.sp
                        }
                    }
                    "color" -> {
                        textColor = parseHexColor(value)
                    }
                    "bg", "highlight" -> {
                        highlightColor = parseHexColor(value)
                    }
                    "url", "link" -> {
                        url = value
                    }
                }
            }
        }

        return ParsedProps(
            fontFamily = fontFamily,
            fontSize = fontSize,
            textColor = textColor,
            highlightColor = highlightColor,
            url = url
        )
    }

    private fun parseHexColor(hex: String): Color? {
        return try {
            val clean = hex.removePrefix("#")
            if (clean.length == 6) {
                val colorInt = clean.toLong(16) or 0x00000000FF000000
                Color(colorInt)
            } else if (clean.length == 8) {
                Color(clean.toLong(16))
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun createFormattedSpan(
        word: String,
        font: RichFontFamily,
        size: RichFontSize,
        textColorHex: String,
        highlightColorHex: String,
        hyperlinkUrl: String
    ): String {
        val cleanWord = word.trim()
        val propList = mutableListOf<String>()

        if (font != RichFontFamily.DEFAULT) {
            propList.add("font=${font.name.lowercase()}")
        }
        if (size != RichFontSize.NORMAL) {
            propList.add("size=${size.spValue}")
        }
        if (textColorHex.isNotBlank()) {
            propList.add("color=$textColorHex")
        }
        if (highlightColorHex.isNotBlank()) {
            propList.add("bg=$highlightColorHex")
        }
        if (hyperlinkUrl.isNotBlank()) {
            propList.add("url=$hyperlinkUrl")
        }

        return if (propList.isEmpty()) {
            cleanWord
        } else {
            "[$cleanWord]{${propList.joinToString(";")}}"
        }
    }
}

/**
 * Interactive Dialog for formatting a word or text range
 */
@Composable
fun RichFormatWordDialog(
    initialWord: String,
    initialUrl: String = "",
    onDismiss: () -> Unit,
    onApplyFormatting: (formattedSpan: String) -> Unit,
    onRemoveFormatting: () -> Unit
) {
    var textInput by remember { mutableStateOf(initialWord) }
    var selectedFont by remember { mutableStateOf(RichFontFamily.DEFAULT) }
    var selectedSize by remember { mutableStateOf(RichFontSize.NORMAL) }
    var selectedTextColorHex by remember { mutableStateOf("") }
    var selectedHighlightHex by remember { mutableStateOf("") }
    var urlInput by remember { mutableStateOf(initialUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FormatPaint,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Format Text & Links",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Word / Selection input
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Word / Selected Text") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Preview
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Live Preview:",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val previewBg = AVAILABLE_HIGHLIGHT_COLORS.firstOrNull { it.hex == selectedHighlightHex }?.color ?: Color.Transparent
                        val previewTextCol = AVAILABLE_TEXT_COLORS.firstOrNull { it.hex == selectedTextColorHex }?.color ?: MaterialTheme.colorScheme.onSurface

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = previewBg
                        ) {
                            Text(
                                text = if (textInput.isNotBlank()) textInput else "Sample Text",
                                style = TextStyle(
                                    fontFamily = selectedFont.composeFont,
                                    fontSize = selectedSize.size,
                                    color = if (urlInput.isNotBlank() && previewTextCol == MaterialTheme.colorScheme.onSurface) MaterialTheme.colorScheme.primary else previewTextCol,
                                    textDecoration = if (urlInput.isNotBlank()) TextDecoration.Underline else TextDecoration.None,
                                    fontWeight = if (urlInput.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // 1. Font Family
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Font Family", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RichFontFamily.values().forEach { font ->
                            FilterChip(
                                selected = selectedFont == font,
                                onClick = { selectedFont = font },
                                label = { Text(font.displayName, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Font Size
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Font Size", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RichFontSize.values().forEach { size ->
                            FilterChip(
                                selected = selectedSize == size,
                                onClick = { selectedSize = size },
                                label = { Text(size.displayName, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Font Color
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Font Color", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVAILABLE_TEXT_COLORS.forEach { opt ->
                            Surface(
                                shape = CircleShape,
                                color = if (opt.color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else opt.color,
                                border = if (selectedTextColorHex == opt.hex) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { selectedTextColorHex = opt.hex }
                            ) {
                                if (selectedTextColorHex == opt.hex) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Highlight Color
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Highlight Color", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AVAILABLE_HIGHLIGHT_COLORS.forEach { opt ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (opt.color == Color.Transparent) MaterialTheme.colorScheme.surfaceVariant else opt.color,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (selectedHighlightHex == opt.hex) 2.dp else 1.dp,
                                    if (selectedHighlightHex == opt.hex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(30.dp)
                                    .clickable { selectedHighlightHex = opt.hex }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selectedHighlightHex == opt.hex) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    } else if (opt.color == Color.Transparent) {
                                        Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Hyperlink URL
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hyperlink (URL)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        if (urlInput.isNotBlank()) {
                            TextButton(onClick = { urlInput = "" }, contentPadding = PaddingValues(0.dp)) {
                                Text("Remove Link", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        placeholder = { Text("https://example.com or any web link") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRemoveFormatting,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear Format")
                    }

                    Button(
                        onClick = {
                            val formatted = RichTextEngine.createFormattedSpan(
                                word = textInput,
                                font = selectedFont,
                                size = selectedSize,
                                textColorHex = selectedTextColorHex,
                                highlightColorHex = selectedHighlightHex,
                                hyperlinkUrl = urlInput
                            )
                            onApplyFormatting(formatted)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply")
                    }
                }
            }
        }
    }
}
