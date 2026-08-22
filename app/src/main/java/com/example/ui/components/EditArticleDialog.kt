package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Addendum
import com.example.data.model.Article
import com.example.ui.theme.AppIcons
import com.example.util.LinkMetadataFetcher
import com.example.util.TimeUtils
import kotlinx.coroutines.launch

@Composable
fun EditArticleDialog(
    article: Article,
    allArticles: List<Article>,
    availableTags: List<String>,
    onDismiss: () -> Unit,
    onSave: (Article) -> Unit,
    onDelete: (Article) -> Unit
) {
    var url by remember { mutableStateOf(article.url) }
    var title by remember { mutableStateOf(article.title) }
    var thumbnailUrl by remember { mutableStateOf(article.thumbnailUrl) }
    var summary by remember { mutableStateOf(article.summary) }
    var notesValue by remember { mutableStateOf(TextFieldValue(article.notes, TextRange(article.notes.length))) }
    var hashtags by remember { mutableStateOf(article.hashtags) }
    var linkedIds by remember { mutableStateOf(article.linkedArticleIds) }

    // Video fields
    var videoUrl by remember { mutableStateOf(article.videoUrl) }
    var videoStartText by remember { mutableStateOf(if (article.videoStartSeconds > 0) TimeUtils.parseSecondsToTimeString(article.videoStartSeconds) else "") }
    var videoEndText by remember { mutableStateOf(if (article.videoEndSeconds > 0) TimeUtils.parseSecondsToTimeString(article.videoEndSeconds) else "") }
    var videoAutostart by remember { mutableStateOf(article.videoAutostart) }

    // Addendums
    var addendums by remember { mutableStateOf(article.addendums) }
    var newAddendumText by remember { mutableStateOf("") }
    var isAddingAddendum by remember { mutableStateOf(false) }

    var isFetchingMetadata by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var showLinkPickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showNotesPreview by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
                .testTag("edit_article_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Note & Media",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(AppIcons.Close, contentDescription = "Close")
                    }
                }

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("URL / Link Address") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_url_input")
                    )

                    // Image Preview & Auto-Fetch Button
                    if (thumbnailUrl.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = "Thumbnail Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    IconButton(
                                        onClick = { thumbnailUrl = "" },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(28.dp)
                                    ) {
                                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                                            Icon(AppIcons.Close, contentDescription = "Remove Image", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else if (url.isNotBlank()) {
                        FilledTonalButton(
                            onClick = {
                                scope.launch {
                                    isFetchingMetadata = true
                                    try {
                                        val meta = LinkMetadataFetcher.fetchMetadata(url)
                                        if (!meta.imageUrl.isNullOrBlank()) {
                                            thumbnailUrl = meta.imageUrl
                                        }
                                    } catch (_: Exception) {} finally {
                                        isFetchingMetadata = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isFetchingMetadata) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-fetching image...", fontSize = 12.sp)
                            } else {
                                Text("Auto-Fetch Image from URL", fontSize = 12.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_title_input")
                    )

                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("Thumbnail Image URL") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Video Support Section (YouTube / Locally hosted)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🎥 Video Support", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("(YouTube or Local Video)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            OutlinedTextField(
                                value = videoUrl,
                                onValueChange = { videoUrl = it },
                                label = { Text("Video URL / Path") },
                                placeholder = { Text("https://youtube.com/watch?v=... or /storage/.../video.mp4") },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = videoStartText,
                                    onValueChange = { videoStartText = it },
                                    label = { Text("Begin at Time") },
                                    placeholder = { Text("e.g. 0:30 or 30s") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = videoEndText,
                                    onValueChange = { videoEndText = it },
                                    label = { Text("End at Time") },
                                    placeholder = { Text("e.g. 2:15 or 135s") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Autostart playback", fontSize = 13.sp)
                                Switch(
                                    checked = videoAutostart,
                                    onCheckedChange = { videoAutostart = it }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Summary") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Rich Text Notes with Formatting Bar (Font, Size, Color, Style, Highlight, Hyperlink)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Personal Notes & Rich Text",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = { showNotesPreview = !showNotesPreview },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(if (showNotesPreview) "Edit Notes" else "Preview Render", fontSize = 12.sp)
                                }
                            }
                        }

                        if (!showNotesPreview) {
                            // Long-press / Selection formatting toolbar
                            RichTextFormattingBar(
                                textFieldValue = notesValue,
                                onValueChange = { notesValue = it }
                            )

                            OutlinedTextField(
                                value = notesValue,
                                onValueChange = { notesValue = it },
                                label = { Text("Notes (Select text to format font, size, color, style, highlight, link)") },
                                placeholder = { Text("Select words to format with toolbar above, or write markdown...") },
                                minLines = 5,
                                maxLines = 10,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("edit_notes_input")
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    RenderFormattedMarkdown(content = notesValue.text.ifBlank { "*No notes entered yet.*" })
                                }
                            }
                        }
                    }

                    // Addendums Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Addendums (${addendums.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                TextButton(
                                    onClick = { isAddingAddendum = !isAddingAddendum },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("+ Add Note", fontSize = 12.sp)
                                }
                            }

                            if (addendums.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    addendums.forEachIndexed { index, item ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Addendum #${index + 1} · ${TimeUtils.formatPostDateTime(item.timestamp)}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(item.content, fontSize = 12.sp, maxLines = 2)
                                                }
                                                IconButton(
                                                    onClick = { addendums = addendums.filterNot { it.id == item.id } },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(AppIcons.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isAddingAddendum) {
                                OutlinedTextField(
                                    value = newAddendumText,
                                    onValueChange = { newAddendumText = it },
                                    placeholder = { Text("Write addendum note...") },
                                    minLines = 2,
                                    maxLines = 4,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            if (newAddendumText.isNotBlank()) {
                                                addendums = addendums + Addendum(content = newAddendumText.trim())
                                                newAddendumText = ""
                                                isAddingAddendum = false
                                            }
                                        },
                                        enabled = newAddendumText.isNotBlank(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Append Addendum", fontSize = 11.5.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Custom Labels & Tags
                    LabelTagPicker(
                        selectedTags = hashtags,
                        onTagsChanged = { hashtags = it },
                        allAvailableTags = availableTags
                    )

                    // Connected Links Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Linked Notes (${linkedIds.size})",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        FilledTonalButton(
                            onClick = { showLinkPickerDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(AppIcons.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Manage Linked Notes", fontSize = 12.sp)
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showDeleteConfirm = true }
                    ) {
                        Icon(
                            imageVector = AppIcons.Delete,
                            contentDescription = "Delete Note",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val startSec = TimeUtils.parseTimeStringToSeconds(videoStartText)
                                val endSec = TimeUtils.parseTimeStringToSeconds(videoEndText)
                                val updated = article.copy(
                                    url = url.trim(),
                                    title = title.trim(),
                                    thumbnailUrl = thumbnailUrl.trim(),
                                    summary = summary.trim(),
                                    notes = notesValue.text.trim(),
                                    hashtags = hashtags.map { it.trim() }.filter { it.isNotBlank() },
                                    linkedArticleIds = linkedIds,
                                    videoUrl = videoUrl.trim(),
                                    videoStartSeconds = startSec,
                                    videoEndSeconds = endSec,
                                    videoAutostart = videoAutostart,
                                    addendums = addendums,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_edit_button")
                        ) {
                            Text("Save Changes", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete '${article.title}' from your knowledge base?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(article)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLinkPickerDialog) {
        LinkPostPickerDialog(
            allArticles = allArticles,
            initialSelectedIds = linkedIds,
            currentArticleId = article.id,
            onDismiss = { showLinkPickerDialog = false },
            onSaveLinks = { selected ->
                linkedIds = selected
                showLinkPickerDialog = false
            }
        )
    }
}

