package com.example.solveflow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewKnowledgeDialog(
    onDismiss: () -> Unit,
    onAddKnowledge: (title: String, topic: String, languageScope: String, content: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("Architecture") }
    var languageScope by remember { mutableStateOf("All") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val topics = listOf("Architecture", "Best Practices", "Security & Validation", "Error Prevention", "Performance")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Retrievable Knowledge",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it; errorMessage = null },
                        label = { Text("Topic / Rule Title *") },
                        placeholder = { Text("e.g., Structured Concurrency & Cancellation Propagation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Topic Chips
                    Column {
                        Text("Category / Topic:", style = MaterialTheme.typography.labelMedium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            topics.take(3).forEach { t ->
                                FilterChip(
                                    selected = topic == t,
                                    onClick = { topic = t },
                                    label = { Text(t, fontSize = 11.sp) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            topics.drop(3).forEach { t ->
                                FilterChip(
                                    selected = topic == t,
                                    onClick = { topic = t },
                                    label = { Text(t, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = languageScope,
                        onValueChange = { languageScope = it },
                        label = { Text("Language Scope") },
                        placeholder = { Text("All, or specific like kotlin, python, rust") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it; errorMessage = null },
                        label = { Text("Retrievable Information & Guidelines *") },
                        placeholder = { Text("Describe the architecture patterns, constraints, error mitigations...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (Comma-separated)") },
                        placeholder = { Text("concurrency, coroutines, async, clean-code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() || content.isBlank()) {
                                errorMessage = "Title and content guidelines are required."
                            } else {
                                onAddKnowledge(
                                    title.trim(),
                                    topic,
                                    if (languageScope.isBlank()) "All" else languageScope.trim(),
                                    content.trim(),
                                    tags.trim()
                                )
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Knowledge")
                    }
                }
            }
        }
    }
}
