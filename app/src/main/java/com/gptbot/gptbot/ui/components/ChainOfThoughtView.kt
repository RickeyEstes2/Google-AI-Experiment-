package com.gptbot.gptbot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.data.model.ChainOfThought
import com.gptbot.gptbot.ui.theme.*

@Composable
fun ChainOfThoughtCard(
    cot: ChainOfThought,
    currentIteration: Int,
    onSelfImproveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val latestIteration = cot.iterations.lastOrNull()
    val confidence = latestIteration?.confidenceScore ?: 86
    val delta = latestIteration?.deltaConfidence ?: 12

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Zinc900,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Emerald500.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Accordion toggle & Iteration Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Emerald500.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Chain of Thought",
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Chain of Thought (5-Stage Reasoning)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFAFAFA)
                        )
                        Text(
                            text = if (isExpanded) "Tap to collapse reasoning" else "Tap to inspect reasoning trace & critique",
                            fontSize = 11.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Iteration pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Emerald500.copy(alpha = 0.15f),
                        modifier = Modifier.border(1.dp, Emerald400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                Icons.Default.Autorenew,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Iteration #$currentIteration • $confidence% (+$delta%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color(0xFFA1A1AA),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider(color = Zinc800, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Stage 1: Decomposition
                    ReasoningStageItem(
                        stageNumber = 1,
                        title = "Stage 1: Intent & Problem Decomposition",
                        icon = Icons.Default.Search,
                        accentColor = Violet400,
                        content = cot.stage1Decomposition
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Stage 2: Hypothesis & Deduction
                    ReasoningStageItem(
                        stageNumber = 2,
                        title = "Stage 2: Hypothesis & Deduction",
                        icon = Icons.Default.Lightbulb,
                        accentColor = Amber400,
                        content = cot.stage2Hypothesis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Stage 3: Contradiction Check
                    ReasoningStageItem(
                        stageNumber = 3,
                        title = "Stage 3: Contradiction & Rigor Verification",
                        icon = Icons.Default.Balance,
                        accentColor = Rose500,
                        content = cot.stage3ContradictionCheck
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 4. Stage 4: Self-Improvement & Reflection Loop
                    ReasoningStageItem(
                        stageNumber = 4,
                        title = "Stage 4: Self-Improvement & Reflection Loop",
                        icon = Icons.Default.AutoFixHigh,
                        accentColor = Emerald400,
                        content = cot.stage4SelfImprovement
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Self-Improvement trigger button
                    Button(
                        onClick = onSelfImproveClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald500,
                            contentColor = Color(0xFF022C22)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = "Improve",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Run Self-Improvement Loop (Iteration #${currentIteration + 1})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReasoningStageItem(
    stageNumber: Int,
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Zinc800.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Zinc700.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content.ifBlank { "Analysis evaluated without anomalies." },
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFE4E4E7),
                lineHeight = 16.sp
            )
        }
    }
}
