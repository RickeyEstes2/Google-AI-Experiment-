package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun AddArticleDialog(
    onDismiss: () -> Unit,
    onSave: (
        url: String,
        title: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var thumbnailUrl by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<List<String>>(emptyList()) }

    // Rich text formatting dialog state
    var showFormatDialog by remember { mutableStateOf(false) }
    var formatWordTarget by remember { mutableStateOf("") }

    var showInsertLatex by remember { mutableStateOf(false) }
    var showInsertChart by remember { mutableStateOf(false) }
    var showInsertVenn by remember { mutableStateOf(false) }

    if (showInsertLatex) {
        InsertLatexDialog(
            onDismiss = { showInsertLatex = false },
            onInsertFormula = { latexCode ->
                notes = if (notes.isBlank()) latexCode else "$notes\n\n$latexCode"
                showInsertLatex = false
            }
        )
    }

    if (showInsertChart) {
        InsertChartDialog(
            onDismiss = { showInsertChart = false },
            onInsertChart = { chartCode ->
                notes = if (notes.isBlank()) chartCode else "$notes\n\n$chartCode"
                showInsertChart = false
            }
        )
    }

    if (showInsertVenn) {
        InsertVennDialog(
            onDismiss = { showInsertVenn = false },
            onInsertVenn = { vennCode ->
                notes = if (notes.isBlank()) vennCode else "$notes\n\n$vennCode"
                showInsertVenn = false
            }
        )
    }

    if (showFormatDialog) {
        RichFormatWordDialog(
            initialWord = formatWordTarget,
            onDismiss = { showFormatDialog = false },
            onApplyFormatting = { formattedSpan ->
                if (formatWordTarget.isNotBlank() && notes.contains(formatWordTarget)) {
                    notes = notes.replaceFirst(formatWordTarget, formattedSpan)
                } else {
                    notes = if (notes.isBlank()) formattedSpan else "$notes $formattedSpan"
                }
                showFormatDialog = false
            },
            onRemoveFormatting = {
                if (formatWordTarget.isNotBlank()) {
                    val plain = formatWordTarget.replace(Regex("\\[([^\\]]+)\\]\\{[^\\}]+\\}"), "$1")
                    notes = notes.replace(formatWordTarget, plain)
                }
                showFormatDialog = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Save Link & Details",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Tip: You can also share any website directly from Google Chrome by tapping 'Share' in Chrome.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Web URL") },
                    placeholder = { Text("https://example.com") },
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_link_url_field")
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (Optional)") },
                    placeholder = { Text("My Saved Web Page") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_link_title_field")
                )

                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    label = { Text("Preview Image URL (Optional)") },
                    placeholder = { Text("https://example.com/image.png") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Image, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary (Optional)") },
                    placeholder = { Text("Brief overview of the article...") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Subject, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes & Rich Text (Optional)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FilledTonalButton(
                            onClick = {
                                formatWordTarget = ""
                                showFormatDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FormatPaint, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Format", fontSize = 11.sp)
                        }
                    }

                    // Quick insert chips for LaTeX, Chart, Venn
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { showInsertLatex = true },
                            label = { Text("+ LaTeX", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = { showInsertChart = true },
                            label = { Text("+ Chart", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = { showInsertVenn = true },
                            label = { Text("+ Venn", fontSize = 11.sp) }
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Add personal thoughts, LaTeX formulas ($$ x^2 $$), charts, Venn diagrams...") },
                        leadingIcon = {
                            Icon(Icons.Outlined.EditNote, contentDescription = null)
                        },
                        minLines = 2,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Custom Labels & Tags Selector
                LabelTagPicker(
                    selectedTags = tags,
                    onTagsChanged = { tags = it }
                )

                Button(
                    onClick = {
                        if (url.isNotBlank() || title.isNotBlank() || notes.isNotBlank() || summary.isNotBlank()) {
                            val finalUrl = if (url.startsWith("http://") || url.startsWith("https://")) {
                                url
                            } else if (url.isNotBlank()) {
                                "https://$url"
                            } else {
                                "https://saved.link/${System.currentTimeMillis()}"
                            }

                            onSave(finalUrl, title, thumbnailUrl, summary, notes, tags)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_save_link_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Link", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

