package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AppCategory
import com.example.data.model.MemoryEntity
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMemoryDialog(
    memoryToEdit: MemoryEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        text: String,
        appName: String,
        packageName: String,
        category: String,
        title: String,
        tags: List<String>,
        sourceType: String,
        sentiment: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(memoryToEdit?.title ?: "") }
    var text by remember { mutableStateOf(memoryToEdit?.text ?: "") }
    var appName by remember { mutableStateOf(memoryToEdit?.appName ?: "Manual Note") }
    var selectedCategory by remember { mutableStateOf(memoryToEdit?.appCategory ?: "Notes & General") }
    var selectedSentiment by remember { mutableStateOf(memoryToEdit?.sentiment ?: "NEUTRAL") }
    var tagsString by remember { mutableStateOf(memoryToEdit?.tags?.joinToString(", ") ?: "") }

    val isEditing = memoryToEdit != null

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
                .testTag("add_edit_memory_dialog")
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
                    Text(
                        text = if (isEditing) "Edit Memory" else "Record New Memory",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(AppIcons.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Summary") },
                        placeholder = { Text("e.g. Q3 Roadmap Review or Book Note") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = appName,
                            onValueChange = { appName = it },
                            label = { Text("Source App") },
                            placeholder = { Text("e.g. Slack, Chrome, Kindle") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category Selector
                    Text("Category:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppCategory.entries.forEach { cat ->
                            val isSelected = selectedCategory == cat.displayName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat.displayName },
                                label = { Text(cat.displayName, fontSize = 11.5.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Sentiment / Importance
                    Text("Importance & Sentiment:", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "NEUTRAL" to "Neutral",
                            "ACTION_ITEM" to "Action Item 📌",
                            "IMPORTANT" to "Important ⭐",
                            "IDEA" to "Idea 💡",
                            "POSITIVE" to "Positive ✨"
                        ).forEach { (key, label) ->
                            val isSelected = selectedSentiment == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSentiment = key },
                                label = { Text(label, fontSize = 11.5.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    // Full Text
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Text Content (Remembered Text) *") },
                        placeholder = { Text("Write or paste the text across apps you want to remember...") },
                        minLines = 6,
                        maxLines = 14,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("memory_text_input")
                    )

                    // Tags
                    OutlinedTextField(
                        value = tagsString,
                        onValueChange = { tagsString = it },
                        label = { Text("Tags (comma separated)") },
                        placeholder = { Text("e.g. work, research, urgent, recipe") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer Save / Cancel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                val tagList = tagsString.split(",")
                                    .map { it.trim().removePrefix("#") }
                                    .filter { it.isNotBlank() }

                                onSave(
                                    memoryToEdit?.id ?: 0L,
                                    text.trim(),
                                    appName.trim().ifBlank { "Manual Note" },
                                    memoryToEdit?.packageName ?: "",
                                    selectedCategory,
                                    title.trim(),
                                    tagList,
                                    memoryToEdit?.sourceType ?: "MANUAL",
                                    selectedSentiment
                                )
                            }
                        },
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_memory_button")
                    ) {
                        Text(if (isEditing) "Save Changes" else "Save Memory")
                    }
                }
            }
        }
    }
}
