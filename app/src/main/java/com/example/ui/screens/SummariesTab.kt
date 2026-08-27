package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SummaryResult
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel

@Composable
fun SummariesTab(
    viewModel: MemoryViewModel,
    summaryResult: SummaryResult?,
    isSummarizing: Boolean,
    summaryTimeframe: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeframes = listOf("Today", "Past 7 Days", "Past 30 Days", "All Time")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Timeframe Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeframes.forEach { tf ->
                val isSelected = summaryTimeframe == tf
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setSummaryTimeframe(tf) },
                    label = { Text(tf, fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isSummarizing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SkyBlue500)
            }
        } else if (summaryResult != null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                border = BorderStroke(1.dp, BorderDark),
                modifier = Modifier.fillMaxWidth().testTag("summary_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = summaryResult.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Cross-App Executive Synthesis",
                                fontSize = 11.5.sp,
                                color = TextMuted
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SkyBlue600.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, SkyBlue600.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${summaryResult.memoryCount} Memories",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkyBlue500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Stats Quick Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardElevatedDark,
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summaryResult.wordCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("Words Synthesized", fontSize = 11.sp, color = TextMuted)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${summaryResult.topApps.size}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                                Text("Active Apps", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }

                    // Key Takeaways
                    if (summaryResult.keyTakeaways.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Key Highlights:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SkyBlue500)
                            summaryResult.keyTakeaways.forEach { takeaway ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(AppIcons.Check, contentDescription = null, tint = Emerald600, modifier = Modifier.size(16.dp))
                                    Text(takeaway, fontSize = 13.sp, color = TextLight, lineHeight = 18.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f))

                    // Detailed Summary Narrative
                    Text("Executive Summary Narrative:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextMuted)
                    Text(
                        text = summaryResult.summary,
                        fontSize = 13.5.sp,
                        color = Slate200,
                        lineHeight = 21.sp
                    )

                    // Top Apps breakdown in summary
                    if (summaryResult.topApps.isNotEmpty()) {
                        Text("App Activity Breakdown:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextMuted)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            summaryResult.topApps.forEach { (app, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(app, fontSize = 12.5.sp, color = TextLight)
                                    Text("$count entries", fontSize = 12.sp, color = SkyBlue500, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = BorderDark.copy(alpha = 0.6f))

                    // Actions: Copy / Share
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val fullSummary = "${summaryResult.title}\n\n${summaryResult.summary}\n\nHighlights:\n${summaryResult.keyTakeaways.joinToString("\n") { "• $it" }}"
                                clipboard.setPrimaryClip(ClipData.newPlainText("CrossApp Summary", fullSummary))
                                Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Copy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Summary", fontSize = 12.5.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val fullSummary = "${summaryResult.title}\n\n${summaryResult.summary}\n\nHighlights:\n${summaryResult.keyTakeaways.joinToString("\n") { "• $it" }}"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, summaryResult.title)
                                    putExtra(Intent.EXTRA_TEXT, fullSummary)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Summary"))
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(AppIcons.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share", fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(90.dp))
    }
}
