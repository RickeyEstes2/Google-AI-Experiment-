package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Article
import com.example.ui.components.*
import com.example.ui.theme.AppIcons
import com.example.ui.viewmodel.LinkFilter
import com.example.ui.viewmodel.MastermindViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindMainScreen(
    viewModel: MastermindViewModel = viewModel()
) {
    val allArticles by viewModel.allArticles.collectAsStateWithLifecycle()
    val filteredArticles by viewModel.filteredArticles.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val selectedHashtag by viewModel.selectedHashtag.collectAsStateWithLifecycle()
    val availableHashtags by viewModel.availableHashtags.collectAsStateWithLifecycle()

    val readingArticle by viewModel.readingArticle.collectAsStateWithLifecycle()
    val editingArticle by viewModel.editingArticle.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val linkedArticles by viewModel.linkedArticles.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var articleForQuickTag by remember { mutableStateOf<Article?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Live Clock State (12-hour format, Day of the week, Day of the month, Month, Year)
    var currentTimeFormatted by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clockFormat = SimpleDateFormat("EEEE, MMMM d, yyyy · hh:mm:ss a", Locale.getDefault())
        while (true) {
            currentTimeFormatted = clockFormat.format(Date())
            delay(1000)
        }
    }

    // Snackbar Trigger
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // App Bar Title & Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Storage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Database Mastermind",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Personal Knowledge Base & Notes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Total count chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "${allArticles.size} Notes",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Live Clock Bar
                    if (currentTimeFormatted.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = currentTimeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text("Search titles, notes, tags, math formulas...", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(AppIcons.Search, contentDescription = "Search", modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(AppIcons.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("main_search_bar")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_article_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(AppIcons.Add, contentDescription = "Add Note")
                    Text("Add Note", fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(LinkFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                text = when (filter) {
                                    LinkFilter.ALL -> "All (${allArticles.size})"
                                    LinkFilter.FAVORITES -> "★ Favorites (${allArticles.count { it.isFavorite }})"
                                    LinkFilter.RECENT -> "Recent"
                                    LinkFilter.HAS_NOTES -> "With Notes (${allArticles.count { it.notes.isNotBlank() }})"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // Hashtag & Label Filter Row
            if (availableHashtags.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(AppIcons.Label, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("Tags:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(availableHashtags) { tag ->
                        val isSelected = selectedHashtag.equals(tag, ignoreCase = true)
                        val count = allArticles.count { it.hashtags.any { t -> t.equals(tag, ignoreCase = true) } }

                        SuggestionChip(
                            onClick = { viewModel.setHashtag(tag) },
                            label = {
                                Text(
                                    text = "$tag ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Feed / Empty State
            if (filteredArticles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedHashtag != null) "No matching notes found" else "Your database is empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedHashtag != null) "Try adjusting your search query or removing tag filters." else "Tap '+ Add Note' below to store your first link, article, or research note.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredArticles, key = { it.id }) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { viewModel.openReader(article) },
                            onEdit = { viewModel.openEditDialog(article) },
                            onToggleFavorite = { viewModel.toggleFavorite(article) },
                            onDelete = { viewModel.deleteLink(article) },
                            onHashtagClick = { tag -> viewModel.setHashtag(tag) },
                            onQuickEditTags = { articleForQuickTag = article }
                        )
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showAddDialog) {
        AddArticleDialog(
            allArticles = allArticles,
            availableTags = availableHashtags,
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { url, title, thumb, sum, notes, tags, links ->
                viewModel.addLink(url, title, thumb, sum, notes, tags, links)
            }
        )
    }

    editingArticle?.let { articleToEdit ->
        EditArticleDialog(
            article = articleToEdit,
            allArticles = allArticles,
            availableTags = availableHashtags,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { updated -> viewModel.updateLink(updated) },
            onDelete = { toDelete -> viewModel.deleteLink(toDelete) }
        )
    }

    readingArticle?.let { articleToRead ->
        ArticleReaderDialog(
            article = articleToRead,
            linkedArticles = linkedArticles,
            allAvailableTags = availableHashtags,
            onDismiss = { viewModel.closeReader() },
            onEdit = {
                viewModel.closeReader()
                viewModel.openEditDialog(articleToRead)
            },
            onToggleFavorite = { viewModel.toggleFavorite(articleToRead) },
            onDelete = {
                viewModel.deleteLink(articleToRead)
            },
            onUpdateHashtags = { newTags ->
                viewModel.updateHashtags(articleToRead.id, newTags)
            },
            onLinkedArticleClick = { clicked ->
                viewModel.openReader(clicked)
            },
            onHashtagClick = { tag ->
                viewModel.setHashtag(tag)
                viewModel.closeReader()
            }
        )
    }

    // Quick Tag & Label Assign Dialog
    articleForQuickTag?.let { targetArticle ->
        QuickTagAssignDialog(
            articleTitle = targetArticle.title,
            currentTags = targetArticle.hashtags,
            allAvailableTags = availableHashtags,
            onDismiss = { articleForQuickTag = null },
            onSaveTags = { newTags ->
                viewModel.updateHashtags(targetArticle.id, newTags)
                articleForQuickTag = null
            }
        )
    }
}
