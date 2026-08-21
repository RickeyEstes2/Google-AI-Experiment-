package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.Article
import com.example.ui.theme.AppIcons
import java.text.SimpleDateFormat
import java.util.*

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
    onHashtagClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
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
                    // Optional Thumbnail Image
                    if (article.thumbnailUrl.isNotBlank()) {
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

                    // Meta: Domain & Timestamp
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
                                text = article.domain,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = dateFormat.format(Date(article.updatedAt)),
                            style = MaterialTheme.typography.labelSmall,
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
