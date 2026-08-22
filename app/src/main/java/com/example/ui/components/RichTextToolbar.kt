package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppIcons

enum class ActiveFormatPanel {
    NONE,
    FONT_FAMILY,
    FONT_SIZE,
    FONT_COLOR,
    HIGHLIGHT_COLOR,
    HYPERLINK
}

@Composable
fun RichTextFormattingBar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    var activePanel by remember { mutableStateOf(ActiveFormatPanel.NONE) }
    var showLinkDialog by remember { mutableStateOf(false) }

    fun applyWrapFormatting(prefix: String, suffix: String) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text
        val selectedText = if (sel.start != sel.end) {
            text.substring(minOf(sel.start, sel.end), maxOf(sel.start, sel.end))
        } else {
            "text"
        }
        val start = minOf(sel.start, sel.end)
        val end = maxOf(sel.start, sel.end)

        val newText = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end)
        val newCursor = start + prefix.length + selectedText.length + suffix.length
        onValueChange(
            textFieldValue.copy(
                text = newText,
                selection = TextRange(start + prefix.length, start + prefix.length + selectedText.length)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF162032), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            .padding(6.dp)
    ) {
        // Main Toolbar Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bold
            FormatToolButton(label = "B", isSelected = false, fontWeight = FontWeight.ExtraBold) {
                applyWrapFormatting("**", "**")
            }

            // Italic
            FormatToolButton(label = "I", isSelected = false, fontStyle = FontStyle.Italic) {
                applyWrapFormatting("*", "*")
            }

            // Underline
            FormatToolButton(label = "U", isSelected = false, isUnderline = true) {
                applyWrapFormatting("<u>", "</u>")
            }

            // Strikethrough
            FormatToolButton(label = "S", isSelected = false, isStrikethrough = true) {
                applyWrapFormatting("~~", "~~")
            }

            VerticalDivider(modifier = Modifier.height(20.dp), color = Color(0xFF334155))

            // Font Family Selector toggle
            FormatToolChip(
                label = "Font",
                icon = "Aa",
                isActive = activePanel == ActiveFormatPanel.FONT_FAMILY
            ) {
                activePanel = if (activePanel == ActiveFormatPanel.FONT_FAMILY) ActiveFormatPanel.NONE else ActiveFormatPanel.FONT_FAMILY
            }

            // Font Size Selector toggle
            FormatToolChip(
                label = "Size",
                icon = "T↕",
                isActive = activePanel == ActiveFormatPanel.FONT_SIZE
            ) {
                activePanel = if (activePanel == ActiveFormatPanel.FONT_SIZE) ActiveFormatPanel.NONE else ActiveFormatPanel.FONT_SIZE
            }

            // Font Color Selector toggle
            FormatToolChip(
                label = "Color",
                icon = "🎨",
                isActive = activePanel == ActiveFormatPanel.FONT_COLOR
            ) {
                activePanel = if (activePanel == ActiveFormatPanel.FONT_COLOR) ActiveFormatPanel.NONE else ActiveFormatPanel.FONT_COLOR
            }

            // Highlight Background Selector toggle
            FormatToolChip(
                label = "Highlight",
                icon = "🖍",
                isActive = activePanel == ActiveFormatPanel.HIGHLIGHT_COLOR
            ) {
                activePanel = if (activePanel == ActiveFormatPanel.HIGHLIGHT_COLOR) ActiveFormatPanel.NONE else ActiveFormatPanel.HIGHLIGHT_COLOR
            }

            // Hyperlink Button
            FormatToolChip(
                label = "Link",
                icon = "🔗",
                isActive = false
            ) {
                showLinkDialog = true
            }

            VerticalDivider(modifier = Modifier.height(20.dp), color = Color(0xFF334155))

            // Inline Code
            FormatToolButton(label = "`code`", isSelected = false, isMonospace = true) {
                applyWrapFormatting("`", "`")
            }

            // Math Formula $$
            FormatToolChip(
                label = "Math",
                icon = "$$",
                isActive = false
            ) {
                applyWrapFormatting("$$ ", " $$")
            }
        }

        // Secondary Expandable Panels
        when (activePanel) {
            ActiveFormatPanel.FONT_FAMILY -> {
                FontFamilyPanel(
                    onSelectFont = { family ->
                        applyWrapFormatting("<span style=\"font-family:$family\">", "</span>")
                        activePanel = ActiveFormatPanel.NONE
                    }
                )
            }
            ActiveFormatPanel.FONT_SIZE -> {
                FontSizePanel(
                    onSelectSize = { sizePx ->
                        applyWrapFormatting("<span style=\"font-size:${sizePx}px\">", "</span>")
                        activePanel = ActiveFormatPanel.NONE
                    }
                )
            }
            ActiveFormatPanel.FONT_COLOR -> {
                FontColorPanel(
                    onSelectColor = { hexColor ->
                        applyWrapFormatting("<span style=\"color:$hexColor\">", "</span>")
                        activePanel = ActiveFormatPanel.NONE
                    }
                )
            }
            ActiveFormatPanel.HIGHLIGHT_COLOR -> {
                HighlightColorPanel(
                    onSelectHighlight = { hexColor ->
                        applyWrapFormatting("<mark style=\"background-color:$hexColor;color:#0F172A\">", "</mark>")
                        activePanel = ActiveFormatPanel.NONE
                    }
                )
            }
            else -> {}
        }
    }

    // Link Dialog
    if (showLinkDialog) {
        val sel = textFieldValue.selection
        val text = textFieldValue.text
        val selectedText = if (sel.start != sel.end) {
            text.substring(minOf(sel.start, sel.end), maxOf(sel.start, sel.end))
        } else {
            ""
        }

        HyperlinkEditorDialog(
            initialText = selectedText,
            onDismiss = { showLinkDialog = false },
            onApplyLink = { linkText, url ->
                val linkMarkdown = "[$linkText]($url)"
                val start = minOf(sel.start, sel.end)
                val end = maxOf(sel.start, sel.end)
                val newText = text.substring(0, start) + linkMarkdown + text.substring(end)
                onValueChange(
                    textFieldValue.copy(
                        text = newText,
                        selection = TextRange(start + linkMarkdown.length)
                    )
                )
                showLinkDialog = false
            }
        )
    }
}

