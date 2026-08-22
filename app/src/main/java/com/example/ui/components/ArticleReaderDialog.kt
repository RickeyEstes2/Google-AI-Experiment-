package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Article
import com.example.ui.theme.AppIcons
import com.example.util.TimeUtils

@Composable
fun ArticleReaderDialog(
    article: Article,
    linkedArticles: List<Article> = emptyList(),
    allAvailableTags: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUpdateHashtags: (List<String>) -> Unit,
    onLinkedArticleClick: (Article) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onAddAddendum: (articleId: Long, text: String) -> Unit = { _, _ -> },
    onRemoveAddendum: (articleId: Long, addendumId: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var newAddendumText by remember { mutableStateOf("") }
    var isAddingAddendum by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(vertical = 8.dp)
                .testTag("article_reader_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(AppIcons.Close, contentDescription = "Close")
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                imageVector = if (article.isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (article.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onEdit) {
                            Icon(AppIcons.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (article.url.startsWith("http")) {
                            IconButton(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            }) {
                                Icon(AppIcons.OpenInNew, contentDescription = "Open URL in browser", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Video Player Component (YouTube or Local Video)
                    if (article.videoUrl.isNotBlank()) {
                        MastermindVideoPlayer(
                            videoUrl = article.videoUrl,
                            startSeconds = article.videoStartSeconds,
                            endSeconds = article.videoEndSeconds,
                            autostart = article.videoAutostart,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    } else if (article.thumbnailUrl.isNotBlank()) {
                        // Optional Thumbnail Image if no video
                        AsyncImage(
                            model = article.thumbnailUrl,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    // Title
                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Meta: Domain & 12-hour formatted post timestamp (e.g. 8:00 AM)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = article.domain.ifBlank { "local.note" },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // 12-hour format: e.g. "Aug 20, 2026 · 8:00 AM"
                        Text(
                            text = TimeUtils.formatPostDateTime(article.updatedAt),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Hyperlink Box if valid web URL
                    if (article.url.startsWith("http")) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(AppIcons.Link, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                HyperlinkText(text = article.url, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Custom Labels & Tags Section
                    LabelTagPicker(
                        selectedTags = article.hashtags,
                        onTagsChanged = { onUpdateHashtags(it) },
                        allAvailableTags = allAvailableTags
                    )

                    // Rich Content Renderer
                    RichArticleContent(
                        article = article,
                        linkedArticles = linkedArticles,
                        onLinkedArticleClick = onLinkedArticleClick
                    )

                    // Addendums Section
                    Divider(modifier = Modifier.padding(top = 10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📝 Addendums (${article.addendums.size})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!isAddingAddendum) {
                            FilledTonalButton(
                                onClick = { isAddingAddendum = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("+ Add Addendum", fontSize = 12.sp)
                            }
                        }
                    }

                    // Render existing Addendums in chronological order
                    if (article.addendums.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            article.addendums.forEachIndexed { index, addendum ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Addendum #${index + 1} · ${TimeUtils.formatPostDateTime(addendum.timestamp)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(
                                                onClick = { onRemoveAddendum(article.id, addendum.id) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    AppIcons.Close,
                                                    contentDescription = "Remove Addendum",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        RenderFormattedMarkdown(content = addendum.content)
                                    }
                                }
                            }
                        }
                    }

                    // Add Addendum Input Area
                    if (isAddingAddendum) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Append New Addendum",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                OutlinedTextField(
                                    value = newAddendumText,
                                    onValueChange = { newAddendumText = it },
                                    placeholder = { Text("Write update or continuation note (supports Markdown & links)...") },
                                    minLines = 2,
                                    maxLines = 6,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = {
                                        isAddingAddendum = false
                                        newAddendumText = ""
                                    }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (newAddendumText.isNotBlank()) {
                                                onAddAddendum(article.id, newAddendumText.trim())
                                                newAddendumText = ""
                                                isAddingAddendum = false
                                            }
                                        },
                                        enabled = newAddendumText.isNotBlank(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Save Addendum", fontSize = 12.5.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Done Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Note")
                }
            }
        }
    }
}

