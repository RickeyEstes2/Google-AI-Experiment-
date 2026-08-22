package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Article
import com.example.ui.theme.*
import com.example.util.TimeUtils

@Composable
fun ArticleCard(
    article: Article,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onHashtagClick: (String) -> Unit,
    onQuickEditTags: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("article_card_${article.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Thumbnail image preview banner
            if (article.thumbnailUrl.isNotBlank()) {
                AsyncImage(
                    model = article.thumbnailUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            // Domain Badge, Post Time (12-hour format e.g. 8:00 AM), & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E2A3F),
                        border = BorderStroke(0.5.dp, BorderDark)
                    ) {
                        Text(
                            text = article.domain.ifBlank { "note" },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.5.sp),
                            color = TextMuted,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    if (article.videoUrl.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF991B1B).copy(alpha = 0.3f),
                            border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text("▶ Video", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFCA5A5))
                                if (article.videoStartSeconds > 0 || article.videoEndSeconds > 0) {
                                    val startStr = TimeUtils.parseSecondsToTimeString(article.videoStartSeconds)
                                    val endStr = if (article.videoEndSeconds > 0) TimeUtils.parseSecondsToTimeString(article.videoEndSeconds) else "end"
                                    Text("($startStr - $endStr)", fontSize = 9.5.sp, color = Color(0xFFFCA5A5))
                                }
                            }
                        }
                    }

                    // 12-Hour formatted post time (e.g. 8:00 AM)
                    Text(
                        text = TimeUtils.formatPostDateTime(article.updatedAt),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Quick tag assign
                    IconButton(
                        onClick = onQuickEditTags,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Label,
                            contentDescription = "Assign Tags",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Edit,
                            contentDescription = "Edit",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Favorite
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (article.isFavorite) AppIcons.Favorite else AppIcons.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (article.isFavorite) Rose600 else TextMuted,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.2).sp
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Summary snippet or notes snippet
            val previewText = article.summary.ifBlank { article.notes.take(150) }
            if (previewText.isNotBlank()) {
                Text(
                    text = previewText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = TextMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Hashtags / Label Chips
            if (article.hashtags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(article.hashtags) { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardElevatedDark,
                            border = BorderStroke(0.8.dp, BorderDark),
                            modifier = Modifier.clickable { onHashtagClick(tag) }
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextLight,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp)
                            )
                        }
                    }
                }
            }

            // Badges row: Connected links & Addendums count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (article.linkedArticleIds.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Link,
                            contentDescription = null,
                            tint = SkyBlue500,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${article.linkedArticleIds.size} connected note${if (article.linkedArticleIds.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = SkyBlue500
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (article.addendums.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF065F46).copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "+${article.addendums.size} addendum${if (article.addendums.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.5.sp,
                                color = Color(0xFF6EE7B7)
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
