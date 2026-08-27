package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.MemoryEntity
import com.example.ui.theme.*
import com.example.util.TimeUtils

@Composable
fun MemoryCard(
    memory: MemoryEntity,
    onClick: () -> Unit,
    onToggleStar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddendumClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val categoryColor = AppCategory.entries.find { it.displayName == memory.appCategory }?.colorHex?.let { Color(it) } ?: SkyBlue500

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, if (memory.isStarred) Amber600.copy(alpha = 0.6f) else BorderDark),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("memory_card_${memory.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: App badge, Category, Time, Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // App Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor)
                            )
                            Text(
                                text = memory.appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }

                    // Source Type & Sentiment Badges
                    if (memory.sentiment == "ACTION_ITEM") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Amber600.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Amber600.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Action Item",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Amber600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else if (memory.sentiment == "IMPORTANT") {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Rose600.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, Rose600.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Important",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Rose600,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (memory.addendums.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SkyBlue600.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, SkyBlue600.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "+${memory.addendums.size} Notes",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SkyBlue500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Time & Star
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = TimeUtils.formatPostDateTime(memory.timestamp),
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (memory.isStarred) AppIcons.Star else AppIcons.StarBorder,
                            contentDescription = "Star",
                            tint = if (memory.isStarred) Amber600 else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Title
            if (memory.title.isNotBlank()) {
                Text(
                    text = memory.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Body Snippet
            Text(
                text = memory.text,
                fontSize = 13.5.sp,
                color = Slate200,
                lineHeight = 19.sp,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            // Tags
            if (memory.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    memory.tags.take(4).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CardElevatedDark,
                            border = BorderStroke(1.dp, BorderDark)
                        ) {
                            Text(
                                text = if (tag.startsWith("#")) tag else "#$tag",
                                fontSize = 11.sp,
                                color = SkyBlue500,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Footer actions bar
            HorizontalDivider(color = BorderDark.copy(alpha = 0.6f), thickness = 0.8.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy button
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Memory", memory.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(AppIcons.Copy, contentDescription = "Copy", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    // Share button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, memory.title)
                                putExtra(Intent.EXTRA_TEXT, "${memory.title}\n\n${memory.text}\n\n[Captured from ${memory.appName}]")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Memory"))
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(AppIcons.Share, contentDescription = "Share", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }

                    // Add Addendum button
                    IconButton(
                        onClick = onAddendumClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(AppIcons.Add, contentDescription = "Add Addendum", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(AppIcons.Edit, contentDescription = "Edit", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(AppIcons.Delete, contentDescription = "Delete", tint = Rose600.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
