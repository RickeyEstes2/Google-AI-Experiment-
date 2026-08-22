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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay

@Composable
fun AddArticleDialog(
    allArticles: List<Article>,
    availableTags: List<String>,
    initialUrl: String = "",
    initialTitle: String = "",
    initialThumbnailUrl: String = "",
    initialSummary: String = "",
    initialNotes: String = "",
    initialHashtags: List<String> = emptyList(),
    initialVideoUrl: String = "",
    initialVideoStartSeconds: Int = 0,
    initialVideoEndSeconds: Int = 0,
    initialVideoAutostart: Boolean = false,
    isFromShare: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (
        url: String,
        title: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedIds: List<Long>,
        videoUrl: String,
        videoStartSeconds: Int,
        videoEndSeconds: Int,
        videoAutostart: Boolean,
        addendums: List<Addendum>
    ) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var title by remember { mutableStateOf(initialTitle) }
    var thumbnailUrl by remember { mutableStateOf(initialThumbnailUrl) }
    var summary by remember { mutableStateOf(initialSummary) }
    var notesValue by remember { mutableStateOf(TextFieldValue(initialNotes, TextRange(initialNotes.length))) }
    var hashtags by remember { mutableStateOf(initialHashtags) }
    var linkedIds by remember { mutableStateOf(listOf<Long>()) }

    // Video properties
    var videoUrl by remember { mutableStateOf(initialVideoUrl) }
    var videoStartText by remember { mutableStateOf(if (initialVideoStartSeconds > 0) TimeUtils.parseSecondsToTimeString(initialVideoStartSeconds) else "") }
    var videoEndText by remember { mutableStateOf(if (initialVideoEndSeconds > 0) TimeUtils.parseSecondsToTimeString(initialVideoEndSeconds) else "") }
    var videoAutostart by remember { mutableStateOf(initialVideoAutostart) }

    // Addendums
    var addendums by remember { mutableStateOf(listOf<Addendum>()) }
    var newAddendumText by remember { mutableStateOf("") }
    var isAddingAddendum by remember { mutableStateOf(false) }

    var isFetchingMetadata by remember { mutableStateOf(false) }
    var showLinkPickerDialog by remember { mutableStateOf(false) }
    var showNotesPreview by remember { mutableStateOf(false) }

    // Auto-fetch image and metadata when URL is entered
    LaunchedEffect(url) {
        val trimmed = url.trim()
        if (trimmed.length > 8 && (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.contains("."))) {
            delay(500) // debounce
            isFetchingMetadata = true
            try {
                // If YouTube link, automatically set video URL
                if (trimmed.contains("youtube.com") || trimmed.contains("youtu.be")) {
                    if (videoUrl.isBlank()) {
                        videoUrl = trimmed
                    }
                }
                val meta = LinkMetadataFetcher.fetchMetadata(trimmed)
                if (thumbnailUrl.isBlank() && !meta.imageUrl.isNullOrBlank()) {
                    thumbnailUrl = meta.imageUrl
                }
                if (title.isBlank() && !meta.title.isNullOrBlank()) {
                    title = meta.title
                }
                if (summary.isBlank() && !meta.description.isNullOrBlank()) {
                    summary = meta.description
                }
            } catch (_: Exception) {} finally {
                isFetchingMetadata = false
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
                .testTag("add_article_dialog")
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
                    Column {
                        Text(
                            text = if (isFromShare) "Save Shared Link" else "Add to Database",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        if (isFromShare) {
                            Text(
                                text = "Shared from Chrome / Browser",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
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
                        onValueChange = {
                            url = it
                            if (title.isBlank() && it.isNotBlank()) {
                                try {
                                    val uri = java.net.URI(it)
                                    val host = uri.host?.removePrefix("www.") ?: ""
                                    if (host.isNotBlank()) {
                                        title = host.replaceFirstChar { char -> char.uppercase() }
                                    }
                                } catch (_: Exception) {}
                            }
                        },
                        label = { Text("URL / Link Address (Optional for notes)") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_url_input")
                    )

                    // Auto-fetching image status indicator
                    if (isFetchingMetadata) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Auto-detecting image & metadata...",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            }
                        }
                    }

                    // Image Preview if available
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
                                Text(
                                    text = "Auto-Attached Image",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        placeholder = { Text("Enter a descriptive title") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_title_input")
                    )

                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("Thumbnail Image URL (Auto-detected)") },
                        placeholder = { Text("https://image.url/photo.jpg") },
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
                        label = { Text("Quick Summary / Key Takeaway") },
                        placeholder = { Text("A brief overview of the article...") },
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
                                modifier = Modifier.fillMaxWidth().testTag("add_notes_input")
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

                    // Custom Labels & Tag Picker
                    LabelTagPicker(
                        selectedTags = hashtags,
                        onTagsChanged = { hashtags = it },
                        allAvailableTags = availableTags
                    )

                    // Connected Links Picker Trigger
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
                            Text("Select Related Notes", fontSize = 12.sp)
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() || url.isNotBlank() || videoUrl.isNotBlank() || notesValue.text.isNotBlank()) {
                                val startSec = TimeUtils.parseTimeStringToSeconds(videoStartText)
                                val endSec = TimeUtils.parseTimeStringToSeconds(videoEndText)
                                onSave(
                                    url,
                                    title.ifBlank { "Untitled Note" },
                                    thumbnailUrl,
                                    summary,
                                    notesValue.text,
                                    hashtags,
                                    linkedIds,
                                    videoUrl,
                                    startSec,
                                    endSec,
                                    videoAutostart,
                                    addendums
                                )
                            }
                        },
                        enabled = title.isNotBlank() || url.isNotBlank() || videoUrl.isNotBlank() || notesValue.text.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_article_button")
                    ) {
                        Text("Save Note", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showLinkPickerDialog) {
        LinkPostPickerDialog(
            allArticles = allArticles,
            initialSelectedIds = linkedIds,
            currentArticleId = null,
            onDismiss = { showLinkPickerDialog = false },
            onSaveLinks = { selected ->
                linkedIds = selected
                showLinkPickerDialog = false
            }
        )
    }
}

