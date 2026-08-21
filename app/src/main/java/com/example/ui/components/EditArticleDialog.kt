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
    var notes by remember { mutableStateOf(article.notes) }
    var hashtags by remember { mutableStateOf(article.hashtags) }
    var linkedIds by remember { mutableStateOf(article.linkedArticleIds) }

    var showLinkPickerDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.9f)
                .padding(vertical = 12.dp)
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
                        text = "Edit Note",
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

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Summary") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Markdown / Math") },
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("edit_notes_input")
                    )

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
                                val updated = article.copy(
                                    url = url.trim(),
                                    title = title.trim(),
                                    thumbnailUrl = thumbnailUrl.trim(),
                                    summary = summary.trim(),
                                    notes = notes.trim(),
                                    hashtags = hashtags,
                                    linkedArticleIds = linkedIds,
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(updated)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_edit_button")
                        ) {
                            Text("Save Changes")
                        }
                    }
                }
            }
        }
    }

    if (showLinkPickerDialog) {
        LinkPostPickerDialog(
            currentArticleId = article.id,
            allArticles = allArticles,
            initialSelectedIds = linkedIds,
            onDismiss = { showLinkPickerDialog = false },
            onSaveLinks = { linkedIds = it }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete this Note?") },
            text = { Text("This will permanently remove '${article.title}' from your local database.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(article)
                        showDeleteConfirm = false
                        onDismiss()
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
}
