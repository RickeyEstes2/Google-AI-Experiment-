package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AppCategory
import com.example.data.model.MemoryEntity
import com.example.ui.components.MemoryCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TimelineTab(
    viewModel: MemoryViewModel,
    memories: List<MemoryEntity>,
    distinctApps: List<String>,
    searchQuery: String,
    selectedAppFilter: String?,
    selectedCategoryFilter: String?,
    starredOnly: Boolean,
    onMemoryClick: (MemoryEntity) -> Unit,
    onEditMemory: (MemoryEntity) -> Unit,
    onAddAddendum: (MemoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Chronological Grouping: Today, Yesterday, This Week, Earlier This Month, Older
    val groupedMemories = remember(memories) {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Start of today
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000L
        val thisWeekStart = todayStart - 6 * 24 * 60 * 60 * 1000L
        val thisMonthStart = todayStart - 30 * 24 * 60 * 60 * 1000L

        val todayList = mutableListOf<MemoryEntity>()
        val yesterdayList = mutableListOf<MemoryEntity>()
        val thisWeekList = mutableListOf<MemoryEntity>()
        val thisMonthList = mutableListOf<MemoryEntity>()
        val olderList = mutableListOf<MemoryEntity>()

        for (m in memories) {
            when {
                m.timestamp >= todayStart -> todayList.add(m)
                m.timestamp >= yesterdayStart -> yesterdayList.add(m)
                m.timestamp >= thisWeekStart -> thisWeekList.add(m)
                m.timestamp >= thisMonthStart -> thisMonthList.add(m)
                else -> olderList.add(m)
            }
        }

        listOf(
            "Today" to todayList,
            "Yesterday" to yesterdayList,
            "This Week" to thisWeekList,
            "Earlier this Month" to thisMonthList,
            "Older Memories" to olderList
        ).filter { it.second.isNotEmpty() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search Bar (Single-line rounded pill)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search text across apps, keywords, tags...", fontSize = 13.5.sp, color = TextMuted) },
            leadingIcon = { Icon(AppIcons.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(AppIcons.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
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
                cursorColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("timeline_search_bar")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Starred toggle chip
            item {
                FilterChip(
                    selected = starredOnly,
                    onClick = { viewModel.toggleStarredOnly() },
                    leadingIcon = {
                        Icon(
                            imageVector = if (starredOnly) AppIcons.Star else AppIcons.StarBorder,
                            contentDescription = null,
                            tint = if (starredOnly) Amber600 else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Starred", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Category Filter Chips
            items(AppCategory.entries) { cat ->
                val isSelected = selectedCategoryFilter == cat.displayName
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setCategoryFilter(if (isSelected) null else cat.displayName) },
                    label = { Text(cat.displayName, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // App Filter Chips
            items(distinctApps) { appName ->
                val isSelected = selectedAppFilter == appName
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setAppFilter(if (isSelected) null else appName) },
                    label = { Text(appName, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (memories.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                    border = BorderStroke(1.dp, BorderDark),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CardElevatedDark,
                            border = BorderStroke(1.dp, BorderDark),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(AppIcons.Timeline, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(28.dp))
                            }
                        }

                        Text(
                            text = if (searchQuery.isNotBlank() || selectedAppFilter != null || selectedCategoryFilter != null)
                                "No matching memories found"
                            else
                                "No memories captured yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )

                        Text(
                            text = if (searchQuery.isNotBlank())
                                "Try broadening your search query or clearing your filter chips."
                            else
                                "Enable the Accessibility Service in Settings to automatically record text across apps, or load sample data to explore right away.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            lineHeight = 18.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { viewModel.populateSampleData() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Load Sample Data", fontSize = 12.5.sp)
                            }
                            OutlinedButton(
                                onClick = { viewModel.openAddDialog() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+ Quick Capture", fontSize = 12.5.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Grouped Chronological List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                groupedMemories.forEach { (sectionTitle, sectionMemories) ->
                    // Section Header
                    item(key = "header_$sectionTitle") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sectionTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.5.sp
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CardElevatedDark,
                                border = BorderStroke(1.dp, BorderDark)
                            ) {
                                Text(
                                    text = "${sectionMemories.size} memories",
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Section Items
                    items(
                        items = sectionMemories,
                        key = { it.id }
                    ) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = { onMemoryClick(memory) },
                            onToggleStar = { viewModel.toggleStar(memory) },
                            onEdit = { onEditMemory(memory) },
                            onDelete = { viewModel.deleteMemory(memory) },
                            onAddendumClick = { onAddAddendum(memory) }
                        )
                    }
                }
            }
        }
    }
}
