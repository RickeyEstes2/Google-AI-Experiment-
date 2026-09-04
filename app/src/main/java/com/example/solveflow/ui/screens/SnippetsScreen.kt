package com.example.solveflow.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.data.model.CodeSnippet
import com.example.solveflow.engine.syntax.SyntaxHighlighterEngine
import com.example.solveflow.ui.components.EditSnippetDialog
import com.example.solveflow.ui.viewmodel.CodeGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnippetsScreen(
    viewModel: CodeGenViewModel,
    modifier: Modifier = Modifier
) {
    val snippets by viewModel.snippets.collectAsState()
    val languages by viewModel.languages.collectAsState()
    var selectedLanguageFilter by remember { mutableStateOf<String?>("All") }
    var searchQuery by remember { mutableStateOf("") }
    var editingSnippet by remember { mutableStateOf<CodeSnippet?>(null) }

    val filteredSnippets = remember(snippets, selectedLanguageFilter, searchQuery) {
        snippets.filter { snippet ->
            val matchesLang = selectedLanguageFilter == "All" || snippet.languageId.equals(selectedLanguageFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    snippet.title.contains(searchQuery, ignoreCase = true) ||
                    snippet.tags.contains(searchQuery, ignoreCase = true) ||
                    snippet.category.contains(searchQuery, ignoreCase = true)
            matchesLang && matchesQuery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reusable Code Snippets",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${snippets.size} snippet(s) available • DBSCAN clustered",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { viewModel.openNewSnippetDialog() },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Snippet", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by title, tag, or category...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Language Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedLanguageFilter == "All",
                    onClick = { selectedLanguageFilter = "All" },
                    label = { Text("All (${snippets.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                languages.forEach { lang ->
                    val count = snippets.count { it.languageId.equals(lang.id, ignoreCase = true) }
                    FilterChip(
                        selected = selectedLanguageFilter == lang.id,
                        onClick = { selectedLanguageFilter = lang.id },
                        label = { Text("${lang.name} ($count)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (filteredSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No snippets match '$searchQuery'" else "No snippets found in this category.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { viewModel.openNewSnippetDialog() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First Snippet")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSnippets, key = { it.id }) { snippet ->
                        SnippetItemCard(
                            snippet = snippet,
                            onUseInPrompt = {
                                viewModel.setPromptText("Based on this pattern: ${snippet.title}")
                                languages.find { it.id == snippet.languageId }?.let { viewModel.setSelectedLanguage(it) }
                                viewModel.setActiveTab(com.example.solveflow.ui.viewmodel.CodeGenTab.GENERATE)
                            },
                            onEdit = {
                                editingSnippet = snippet
                            }
                        )
                    }
                }
            }
        }
    }

    editingSnippet?.let { snippetToEdit ->
        EditSnippetDialog(
            snippet = snippetToEdit,
            languages = languages,
            onDismiss = { editingSnippet = null },
            onSave = { updated ->
                viewModel.updateSnippet(updated)
                editingSnippet = null
            },
            onDelete = { toDelete ->
                viewModel.deleteSnippet(toDelete)
                editingSnippet = null
            }
        )
    }
}

@Composable
fun SnippetItemCard(
    snippet: CodeSnippet,
    onUseInPrompt: () -> Unit,
    onEdit: () -> Unit
) {
    val previewCode = remember(snippet.code, snippet.languageId) {
        val truncated = snippet.code.lines().take(6).joinToString("\n")
        SyntaxHighlighterEngine.highlight(truncated, snippet.languageId, isDark = true)
    }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (snippet.isFineTunedExemplar) Icons.Default.Star else Icons.Default.DataObject,
                        contentDescription = null,
                        tint = if (snippet.isFineTunedExemplar) Color(0xFFEAB308) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = snippet.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = snippet.languageId.uppercase(),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (snippet.isFineTunedExemplar) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Golden Fine-Tuned Exemplar (Priority DBSCAN Vector)",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code Preview with Syntax Highlighting
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = previewCode,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tags and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tags: ${snippet.tags}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Snippet in Highlighter Editor",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    TextButton(onClick = onUseInPrompt) {
                        Text("Generate with this", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
