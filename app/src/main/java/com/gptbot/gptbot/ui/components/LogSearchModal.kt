package com.gptbot.gptbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.gptbot.gptbot.data.model.LogEntry
import com.gptbot.gptbot.engine.log.LogSearchEngine
import com.gptbot.gptbot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSearchModal(
    onDismiss: () -> Unit,
    onAnalyzeInChat: (String) -> Unit
) {
    var rawText by remember { mutableStateOf(LogSearchEngine.sampleAndroidLogcat) }
    var query by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf("ALL") }

    val parsedEntries = remember(rawText) {
        LogSearchEngine.parseLogs(rawText)
    }

    val filteredEntries = remember(parsedEntries, query, selectedLevel, isRegex) {
        LogSearchEngine.filterLogs(parsedEntries, query, selectedLevel, isRegex)
    }

    val levels = listOf("ALL", "FATAL", "ERROR", "WARN", "INFO", "DEBUG")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Zinc900,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Zinc700) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Article,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Log File Search & Root-Cause",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFAFAFA)
                    )
                }

                // Sample log selector buttons
                Row {
                    TextButton(onClick = { rawText = LogSearchEngine.sampleAndroidLogcat }) {
                        Text("Android", fontSize = 11.sp, color = Emerald400)
                    }
                    TextButton(onClick = { rawText = LogSearchEngine.sampleKubernetesLog }) {
                        Text("K8s", fontSize = 11.sp, color = Emerald400)
                    }
                    TextButton(onClick = { rawText = LogSearchEngine.sampleDatabaseLog }) {
                        Text("Postgres", fontSize = 11.sp, color = Emerald400)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search bar & Regex toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search logs (e.g. NullPointerException, error, 500)", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFA1A1AA), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Emerald500,
                        unfocusedBorderColor = Zinc700,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = isRegex,
                    onClick = { isRegex = !isRegex },
                    label = { Text("Regex", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald500.copy(alpha = 0.25f),
                        selectedLabelColor = Emerald400
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Level filters
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                levels.forEach { lvl ->
                    val isSelected = selectedLevel == lvl
                    val chipColor = when (lvl) {
                        "FATAL", "ERROR" -> Rose500
                        "WARN" -> Amber400
                        "INFO" -> Sky400
                        "DEBUG" -> Indigo400
                        else -> Emerald400
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLevel = lvl },
                        label = { Text(lvl, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = chipColor.copy(alpha = 0.25f),
                            selectedLabelColor = chipColor
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stats bar & Send to CoT action
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Showing ${filteredEntries.size} of ${parsedEntries.size} entries",
                    fontSize = 11.sp,
                    color = Color(0xFFA1A1AA)
                )

                Button(
                    onClick = {
                        val sampleToAnalyze = filteredEntries.take(5).joinToString("\n") { it.message + (it.stackTrace ?: "") }
                        onAnalyzeInChat("Analyze this log file and find the root cause:\n$sampleToAnalyze")
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF022C22)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Analyze with CoT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Log entries list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(filteredEntries, key = { it.id }) { entry ->
                    LogItemCard(entry = entry)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LogItemCard(entry: LogEntry) {
    val levelColor = when (entry.level) {
        "FATAL", "ERROR" -> Rose500
        "WARN" -> Amber400
        "DEBUG" -> Indigo400
        else -> Emerald400
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Zinc800.copy(alpha = 0.7f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Zinc700.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = levelColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = entry.level,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = levelColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFAFAFA)
                    )
                }

                Text(
                    text = entry.timestamp,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFA1A1AA)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.message,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE4E4E7)
            )

            if (entry.stackTrace != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = entry.stackTrace,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Rose400,
                    modifier = Modifier
                        .background(Rose500.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }
        }
    }
}
