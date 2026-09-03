package com.example.solveflow.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.engine.syntax.SyntaxHighlighterEngine

/**
 * An IDE-grade code snippet editor with dynamic syntax highlighting, line numbers gutter,
 * keyword legend, and visual distinction of language tokens.
 */
@Composable
fun CodeSnippetEditor(
    value: String,
    onValueChange: (String) -> Unit,
    languageId: String,
    modifier: Modifier = Modifier,
    minHeight: Dp = 180.dp,
    maxHeight: Dp = 320.dp,
    placeholder: String = "// Enter or paste code here...",
    customKeywords: Set<String> = emptySet(),
    showLegend: Boolean = true
) {
    val context = LocalContext.current
    var showSyntaxLegend by remember { mutableStateOf(false) }

    val visualTransformation = remember(languageId, customKeywords) {
        SyntaxHighlighterEngine.createVisualTransformation(
            languageId = languageId,
            isDark = true,
            customKeywords = customKeywords
        )
    }

    val lineCount = remember(value) {
        if (value.isEmpty()) 1 else value.count { it == '\n' } + 1
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(Color(0xFF313244))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Editor Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF252538))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Terminal Window Indicator dots
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                    Spacer(modifier = Modifier.width(5.dp))
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF313244)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = Color(0xFF89B4FA),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = languageId.uppercase(),
                                color = Color(0xFFCDD6F4),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$lineCount lines • ${value.length} chars",
                        color = Color(0xFF6C7086),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (showLegend) {
                        IconButton(
                            onClick = { showSyntaxLegend = !showSyntaxLegend },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Syntax Highlighting Legend",
                                tint = if (showSyntaxLegend) Color(0xFFCBA6F7) else Color(0xFF9399B2),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (value.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Code", value))
                                Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = Color(0xFF9399B2),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Syntax Legend Bar
            if (showSyntaxLegend) {
                Surface(
                    color = Color(0xFF181825),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(
                            text = "Syntax Engine Active: Regex Tokenizer & Palette",
                            color = Color(0xFFBAC2DE),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SyntaxPill("Keywords", Color(0xFFC678DD), true)
                            SyntaxPill("Control", Color(0xFFFF7B72), true)
                            SyntaxPill("Types", Color(0xFFE5C07B), false)
                            SyntaxPill("Strings", Color(0xFF98C379), false)
                            SyntaxPill("Numbers", Color(0xFFD19A66), false)
                            SyntaxPill("Comments", Color(0xFF7F849C), false)
                            SyntaxPill("Calls()", Color(0xFF89DCEB), false)
                        }
                    }
                }
            }

            // Editor Body with Gutter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight, max = maxHeight)
                    .padding(8.dp)
            ) {
                // Line numbers gutter
                Column(
                    modifier = Modifier
                        .width(36.dp)
                        .padding(end = 8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    val lines = if (value.isEmpty()) listOf("") else value.lines()
                    lines.forEachIndexed { i, _ ->
                        Text(
                            text = "${i + 1}",
                            color = Color(0xFF585B70),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }
                }

                // Vertical divider between gutter and editor
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color(0xFF313244))
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Editable text field with syntax highlighting visual transformation
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF585B70),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        )
                    }

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxSize(),
                        textStyle = TextStyle(
                            color = Color(0xFFCDD6F4),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 18.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF89B4FA)),
                        visualTransformation = visualTransformation
                    )
                }
            }
        }
    }
}

@Composable
private fun SyntaxPill(label: String, color: Color, isBold: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(color.copy(alpha = 0.4f)))
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
