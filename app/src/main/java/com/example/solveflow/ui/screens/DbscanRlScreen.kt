package com.example.solveflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.solveflow.ui.viewmodel.CodeGenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DbscanRlScreen(
    viewModel: CodeGenViewModel,
    modifier: Modifier = Modifier
) {
    val dbscanResult by viewModel.dbscanResult.collectAsState()
    val eps by viewModel.dbscanEps.collectAsState()
    val minPts by viewModel.dbscanMinPts.collectAsState()
    val policyEntries by viewModel.policyEntries.collectAsState()

    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = DBSCAN, 1 = Reinforce Learning

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Mode Selector TabRow
        TabRow(selectedTabIndex = activeSubTab) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("DBSCAN Clustering") },
                icon = { Icon(Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Reinforcement Learning") },
                icon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
            )
        }

        if (activeSubTab == 0) {
            // --- DBSCAN SECTION ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "DBSCAN Hyperparameters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Density-Based Spatial Clustering of Applications with Noise groups code snippets by semantic proximity while rejecting outliers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Epsilon Slider
                    Text(
                        text = "Epsilon Radius (ε): ${String.format("%.2f", eps)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = eps.toFloat(),
                        onValueChange = { viewModel.setDBSCANParams(it.toDouble(), minPts) },
                        valueRange = 0.1f..0.9f,
                        steps = 8
                    )

                    // MinPts Slider
                    Text(
                        text = "Minimum Points (MinPts): $minPts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = minPts.toFloat(),
                        onValueChange = { viewModel.setDBSCANParams(eps, it.toInt()) },
                        valueRange = 1f..5f,
                        steps = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.recalculateDBSCAN() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Re-Cluster Semantic Vector Space")
                    }
                }
            }

            // DBSCAN Metrics Overview
            dbscanResult?.let { result ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBadge(
                        title = "Clusters Formed",
                        value = "${result.clusters.size}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        title = "Core Points",
                        value = "${result.corePointIds.size}",
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f)
                    )
                    MetricBadge(
                        title = "Noise Outliers",
                        value = "${result.noisePoints.size}",
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dense Clusters
                Text(
                    text = "Identified Dense Semantic Clusters:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (result.clusters.isEmpty()) {
                    Text(
                        text = "No dense clusters formed at current ε radius. Try increasing Epsilon or adding more snippets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    result.clusters.forEach { (clusterId, points) ->
                        val keywords = result.clusterKeywords[clusterId] ?: emptyList()
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Cluster #$clusterId",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${points.size} vector point(s)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    keywords.forEach { kw ->
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = kw,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                points.forEach { pt ->
                                    val isCore = result.corePointIds.contains(pt.id)
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(if (isCore) Color(0xFF16A34A) else Color(0xFFEAB308), RoundedCornerShape(4.dp))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = pt.title,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = if (isCore) "Core" else "Border",
                                            fontSize = 10.sp,
                                            color = if (isCore) Color(0xFF16A34A) else Color(0xFFEAB308)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Noise Outliers Section
                if (result.noisePoints.isNotEmpty()) {
                    Text(
                        text = "Noise Points (Isolated Outliers):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            result.noisePoints.forEach { pt ->
                                Text(
                                    text = "• ${pt.title} (${pt.languageId})",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // --- REINFORCEMENT LEARNING SECTION ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Q-Learning Policy Architecture",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The generator continuously optimizes code style and error mitigation via Contextual Bandit Q-Learning. Actions represent generation strategies (Concise, Defensive, Modular, High-Performance, Test-Driven).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Exploration Rate (ε): 15% (Epsilon-greedy)", style = MaterialTheme.typography.labelSmall)
                        Text("Learning Rate (α): 0.25", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Learned Policy Q-Table entries
            Text(
                text = "Learned Policy State-Action Q-Table:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (policyEntries.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No policy episodes logged yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Generate code, submit thumbs up/down rewards, or edit mistakes to train the Q-learning policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                policyEntries.groupBy { it.stateKey }.forEach { (stateKey, entries) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "State: $stateKey",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val totalEpisodes = entries.sumOf { it.updateCount }
                                Text(
                                    text = "$totalEpisodes episode(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            entries.sortedByDescending { it.qValue }.forEach { entry ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.strategyName.replace("_", " "),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        LinearProgressIndicator(
                                            progress = { ((entry.qValue + 1.0) / 2.0).coerceIn(0.0, 1.0).toFloat() },
                                            modifier = Modifier.fillMaxWidth(0.8f).height(4.dp),
                                            color = if (entry.qValue >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Q = ${String.format("%.3f", entry.qValue)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.qValue >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                        Text(
                                            text = "N = ${entry.updateCount}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
