package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Article

@Composable
fun EditArticleDialog(
    article: Article,
    allArticles: List<Article>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        url: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedPostIds: List<Long>
    ) -> Unit
) {
    var title by remember { mutableStateOf(article.title) }
    var url by remember { mutableStateOf(article.url) }
    var thumbnailUrl by remember { mutableStateOf(article.thumbnailUrl) }
    var summary by remember { mutableStateOf(article.summary) }
    var notes by remember { mutableStateOf(article.notes) }
    var hashtags by remember { mutableStateOf(article.hashtags) }
    var newTagInput by remember { mutableStateOf("") }
    var isAddingTag by remember { mutableStateOf(false) }

    var linkedPostIds by remember { mutableStateOf(article.linkedPostIds) }
    var showLinkPicker by remember { mutableStateOf(false) }

    // Rich text formatting dialog state
    var showFormatDialog by remember { mutableStateOf(false) }
    var formatWordTarget by remember { mutableStateOf("") }

    // Insert LaTeX, Chart, Venn dialogs state
    var showInsertLatex by remember { mutableStateOf(false) }
    var showInsertChart by remember { mutableStateOf(false) }
    var showInsertVenn by remember { mutableStateOf(false) }
    var showPreviewTab by remember { mutableStateOf(false) }

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

    if (showLinkPicker) {
        LinkPostPickerDialog(
            currentArticleId = article.id,
            allArticles = allArticles,
            initialSelectedIds = linkedPostIds,
            onDismiss = { showLinkPicker = false },
            onConfirmSelection = { selected ->
                linkedPostIds = selected
                showLinkPicker = false
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
                    // Strips markup if present
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
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Edit Post",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("edit_post_title_field")
                )

                // URL
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Web URL") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Thumbnail / Preview Image URL
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("Preview Image URL (Optional)") },
                        placeholder = { Text("https://example.com/image.jpg") },
                        leadingIcon = { Icon(Icons.Outlined.Image, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (thumbnailUrl.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AsyncImage(
                                model = thumbnailUrl,
                                contentDescription = "Preview Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(
                                text = "Preview image loaded",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { thumbnailUrl = "" }) {
                                Text("Remove Image", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Summary
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Summary") },
                    placeholder = { Text("Brief overview...") },
                    leadingIcon = { Icon(Icons.Outlined.Subject, contentDescription = null) },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes with Rich Formatting Toolbar
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notes & Rich Formatting",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FilledTonalButton(
                            onClick = {
                                formatWordTarget = ""
                                showFormatDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FormatPaint, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Format Word / Link", fontSize = 11.sp)
                        }
                    }

                    // Rich Insertion Tools & Preview Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = showPreviewTab,
                            onClick = { showPreviewTab = !showPreviewTab },
                            label = { Text(if (showPreviewTab) "Edit Mode" else "Live Preview", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    if (showPreviewTab) Icons.Default.Edit else Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        )

                        SuggestionChip(
                            onClick = { showInsertLatex = true },
                            label = { Text("+ LaTeX Formula", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        SuggestionChip(
                            onClick = { showInsertChart = true },
                            label = { Text("+ Chart", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )

                        SuggestionChip(
                            onClick = { showInsertVenn = true },
                            label = { Text("+ Venn Diagram", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.DonutSmall, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        )
                    }

                    if (showPreviewTab) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "PREVIEW",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                RichArticleContent(
                                    rawContent = notes
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            placeholder = { Text("Personal notes, LaTeX formulas ($$ x^2 $$), charts, Venn diagrams, hyperlinks...") },
                            leadingIcon = { Icon(Icons.Outlined.EditNote, contentDescription = null) },
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Quick formatting suggestions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Highlight", "Key Point", "Read Later", "Action Item").forEach { sampleText ->
                            SuggestionChip(
                                onClick = {
                                    formatWordTarget = sampleText
                                    showFormatDialog = true
                                },
                                label = { Text("+ Style \"$sampleText\"", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Custom Labels & Tags Management
                LabelTagPicker(
                    selectedTags = hashtags,
                    onTagsChanged = { hashtags = it },
                    allAvailableTags = allArticles.flatMap { it.hashtags }.distinct()
                )

                // Linked Posts Management
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Linked Posts (${linkedPostIds.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { showLinkPicker = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage Links", fontSize = 12.sp)
                        }
                    }

                    val linkedArticles = remember(allArticles, linkedPostIds) {
                        allArticles.filter { linkedPostIds.contains(it.id) }
                    }

                    if (linkedArticles.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            linkedArticles.forEach { linked ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(linked.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), maxLines = 1)
                                            Text(linked.sourceDomain, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                                        }
                                        IconButton(
                                            onClick = {
                                                linkedPostIds = linkedPostIds.filterNot { it == linked.id }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Unlink", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No other posts linked yet. Cross-link posts to browse them together.", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() || url.isNotBlank() || notes.isNotBlank()) {
                                onSave(
                                    title,
                                    url,
                                    thumbnailUrl,
                                    summary,
                                    notes,
                                    hashtags,
                                    linkedPostIds
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f).testTag("save_edited_post_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
