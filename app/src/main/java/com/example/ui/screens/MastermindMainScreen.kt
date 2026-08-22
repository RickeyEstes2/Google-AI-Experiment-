package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Article
import com.example.ui.components.*
import com.example.ui.theme.*
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
    val pendingInitialData by viewModel.pendingInitialData.collectAsStateWithLifecycle()
    val linkedArticles by viewModel.linkedArticles.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var articleForQuickTag by remember { mutableStateOf<Article?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Live Clock State (Format: Thursday, August 20, 2026 · 8:31:25 PM - 12 hour, no leading 0)
    var currentTimeFormatted by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clockFormat = SimpleDateFormat("EEEE, MMMM d, yyyy · h:mm:ss a", Locale.getDefault())
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
                color = DeepDarkBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // App Bar Title
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // White Rounded Square Menu Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = AppIcons.Menu,
                                        contentDescription = "Menu",
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Database Mastermind",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = Color.White
                                )
                                Text(
                                    text = "Personal Knowledge Base & Notes",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = TextMuted
                                )
                            }
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
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = currentTimeFormatted,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMuted
                                )
                            )
                        }
                    }

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search titles, notes, tags, formulas...",
                                fontSize = 13.5.sp,
                                color = TextMuted
                            )
                        },
                        leadingIcon = {
                            Icon(
                                AppIcons.Search,
                                contentDescription = "Search",
                                tint = TextMuted,
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(AppIcons.Clear, contentDescription = "Clear search", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = CardSurfaceDark,
                            focusedContainerColor = CardSurfaceDark,
                            unfocusedBorderColor = BorderDark,
                            focusedBorderColor = SkyBlue500,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("main_search_bar")
                    )
                }
            }
        },
        floatingActionButton = {
            // White Pill Floating Action Button
            Surface(
                onClick = { viewModel.openAddDialog() },
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("add_article_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = AppIcons.Add,
                        contentDescription = "Add Note",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Add Note",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        },
        containerColor = DeepDarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Row 1
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(LinkFilter.values()) { filter ->
                    val isSelected = selectedFilter == filter
                    val labelText = when (filter) {
                        LinkFilter.ALL -> "All (${allArticles.size})"
                        LinkFilter.FAVORITES -> "★ Favorites (${allArticles.count { it.isFavorite }})"
                        LinkFilter.RECENT -> "Recent"
                        LinkFilter.HAS_NOTES -> "With Notes (${allArticles.count { it.notes.isNotBlank() }})"
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) CardElevatedDark else Color.Transparent,
                        border = BorderStroke(1.dp, if (isSelected) BorderDark else BorderDark),
                        modifier = Modifier.clickable { viewModel.setFilter(filter) }
                    ) {
                        Text(
                            text = labelText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else TextMuted
                            ),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Hashtags / Labels Filter Row 2
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
                            color = CardSurfaceDark,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = AppIcons.Label,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = Color.White
                                )
                                Text(
                                    text = "Tags:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    items(availableHashtags) { tag ->
                        val isSelected = selectedHashtag.equals(tag, ignoreCase = true)
                        val count = allArticles.count { it.hashtags.any { t -> t.equals(tag, ignoreCase = true) } }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) CardElevatedDark else CardSurfaceDark,
                            border = BorderStroke(1.dp, if (isSelected) SkyBlue500 else BorderDark),
                            modifier = Modifier.clickable { viewModel.setHashtag(tag) }
                        ) {
                            Text(
                                text = "$tag ($count)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextMuted
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = CardSurfaceDark,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = AppIcons.Search,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedHashtag != null) "No matching notes found" else "Your database is empty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedHashtag != null) "Try adjusting your search query or removing tag filters." else "Tap '+ Add Note' below to store your first link, article, or research note.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center
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

    // Quick Tag Edit Dialog
    articleForQuickTag?.let { article ->
        QuickTagAssignDialog(
            articleTitle = article.title,
            currentTags = article.hashtags,
            allAvailableTags = availableHashtags,
            onDismiss = { articleForQuickTag = null },
            onSaveTags = { updatedTags ->
                viewModel.updateHashtags(article.id, updatedTags)
                articleForQuickTag = null
            }
        )
    }

    // Full Reader Screen / Dialog
    readingArticle?.let { article ->
        ArticleReaderDialog(
            article = article,
            linkedArticles = linkedArticles,
            allAvailableTags = availableHashtags,
            onDismiss = { viewModel.closeReader() },
            onEdit = {
                viewModel.closeReader()
                viewModel.openEditDialog(article)
            },
            onToggleFavorite = { viewModel.toggleFavorite(article) },
            onDelete = {
                viewModel.deleteLink(article)
            },
            onUpdateHashtags = { updatedTags ->
                viewModel.updateHashtags(article.id, updatedTags)
            },
            onLinkedArticleClick = { linked ->
                viewModel.openReader(linked)
            },
            onHashtagClick = { tag ->
                viewModel.closeReader()
                viewModel.setHashtag(tag)
            }
        )
    }

    // Add Dialog
    if (showAddDialog) {
        AddArticleDialog(
            allArticles = allArticles,
            availableTags = availableHashtags,
            initialUrl = pendingInitialData?.url ?: "",
            initialTitle = pendingInitialData?.title ?: "",
            initialThumbnailUrl = pendingInitialData?.thumbnailUrl ?: "",
            initialSummary = pendingInitialData?.summary ?: "",
            initialNotes = pendingInitialData?.notes ?: "",
            initialHashtags = pendingInitialData?.hashtags ?: emptyList(),
            isFromShare = pendingInitialData != null,
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { url, title, thumb, sum, notes, tags, links, videoUrl, videoStart, videoEnd, videoAutostart, addendums ->
                viewModel.addLink(
                    url = url,
                    title = title,
                    thumbnailUrl = thumb,
                    summary = sum,
                    notes = notes,
                    hashtags = tags,
                    linkedIds = links,
                    videoUrl = videoUrl,
                    videoStartSeconds = videoStart,
                    videoEndSeconds = videoEnd,
                    videoAutostart = videoAutostart,
                    addendums = addendums
                )
                viewModel.closeAddDialog()
            }
        )
    }

    // Edit Dialog
    editingArticle?.let { article ->
        EditArticleDialog(
            article = article,
            allArticles = allArticles,
            availableTags = availableHashtags,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { updated ->
                viewModel.updateLink(updated)
                viewModel.closeEditDialog()
            },
            onDelete = { toDelete ->
                viewModel.deleteLink(toDelete)
                viewModel.closeEditDialog()
            }
        )
    }
}
