package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.Article
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.ui.components.*
import com.example.ui.viewmodel.MastermindViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastermindMainScreen(
    viewModel: MastermindViewModel,
    modifier: Modifier = Modifier
) {
    // ViewModel States
    val timeString by viewModel.currentTime.collectAsState()
    val dateString by viewModel.currentDate.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedHashtag by viewModel.selectedHashtag.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val articles by viewModel.filteredArticles.collectAsState()
    val categoriesFromDb by viewModel.categories.collectAsState(initial = emptyList())

    val activeArticle by viewModel.activeArticle.collectAsState()
    val isAddDialogOpen by viewModel.isAddArticleDialogOpen.collectAsState()
    val isNlpModalOpen by viewModel.isNlpModalOpen.collectAsState()
    val nlpAnalysis by viewModel.nlpAnalysis.collectAsState()
    val nlpTargetTitle by viewModel.nlpTargetTitle.collectAsState()

    val isEquationModalOpen by viewModel.isEquationModalOpen.collectAsState()
    val activeEquations by viewModel.activeEquations.collectAsState()
    val selectedEquationForSolve by viewModel.selectedEquationForSolve.collectAsState()
    val equationSolutionText by viewModel.equationSolutionText.collectAsState()
    val isEquationSolving by viewModel.isEquationSolving.collectAsState()

    val isChatPanelOpen by viewModel.isChatPanelOpen.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatThinking by viewModel.isChatThinking.collectAsState()

    val isDriveSyncOpen by viewModel.isDriveSyncModalOpen.collectAsState()
    val driveSyncState by viewModel.driveSyncState.collectAsState()
    val syncMessage by viewModel.syncMessage.collectAsState()

    val defaultCategories = listOf("All", "Science & Physics", "Linguistics & NLP", "Tech & AI", "Mathematics", "Philosophy")
    val allCategoryOptions = remember(categoriesFromDb) {
        (listOf("All") + defaultCategories.drop(1) + categoriesFromDb).distinct()
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(syncMessage) {
        syncMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSyncMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Fixed Top Header with live 12-hour clock, day, date, month, year & modal triggers
            FixedTopHeader(
                timeString = timeString,
                dateString = dateString,
                onOpenNlp = { viewModel.openNlpModal() },
                onOpenEquations = { viewModel.openEquationModal() },
                onOpenChat = { viewModel.openChatPanel() },
                onOpenSync = { viewModel.openDriveSyncModal() },
                onOpenAdd = { viewModel.openAddArticleDialog() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddArticleDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.testTag("add_article_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Save Chrome Page / Article")
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
            // Search Engine & Grid/List Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search articles, hashtags, or equations...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_engine_input")
                )

                // Grid / List Toggle Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.setGridView(!isGridView) },
                        modifier = Modifier.testTag("toggle_grid_view_button")
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.Default.GridView else Icons.AutoMirrored.Filled.ViewList,
                            contentDescription = "Toggle Grid/List",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Categories Filter Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(allCategoryOptions) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(20.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // Active Hashtag Filter Chip (if filtered by hashtag)
            AnimatedVisibility(visible = selectedHashtag != null) {
                selectedHashtag?.let { tag ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "Filtering by:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
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
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear tag filter",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Articles Grid or List
            if (articles.isEmpty()) {
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
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = "No saved reading articles found",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Share pages directly from Google Chrome or tap '+' below to save notes offline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                if (isGridView) {
                    // Clean Grid View (Mandatory user requirement)
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(articles, key = { it.id }) { article ->
                            ArticleCardGrid(
                                article = article,
                                onClick = { viewModel.openArticle(article) },
                                onToggleFavorite = { viewModel.toggleFavorite(article) },
                                onOpenNlp = { viewModel.openNlpModal(article) },
                                onOpenEquations = { viewModel.openEquationModal(article) },
                                onOpenChat = { viewModel.openChatPanel(article) },
                                onHashtagClick = { tag -> viewModel.setHashtag(tag) }
                            )
                        }
                    }
                } else {
                    // List View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(articles, key = { it.id }) { article ->
                            ArticleCardList(
                                article = article,
                                onClick = { viewModel.openArticle(article) },
                                onToggleFavorite = { viewModel.toggleFavorite(article) },
                                onOpenNlp = { viewModel.openNlpModal(article) },
                                onOpenEquations = { viewModel.openEquationModal(article) },
                                onOpenChat = { viewModel.openChatPanel(article) },
                                onHashtagClick = { tag -> viewModel.setHashtag(tag) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal 1: NLP Linguistics Analysis Dialog
    if (isNlpModalOpen) {
        NLPAnalysisModal(
            analysis = nlpAnalysis,
            targetTitle = nlpTargetTitle,
            onDismiss = { viewModel.closeNlpModal() }
        )
    }

    // Modal 2: Physics & Math Equation Solver Dialog (Color Coded 4 Forces)
    if (isEquationModalOpen) {
        EquationSolverModal(
            equations = activeEquations,
            selectedEquation = selectedEquationForSolve,
            solutionResult = equationSolutionText,
            isSolving = isEquationSolving,
            onSelectEquation = { viewModel.selectEquationForSolve(it) },
            onSolveWithAi = { eq, inputs -> viewModel.solveEquationWithHighThinking(eq, inputs) },
            onDismiss = { viewModel.closeEquationModal() }
        )
    }

    // Modal 3: AI Chat Panel
    if (isChatPanelOpen) {
        AIChatPanel(
            targetArticle = activeArticle,
            messages = chatMessages,
            isThinking = isChatThinking,
            onSendMessage = { viewModel.sendChatMessage(it) },
            onDismiss = { viewModel.closeChatPanel() }
        )
    }

    // Modal 4: Google Drive Sync
    if (isDriveSyncOpen) {
        DriveSyncModal(
            syncState = driveSyncState,
            syncMessage = syncMessage,
            onTriggerSync = { viewModel.triggerDriveSync() },
            onDismiss = { viewModel.closeDriveSyncModal() }
        )
    }

    // Modal 5: Article Full Offline Reader
    activeArticle?.let { article ->
        ArticleReaderDialog(
            article = article,
            onDismiss = { viewModel.closeArticle() },
            onToggleFavorite = { viewModel.toggleFavorite(article) },
            onOpenNlp = { viewModel.openNlpModal(article) },
            onOpenEquations = { viewModel.openEquationModal(article) },
            onOpenChat = { viewModel.openChatPanel(article) },
            onDelete = { viewModel.deleteArticle(article) },
            onHashtagClick = { tag -> viewModel.setHashtag(tag) }
        )
    }

    // Modal 6: Add Article Dialog
    if (isAddDialogOpen) {
        AddArticleDialog(
            onDismiss = { viewModel.closeAddArticleDialog() },
            onSave = { url, title, content, category ->
                viewModel.addNewArticle(url, title, content, category)
            }
        )
    }
}