@Composable
private fun FormatToolButton(
    label: String,
    isSelected: Boolean,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    isUnderline: Boolean = false,
    isStrikethrough: Boolean = false,
    isMonospace: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
        modifier = Modifier.height(32.dp).padding(horizontal = 2.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = fontWeight ?: FontWeight.Normal,
                fontStyle = fontStyle,
                fontFamily = if (isMonospace) FontFamily.Monospace else null
            )
        }
    }
}

@Composable
private fun FormatToolChip(
    label: String,
    icon: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) Color(0xFF0284C7) else Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color(0xFF38BDF8) else Color(0xFF334155)),
        modifier = Modifier.height(32.dp).padding(horizontal = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(text = icon, fontSize = 12.sp)
            Text(
                text = label,
                color = if (isActive) Color.White else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FontFamilyPanel(onSelectFont: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val fonts = listOf(
            "sans-serif" to "Sans-Serif (Modern)",
            "serif" to "Serif (Editorial)",
            "monospace" to "Monospace (Code)",
            "cursive" to "Cursive (Script)"
        )
        fonts.forEach { (fontKey, fontLabel) ->
            Surface(
                onClick = { onSelectFont(fontKey) },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Text(
                    text = fontLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FontSizePanel(onSelectSize: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val sizes = listOf(
            12 to "Small (12px)",
            15 to "Normal (15px)",
            18 to "Medium (18px)",
            22 to "Large (22px)",
            28 to "Display (28px)"
        )
        sizes.forEach { (sizePx, sizeLabel) ->
            Surface(
                onClick = { onSelectSize(sizePx) },
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
            ) {
                Text(
                    text = sizeLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun FontColorPanel(onSelectColor: (String) -> Unit) {
    val colors = listOf(
        "#FFFFFF" to Color.White,
        "#94A3B8" to Color(0xFF94A3B8),
        "#38BDF8" to Color(0xFF38BDF8),
        "#34D399" to Color(0xFF34D399),
        "#FBBF24" to Color(0xFFFBBF24),
        "#FB7185" to Color(0xFFFB7185),
        "#C084FC" to Color(0xFFC084FC),
        "#FB923C" to Color(0xFFFB923C)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Text Color:", color = Color(0xFF94A3B8), fontSize = 11.sp)
        colors.forEach { (hex, color) ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { onSelectColor(hex) }
            )
        }
    }
}

@Composable
private fun HighlightColorPanel(onSelectHighlight: (String) -> Unit) {
    val highlights = listOf(
        "#FEF08A" to Color(0xFFFEF08A), // Yellow
        "#BBF7D0" to Color(0xFFBBF7D0), // Green
        "#BAE6FD" to Color(0xFFBAE6FD), // Sky
        "#FBCFE8" to Color(0xFFFBCFE8), // Pink
        "#E9D5FF" to Color(0xFFE9D5FF), // Purple
        "#FED7AA" to Color(0xFFFED7AA)  // Orange
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Highlight:", color = Color(0xFF94A3B8), fontSize = 11.sp)
        highlights.forEach { (hex, color) ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(4.dp))
                    .clickable { onSelectHighlight(hex) }
            )
        }
    }
}

@Composable
fun HyperlinkEditorDialog(
    initialText: String,
    initialUrl: String = "",
    onDismiss: () -> Unit,
    onApplyLink: (text: String, url: String) -> Unit
) {
    var linkText by remember { mutableStateOf(initialText.ifBlank { "Click here" }) }
    var linkUrl by remember { mutableStateOf(initialUrl.ifBlank { "https://" }) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E293B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Insert / Edit Hyperlink",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                OutlinedTextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text("Display Text") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("Destination URL") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (linkUrl.isNotBlank()) {
                                onApplyLink(linkText.ifBlank { linkUrl }, linkUrl)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text("Apply Link", color = Color.White)
                    }
                }
            }
        }
    }
}
