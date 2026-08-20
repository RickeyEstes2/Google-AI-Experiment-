package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.ui.components.AddArticleDialog
import com.example.ui.components.ArticleCard
import com.example.ui.components.ArticleReaderDialog
import com.example.ui.components.EditArticleDialog
import com.example.ui.components.LinkPostPickerDialog
import com.example.ui.viewmodel.LinkFilter
import com.example.ui.viewmodel.MastermindViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindMainScreen(
    viewModel: MastermindViewModel,
    modifier: Modifier = Modifier
) {
    val articles by viewModel.displayedArticles.collectAsState()
    val allArticlesList by viewModel.allArticles.collectAsState(initial = emptyList())
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedHashtag by viewModel.selectedHashtag.collectAsState()
    val availableHashtags by viewModel.allAvailableHashtags.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddDialogOpen by viewModel.isAddDialogOpen.collectAsState()
    val activeArticle by viewModel.activeArticle.collectAsState()
    val activeArticleLinkedPosts by viewModel.activeArticleLinkedPosts.collectAsState()
    val articleBackStack by viewModel.articleBackStack.collectAsState()
    val editingArticle by viewModel.editingArticle.collectAsState()
    val targetArticleForLinking by viewModel.targetArticleForLinking.collectAsState()
    val isLinkPickerOpen by viewModel.isLinkPickerOpen.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Live Clock State for Fixed Header (12-hour format, Day of the week, Day of the month, Month, Year)
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val timeFormatter = remember { SimpleDateFormat("hh:mm:ss a", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }

    val formattedTime = remember(currentTime) { timeFormatter.format(Date(currentTime)) }
    val formattedDate = remember(currentTime) { dateFormatter.format(Date(currentTime)) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // App Title Row
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
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Chrome Hub",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "${allArticlesList.size} Saved",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // FIXED HEADER: 12-Hour Time, Day of Week, Day of Month, Month, and Year
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Time (12-hour format)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Schedule,
                                    contentDescription = "Current Time",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            // Right: Day of week, Month, Day of month, Year
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarToday,
                                    contentDescription = "Current Date",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_link_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Link")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search title, notes, hashtags, comments...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_links_input")
            )

            // Filter Chips (All, Favorites) + Dynamic Hashtags
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == LinkFilter.ALL,
                        onClick = { viewModel.setFilter(LinkFilter.ALL) },
                        label = { Text("All Links") },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == LinkFilter.FAVORITES,
                        onClick = { viewModel.setFilter(LinkFilter.FAVORITES) },
                        label = { Text("Favorites") },
                        leadingIcon = {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                }

                // Dynamic Hashtag Chips
                items(availableHashtags) { tag ->
                    val isTagSelected = selectedHashtag.equals(tag, ignoreCase = true)
                    FilterChip(
                        selected = isTagSelected,
                        onClick = {
                            if (isTagSelected) {
                                viewModel.setHashtag(null)
                            } else {
                                viewModel.setHashtag(tag)
                            }
                        },
                        label = { Text(tag) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Tag, contentDescription = null, modifier = Modifier.size(14.dp))
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            // Active Hashtag Filter Banner
            AnimatedVisibility(visible = selectedHashtag != null) {
                selectedHashtag?.let { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Filtered by tag:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.clickable { viewModel.setHashtag(null) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear tag filter",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main List or Empty State
            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedHashtag != null) "No matching links found" else "No saved links yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "How to Share from Google Chrome:",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Text(
                                    text = "1. Open any website in Google Chrome.\n2. Tap the three dots (⋮) menu in Chrome.\n3. Tap Share... and choose Chrome Hub.\n4. Your link, summary, notes, preview image, and hashtags are saved!",
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.openAddDialog() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Link Manually")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(articles, key = { it.id }) { article ->
                        ArticleCard(
                            article = article,
                            onClick = { viewModel.openArticle(article) },
                            onEdit = { viewModel.openEditDialog(article) },
                            onToggleFavorite = { viewModel.toggleFavorite(article) },
                            onDelete = { viewModel.deleteLink(article) },
                            onHashtagClick = { tag -> viewModel.setHashtag(tag) }
                        )
                    }
                }
            }
        }
    }

    // Add Link Dialog
    if (isAddDialogOpen) {
        AddArticleDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { url, title, thumbnailUrl, summary, notes, hashtags ->
                viewModel.addNewLink(
                    url = url,
                    title = title,
                    thumbnailUrl = thumbnailUrl,
                    summary = summary,
                    notes = notes,
                    hashtags = hashtags
                )
            }
        )
    }

    // Edit Post Dialog
    editingArticle?.let { articleToEdit ->
        EditArticleDialog(
            article = articleToEdit,
            allArticles = allArticlesList,
            onDismiss = { viewModel.closeEditDialog() },
            onSave = { title, url, thumbnailUrl, summary, notes, hashtags, linkedPostIds ->
                viewModel.saveEditedArticle(
                    id = articleToEdit.id,
                    title = title,
                    url = url,
                    thumbnailUrl = thumbnailUrl,
                    summary = summary,
                    notes = notes,
                    hashtags = hashtags,
                    linkedPostIds = linkedPostIds
                )
            }
        )
    }

    // Link Post Picker Dialog
    if (isLinkPickerOpen && targetArticleForLinking != null) {
        val target = targetArticleForLinking!!
        LinkPostPickerDialog(
            currentArticleId = target.id,
            allArticles = allArticlesList,
            initialSelectedIds = target.linkedPostIds,
            onDismiss = { viewModel.closeLinkPicker() },
            onConfirmSelection = { newLinkedIds ->
                viewModel.updateLinkedPosts(target.id, newLinkedIds)
            }
        )
    }

    // Article Reader / Detail Dialog
    activeArticle?.let { article ->
        ArticleReaderDialog(
            article = article,
            linkedArticles = activeArticleLinkedPosts,
            backStackDepth = articleBackStack.size,
            onDismiss = { viewModel.closeArticle() },
            onNavigateBack = { viewModel.navigateBackInStack() },
            onNavigateToLinkedArticle = { linked -> viewModel.navigateToLinkedArticle(linked) },
            onOpenEditPost = { viewModel.openEditDialog(article) },
            onToggleFavorite = { viewModel.toggleFavorite(article) },
            onDelete = { viewModel.deleteLink(article) },
            onUpdateNotes = { newNotes ->
                viewModel.updateNotes(article.id, newNotes)
            },
            onUpdateHashtags = { newHashtags ->
                viewModel.updateHashtags(article.id, newHashtags)
            },
            onAddComment = { commentText ->
                viewModel.addCommentToActiveLink(commentText)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteCommentFromActiveLink(commentId)
            },
            onUpdateComment = { commentId, newText ->
                viewModel.updateCommentText(article.id, commentId, newText)
            },
            onHashtagClick = { tag ->
                viewModel.setHashtag(tag)
            }
        )
    }
}
