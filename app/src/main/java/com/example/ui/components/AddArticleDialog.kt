package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Article
import com.example.ui.theme.AppIcons

@Composable
fun AddArticleDialog(
    allArticles: List<Article>,
    availableTags: List<String>,
    initialUrl: String = "",
    initialTitle: String = "",
    initialNotes: String = "",
    initialHashtags: List<String> = emptyList(),
    isFromShare: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (url: String, title: String, thumbnailUrl: String, summary: String, notes: String, hashtags: List<String>, linkedIds: List<Long>) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var title by remember { mutableStateOf(initialTitle) }
    var thumbnailUrl by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(initialNotes) }
    var hashtags by remember { mutableStateOf(initialHashtags) }
    var linkedIds by remember { mutableStateOf(listOf<Long>()) }

    var showLinkPickerDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp)
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
                        label = { Text("URL / Link Address *") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_url_input")
                    )

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
                        label = { Text("Thumbnail Image URL (Optional)") },
                        placeholder = { Text("https://image.url/photo.jpg") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Personal Notes / Markdown / Math Formulas") },
                        placeholder = { Text("Write in markdown, code snippets, or latex formulas ($$ ... $$)...") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("add_notes_input")
                    )

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
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (url.isNotBlank() || title.isNotBlank()) {
                                onSave(url, title, thumbnailUrl, summary, notes, hashtags, linkedIds)
                                onDismiss()
                            }
                        },
                        enabled = url.isNotBlank() || title.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_article_button")
                    ) {
                        Text("Save Note")
                    }
                }
            }
        }
    }

    if (showLinkPickerDialog) {
        LinkPostPickerDialog(
            currentArticleId = 0L,
            allArticles = allArticles,
            initialSelectedIds = linkedIds,
            onDismiss = { showLinkPickerDialog = false },
            onSaveLinks = { linkedIds = it }
        )
    }
}
