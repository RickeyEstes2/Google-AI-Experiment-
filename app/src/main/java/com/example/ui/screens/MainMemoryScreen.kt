package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MemoryEntity
import com.example.ui.components.AddEditMemoryDialog
import com.example.ui.components.MemoryDetailDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel
import com.example.ui.viewmodel.NavigationTab
import com.example.util.TimeUtils
import kotlinx.coroutines.delay
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMemoryScreen(viewModel: MemoryViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val distinctApps by viewModel.distinctApps.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedAppFilter by viewModel.selectedAppFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val starredOnly by viewModel.starredOnly.collectAsStateWithLifecycle()

    val selectedDetail by viewModel.selectedMemoryDetail.collectAsStateWithLifecycle()
    val showAddEditDialog by viewModel.showAddEditDialog.collectAsStateWithLifecycle()
    val memoryToEdit by viewModel.memoryToEdit.collectAsStateWithLifecycle()

    val qnaQuery by viewModel.qnaQuery.collectAsStateWithLifecycle()
    val currentAnswer by viewModel.currentAnswer.collectAsStateWithLifecycle()
    val isAnswering by viewModel.isAnswering.collectAsStateWithLifecycle()
    val answerHistory by viewModel.answerHistory.collectAsStateWithLifecycle()

    val summaryResult by viewModel.summaryResult.collectAsStateWithLifecycle()
    val isSummarizing by viewModel.isSummarizing.collectAsStateWithLifecycle()
    val summaryTimeframe by viewModel.summaryTimeframe.collectAsStateWithLifecycle()

    val personalStats by viewModel.personalStats.collectAsStateWithLifecycle()
    val exportStatusMessage by viewModel.exportStatusMessage.collectAsStateWithLifecycle()
    val isAccessibilityActive by viewModel.isAccessibilityActive.collectAsStateWithLifecycle()

    // 12-hour live clock without leading zero
    var liveClockText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            liveClockText = TimeUtils.formatLiveClock(Date())
            delay(1000)
        }
    }

    var addendumTargetMemory by remember { mutableStateOf<MemoryEntity?>(null) }
    var quickAddendumInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(
                color = DeepDarkBackground,
                border = BorderStroke(0.dp, Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // App Bar Title & Live Clock
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
                                shape = RoundedCornerShape(10.dp),
                                color = SkyBlue600.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, SkyBlue600.copy(alpha = 0.4f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(AppIcons.Sparkles, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(20.dp))
                                }
                            }

                            Column {
                                Text(
                                    text = "CrossApp Memory",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = selectedTab.title,
                                    fontSize = 11.5.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        // Accessibility Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAccessibilityActive) Emerald600.copy(alpha = 0.2f) else CardElevatedDark,
                            border = BorderStroke(1.dp, if (isAccessibilityActive) Emerald600.copy(alpha = 0.4f) else BorderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (isAccessibilityActive) Emerald600 else Amber600, RoundedCornerShape(3.dp))
                                )
                                Text(
                                    text = if (isAccessibilityActive) "Monitoring" else "Local DB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isAccessibilityActive) Emerald600 else TextMuted
                                )
                            }
                        }
                    }

                    // Live 12-Hour Clock Bar
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CardElevatedDark.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BorderDark.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = liveClockText,
                                fontSize = 11.5.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "${memories.size} Memories",
                                fontSize = 11.5.sp,
                                color = SkyBlue500,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardSurfaceDark,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(tab) },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    NavigationTab.TIMELINE -> AppIcons.Timeline
                                    NavigationTab.ANSWERS -> AppIcons.Answers
                                    NavigationTab.SUMMARIES -> AppIcons.Summaries
                                    NavigationTab.INSIGHTS -> AppIcons.Stats
                                    NavigationTab.EXPORT -> AppIcons.Export
                                },
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label, fontSize = 10.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SkyBlue500,
                            selectedTextColor = SkyBlue500,
                            indicatorColor = SkyBlue600.copy(alpha = 0.15f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == NavigationTab.TIMELINE) {
                FloatingActionButton(
                    onClick = { viewModel.openAddDialog() },
                    containerColor = SkyBlue500,
                    contentColor = Slate900,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_capture_fab")
                ) {
                    Icon(AppIcons.Add, contentDescription = "Quick Capture")
                }
            }
        },
        containerColor = DeepDarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                NavigationTab.TIMELINE -> {
                    TimelineTab(
                        viewModel = viewModel,
                        memories = memories,
                        distinctApps = distinctApps,
                        searchQuery = searchQuery,
                        selectedAppFilter = selectedAppFilter,
                        selectedCategoryFilter = selectedCategoryFilter,
                        starredOnly = starredOnly,
                        onMemoryClick = { viewModel.openMemoryDetail(it) },
                        onEditMemory = { viewModel.openEditDialog(it) },
                        onAddAddendum = { addendumTargetMemory = it }
                    )
                }
                NavigationTab.ANSWERS -> {
                    AnswersTab(
                        viewModel = viewModel,
                        qnaQuery = qnaQuery,
                        currentAnswer = currentAnswer,
                        isAnswering = isAnswering,
                        answerHistory = answerHistory,
                        onMemoryClick = { viewModel.openMemoryDetail(it) }
                    )
                }
                NavigationTab.SUMMARIES -> {
                    SummariesTab(
                        viewModel = viewModel,
                        summaryResult = summaryResult,
                        isSummarizing = isSummarizing,
                        summaryTimeframe = summaryTimeframe
                    )
                }
                NavigationTab.INSIGHTS -> {
                    InsightsTab(
                        viewModel = viewModel,
                        stats = personalStats
                    )
                }
                NavigationTab.EXPORT -> {
                    ExportSettingsTab(
                        viewModel = viewModel,
                        isAccessibilityActive = isAccessibilityActive,
                        exportStatusMessage = exportStatusMessage
                    )
                }
            }
        }
    }

    // Detail Dialog
    if (selectedDetail != null) {
        MemoryDetailDialog(
            memory = selectedDetail!!,
            onDismiss = { viewModel.closeMemoryDetail() },
            onToggleStar = { viewModel.toggleStar(selectedDetail!!) },
            onEdit = {
                val toEdit = selectedDetail
                viewModel.closeMemoryDetail()
                toEdit?.let { viewModel.openEditDialog(it) }
            },
            onDelete = { viewModel.deleteMemory(selectedDetail!!) },
            onAddAddendum = { content -> viewModel.addAddendum(selectedDetail!!.id, content) },
            onRemoveAddendum = { addendumId -> viewModel.removeAddendum(selectedDetail!!.id, addendumId) }
        )
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditMemoryDialog(
            memoryToEdit = memoryToEdit,
            onDismiss = { viewModel.closeAddEditDialog() },
            onSave = { id, text, appName, pkgName, category, title, tags, sourceType, sentiment ->
                viewModel.saveMemory(
                    id = id,
                    text = text,
                    appName = appName,
                    packageName = pkgName,
                    category = category,
                    title = title,
                    tags = tags,
                    sourceType = sourceType,
                    sentiment = sentiment
                )
            }
        )
    }

    // Quick Addendum Dialog
    if (addendumTargetMemory != null) {
        AlertDialog(
            onDismissRequest = {
                addendumTargetMemory = null
                quickAddendumInput = ""
            },
            title = { Text("Append Note to ${addendumTargetMemory!!.appName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = addendumTargetMemory!!.title.ifBlank { addendumTargetMemory!!.text.take(60) },
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    OutlinedTextField(
                        value = quickAddendumInput,
                        onValueChange = { quickAddendumInput = it },
                        placeholder = { Text("Write addendum or update...", fontSize = 13.sp) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (quickAddendumInput.isNotBlank()) {
                            viewModel.addAddendum(addendumTargetMemory!!.id, quickAddendumInput.trim())
                            addendumTargetMemory = null
                            quickAddendumInput = ""
                        }
                    },
                    enabled = quickAddendumInput.isNotBlank()
                ) {
                    Text("Append")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    addendumTargetMemory = null
                    quickAddendumInput = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
