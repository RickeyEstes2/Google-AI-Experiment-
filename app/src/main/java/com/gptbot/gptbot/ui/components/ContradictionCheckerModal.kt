package com.gptbot.gptbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.data.model.ContradictionReport
import com.gptbot.gptbot.engine.contradiction.ContradictionDetectorEngine
import com.gptbot.gptbot.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContradictionCheckerModal(
    onDismiss: () -> Unit,
    onDiscussInChat: (String) -> Unit
) {
    var statementInput by remember {
        mutableStateOf("Our microservices achieve sub-millisecond (< 1ms) zero latency across multi-continent global regions.")
    }

    var report by remember {
        mutableStateOf(ContradictionDetectorEngine.analyze(statementInput))
    }

    val sampleContradictions = listOf(
        "Sub-millisecond global latency across multi-continent regions",
        "Pure immutable function that mutates shared global state on each render",
        "Guaranteed zero packet loss delivery over raw unacknowledged UDP",
        "Perfect consistency and 100% availability during network partitions (CAP theorem)"
    )

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Balance,
                    contentDescription = null,
                    tint = Rose400,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Propositional Contradiction Detector",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFAFAFA)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets
            Text(
                text = "PRESET TEST CASES:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Emerald400,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            sampleContradictions.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Zinc800,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                        .clickable {
                            statementInput = preset
                            report = ContradictionDetectorEngine.analyze(preset)
                        }
                        .border(1.dp, Zinc700, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = preset,
                        fontSize = 11.sp,
                        color = Color(0xFFD4D4D8),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input
            OutlinedTextField(
                value = statementInput,
                onValueChange = {
                    statementInput = it
                    report = ContradictionDetectorEngine.analyze(it)
                },
                label = { Text("Statement or Hypothesis to Test", fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Zinc700,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Report Output
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                item {
                    val badgeBg = if (report.hasContradiction) Rose500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f)
                    val badgeColor = if (report.hasContradiction) Rose400 else Emerald400

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = badgeBg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, badgeColor, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                if (report.hasContradiction) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (report.hasContradiction) "CONTRADICTION DETECTED: [${report.severity} SEVERITY]" else "NO CONTRADICTION DETECTED (PROPOSITIONS SOUND)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }

                if (report.hasContradiction) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Zinc800.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Zinc700, RoundedCornerShape(10.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Conflict Type: ${report.contradictionType}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Amber400
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Premise A: ${report.premiseA}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFAFAFA)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Premise B: ${report.premiseB}",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFAFAFA)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Formal Breakdown:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA1A1AA)
                                )
                                Text(
                                    text = report.detailedAnalysis,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE4E4E7)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resolution Strategy: ${report.resolutionProposal}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Emerald400
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    onDiscussInChat("Analyze and resolve this contradiction:\n$statementInput")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald500, contentColor = Color(0xFF022C22)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Investigate Contradiction with CoT Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
