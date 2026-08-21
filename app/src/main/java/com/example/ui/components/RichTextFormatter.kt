package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RenderFormattedMarkdown(
    content: String,
    modifier: Modifier = Modifier
) {
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
                    // Close code block
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
                    Text(
                        text = parseInlineStyles(trimmed.removePrefix("### ")),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = parseInlineStyles(trimmed.removePrefix("## ")),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("# ") -> {
                    Text(
                        text = parseInlineStyles(trimmed.removePrefix("# ")),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                // Bulleted list item
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = parseInlineStyles(trimmed.substring(2)),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
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
                        Text(number, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = parseInlineStyles(textPart),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
                // Empty line spacer
                trimmed.isBlank() -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
                // Standard text line
                else -> {
                    Text(
                        text = parseInlineStyles(line),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }

        // Flush any open blocks
        if (codeBlockLines.isNotEmpty()) {
            CodeBlock(codeBlockLines.joinToString("\n"))
        }
        if (latexBlockLines.isNotEmpty()) {
            LatexMathFormula(latex = latexBlockLines.joinToString(" "), isBlock = true)
        }
    }
}

@Composable
private fun CodeBlock(code: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF1E293B),
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
                color = Color(0xFFF1F5F9),
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                lineHeight = 18.sp
            )
        }
    }
}

fun parseInlineStyles(text: String) = buildAnnotatedString {
    var i = 0
    while (i < text.length) {
        // Bold: **text**
        if (text.startsWith("**", i)) {
            val end = text.indexOf("**", i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(text.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }
        // Italic: *text* or _text_
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
        // Inline code: `code`
        if (text.startsWith("`", i)) {
            val end = text.indexOf("`", i + 1)
            if (end != -1) {
                withStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFFE2E8F0),
                        color = Color(0xFF0F172A)
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
