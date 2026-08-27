package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PersonalStats
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightsTab(
    viewModel: MemoryViewModel,
    stats: PersonalStats,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Top Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Total Memories",
                value = "${stats.totalMemories}",
                subtitle = "Events Logged",
                icon = AppIcons.Timeline,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Words Remembered",
                value = "${stats.totalWords}",
                subtitle = "Across all apps",
                icon = AppIcons.Table,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Apps Tracked",
                value = "${stats.uniqueAppsCount}",
                subtitle = "Active apps",
                icon = AppIcons.Apps,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Starred Items",
                value = "${stats.starredCount}",
                subtitle = "Key takeaways",
                icon = AppIcons.Star,
                modifier = Modifier.weight(1f)
            )
        }

        // Personal Insights Box
        if (stats.personalInsights.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth().testTag("personal_insights_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(AppIcons.Insights, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
                        Text("Personal Insights & Patterns", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        stats.personalInsights.forEach { insight ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CardElevatedDark,
                                border = BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = insight,
                                    fontSize = 13.sp,
                                    color = TextLight,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // App Distribution Progress Bars
        if (stats.appDistribution.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Top Active Apps Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)

                    stats.appDistribution.take(6).forEach { appStat ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(appStat.appName, fontSize = 13.sp, color = TextLight, fontWeight = FontWeight.Medium)
                                Text("${appStat.count} entries (${String.format("%.1f", appStat.percentage)}%)", fontSize = 12.sp, color = SkyBlue500)
                            }
                            LinearProgressIndicator(
                                progress = { appStat.percentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = SkyBlue500,
                                trackColor = Slate800
                            )
                        }
                    }
                }
            }
        }

        // 24-Hour Activity Heatmap
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("24-Hour Information Flow Distribution", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                Text("Activity frequency by hour of day (0:00 - 23:00):", fontSize = 11.5.sp, color = TextMuted)

                val maxCount = (stats.hourlyActivity.maxOrNull() ?: 1).coerceAtLeast(1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.hourlyActivity.forEachIndexed { hour, count ->
                        val heightFraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.08f, 1f)
                        val barColor = if (count > 0) SkyBlue500 else Slate700

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                    .background(barColor)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("12 AM", fontSize = 10.sp, color = TextMuted)
                    Text("6 AM", fontSize = 10.sp, color = TextMuted)
                    Text("12 PM", fontSize = 10.sp, color = TextMuted)
                    Text("6 PM", fontSize = 10.sp, color = TextMuted)
                    Text("11 PM", fontSize = 10.sp, color = TextMuted)
                }
            }
        }

        // Key Themes (#hashtags)
        if (stats.keyThemes.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Recurring Topics & Keyword Trends", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        stats.keyThemes.forEach { theme ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CardElevatedDark,
                                border = BorderStroke(1.dp, BorderDark)
                            ) {
                                Text(
                                    text = theme,
                                    fontSize = 12.sp,
                                    color = SkyBlue500,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Detected Action Items
        if (stats.actionItemsDetected.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(AppIcons.TaskAlt, contentDescription = null, tint = Emerald600, modifier = Modifier.size(20.dp))
                        Text("Detected Action Items & Tasks", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        stats.actionItemsDetected.forEach { action ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CardElevatedDark,
                                border = BorderStroke(1.dp, BorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📌 $action",
                                    fontSize = 12.5.sp,
                                    color = TextLight,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
        border = BorderStroke(1.dp, BorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.5.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                Icon(icon, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(18.dp))
            }
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = 10.5.sp, color = TextMuted)
        }
    }
}
