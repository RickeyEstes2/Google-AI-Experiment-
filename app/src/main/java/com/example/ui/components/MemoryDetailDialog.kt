package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppCategory
import com.example.data.model.MemoryEntity
import com.example.ui.theme.*
import com.example.util.TimeUtils

@Composable
fun MemoryDetailDialog(
    memory: MemoryEntity,
    onDismiss: () -> Unit,
    onToggleStar: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddAddendum: (String) -> Unit,
    onRemoveAddendum: (String) -> Unit
) {
    val context = LocalContext.current
    var newAddendumText by remember { mutableStateOf("") }
    var isAddingAddendum by remember { mutableStateOf(false) }

    val categoryColor = AppCategory.entries.find { it.displayName == memory.appCategory }?.colorHex?.let { Color(it) } ?: SkyBlue500

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("memory_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
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
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = categoryColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, categoryColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = memory.appName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CardElevatedDark,
                            border = BorderStroke(1.dp, BorderDark)
                        ) {
                            Text(
                                text = memory.appCategory,
                                fontSize = 11.5.sp,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleStar) {
                            Icon(
                                imageVector = if (memory.isStarred) AppIcons.Star else AppIcons.StarBorder,
                                contentDescription = "Star",
                                tint = if (memory.isStarred) Amber600 else TextMuted
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(AppIcons.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    if (memory.title.isNotBlank()) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 20.sp
                            )
                        )
                    }

                    // Metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🕒 ${TimeUtils.formatPostDateTime(memory.timestamp)}",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "📝 ${memory.wordCount} words",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "📱 ${memory.sourceType}",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }

                    // Main Text Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardElevatedDark,
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = memory.text,
                                fontSize = 14.5.sp,
                                color = TextLight,
                                lineHeight = 22.sp
                            )
                        }
                    }

                    // Tags
                    if (memory.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            memory.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Slate800,
                                    border = BorderStroke(1.dp, BorderDark)
                                ) {
                                    Text(
                                        text = if (tag.startsWith("#")) tag else "#$tag",
                                        fontSize = 12.sp,
                                        color = SkyBlue500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Addendums Section
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardElevatedDark.copy(alpha = 0.6f)),
                        border = BorderStroke(1.dp, BorderDark),
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
                                    text = "Addendums & Notes (${memory.addendums.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                TextButton(
                                    onClick = { isAddingAddendum = !isAddingAddendum },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(if (isAddingAddendum) "Cancel" else "+ Add Note", fontSize = 12.sp, color = SkyBlue500)
                                }
                            }

                            if (memory.addendums.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    memory.addendums.forEachIndexed { index, addendum ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CardSurfaceDark,
                                            border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Addendum #${index + 1} · ${TimeUtils.formatPostDateTime(addendum.timestamp)}",
                                                        fontSize = 10.5.sp,
                                                        color = SkyBlue500,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = addendum.content,
                                                        fontSize = 13.sp,
                                                        color = TextLight
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { onRemoveAddendum(addendum.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(AppIcons.Close, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(14.dp))
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
                                    placeholder = { Text("Write addendum or update note...", fontSize = 13.sp) },
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
                                                onAddAddendum(newAddendumText.trim())
                                                newAddendumText = ""
                                                isAddingAddendum = false
                                            }
                                        },
                                        enabled = newAddendumText.isNotBlank(),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Append Addendum", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Memory", memory.text))
                                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Copy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy", fontSize = 12.5.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, memory.title)
                                    putExtra(Intent.EXTRA_TEXT, "${memory.title}\n\n${memory.text}\n\n[Captured from ${memory.appName}]")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Memory"))
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.5.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = onEdit,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit", fontSize = 12.5.sp)
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Rose600)
                        ) {
                            Icon(AppIcons.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
