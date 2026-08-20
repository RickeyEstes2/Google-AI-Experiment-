package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Standard preset tags for quick 1-tap categorization
val POPULAR_TAG_PRESETS = listOf(
    "Work" to Color(0xFF3B82F6),
    "Research" to Color(0xFF8B5CF6),
    "Tech" to Color(0xFF10B981),
    "AI" to Color(0xFFEC4899),
    "ReadLater" to Color(0xFFF59E0B),
    "Ideas" to Color(0xFF06B6D4),
    "Design" to Color(0xFF6366F1),
    "Finance" to Color(0xFF14B8A6),
    "Reference" to Color(0xFF64748B),
    "Tutorial" to Color(0xFFE11D48)
)

/**
 * Reusable tag and label selector component for Add and Edit dialogs.
 */
@Composable
fun LabelTagPicker(
    selectedTags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    allAvailableTags: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var customTagInput by remember { mutableStateOf("") }
    var isInputExpanded by remember { mutableStateOf(false) }

    fun addTag(rawTag: String) {
        val clean = rawTag.trim().removePrefix("#")
        if (clean.isNotBlank()) {
            val formatted = "#$clean"
            if (!selectedTags.any { it.equals(formatted, ignoreCase = true) }) {
                onTagsChanged(selectedTags + formatted)
            }
        }
        customTagInput = ""
    }

    fun removeTag(tagToRemove: String) {
        onTagsChanged(selectedTags.filterNot { it.equals(tagToRemove, ignoreCase = true) })
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Label,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Custom Labels & Tags (${selectedTags.size})",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            TextButton(
                onClick = { isInputExpanded = !isInputExpanded },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (isInputExpanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isInputExpanded) "Done" else "+ Custom Label",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Custom Label Input Box
        AnimatedVisibility(
            visible = isInputExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        placeholder = { Text("Enter tag or label (e.g. project_x)", fontSize = 12.sp) },
                        leadingIcon = {
                            Text(
                                text = "#",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { addTag(customTagInput) }),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("custom_tag_text_input")
                    )

                    Button(
                        onClick = { addTag(customTagInput) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("add_custom_tag_button")
                    ) {
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Selected Active Tags Chips
        if (selectedTags.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                selectedTags.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove tag $tag",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .clickable { removeTag(tag) }
                                    .padding(2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Preset Suggestions & Existing Tags
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Quick Assign Presets & Suggestions:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Popular preset suggestions
                items(POPULAR_TAG_PRESETS) { (name, color) ->
                    val tagFormatted = "#$name"
                    val isAlreadySelected = selectedTags.any { it.equals(tagFormatted, ignoreCase = true) }

                    SuggestionChip(
                        onClick = {
                            if (isAlreadySelected) {
                                removeTag(tagFormatted)
                            } else {
                                addTag(name)
                            }
                        },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(
                                    text = if (isAlreadySelected) "✓ $name" else "+ $name",
                                    fontSize = 11.sp,
                                    fontWeight = if (isAlreadySelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isAlreadySelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                        )
                    )
                }

                // Known custom tags from library
                val unusedExisting = allAvailableTags.filterNot { avail ->
                    selectedTags.any { it.equals(avail, ignoreCase = true) } ||
                    POPULAR_TAG_PRESETS.any { "#${it.first}".equals(avail, ignoreCase = true) }
                }

                items(unusedExisting) { existingTag ->
                    SuggestionChip(
                        onClick = { addTag(existingTag) },
                        label = { Text("+ $existingTag", fontSize = 11.sp) },
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog to quickly assign/edit tags and custom labels for an article without opening full edit mode.
 */
@Composable
fun QuickTagAssignDialog(
    articleTitle: String,
    currentTags: List<String>,
    allAvailableTags: List<String>,
    onDismiss: () -> Unit,
    onSaveTags: (List<String>) -> Unit
) {
    var tagsState by remember { mutableStateOf(currentTags) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Label,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Assign Labels & Tags",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = articleTitle,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                LabelTagPicker(
                    selectedTags = tagsState,
                    onTagsChanged = { tagsState = it },
                    allAvailableTags = allAvailableTags
                )

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
                            onSaveTags(tagsState)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Tags")
                    }
                }
            }
        }
    }
}
