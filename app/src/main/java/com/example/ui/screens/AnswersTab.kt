package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.example.data.model.AnswerResult
import com.example.data.model.MemoryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MemoryViewModel
import com.example.util.TimeUtils

@Composable
fun AnswersTab(
    viewModel: MemoryViewModel,
    qnaQuery: String,
    currentAnswer: AnswerResult?,
    isAnswering: Boolean,
    answerHistory: List<AnswerResult>,
    onMemoryClick: (MemoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickQuestions = listOf(
        "Action items and tasks",
        "Summarize work discussions",
        "What did I read in Chrome?",
        "Dinner or event plans",
        "Key math formulas and notes",
        "Order confirmations and delivery dates"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Input Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, BorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(AppIcons.Sparkles, contentDescription = null, tint = SkyBlue500, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Ask Your Memories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                OutlinedTextField(
                    value = qnaQuery,
                    onValueChange = { viewModel.setQnaQuery(it) },
                    placeholder = { Text("e.g., What did Alex say about the roadmap?", fontSize = 13.5.sp, color = TextMuted) },
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardElevatedDark,
                        focusedContainerColor = CardElevatedDark,
                        unfocusedBorderColor = BorderDark,
                        focusedBorderColor = SkyBlue500
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("qna_query_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instant Semantic Cross-App Q&A",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                    Button(
                        onClick = { viewModel.askQuestion() },
                        enabled = qnaQuery.isNotBlank() && !isAnswering,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("ask_button")
                    ) {
                        if (isAnswering) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Icon(AppIcons.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ask", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Suggestions
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickQuestions) { question ->
                SuggestionChip(
                    onClick = {
                        viewModel.setQnaQuery(question)
                        viewModel.askQuestion(question)
                    },
                    label = { Text(question, fontSize = 11.5.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Answers / Results Stream
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (currentAnswer != null) {
                item(key = "current_answer") {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, SkyBlue500.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth().testTag("active_answer_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header Query
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SkyBlue600.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, SkyBlue600.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "Q: \"${currentAnswer.query}\"",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = SkyBlue500,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }

                            // Synthesized Answer
                            Text(
                                text = currentAnswer.answer,
                                fontSize = 14.sp,
                                color = TextLight,
                                lineHeight = 21.sp
                            )

                            // Key Takeaway Points
                            if (currentAnswer.keyPoints.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Key Source Insights:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    currentAnswer.keyPoints.forEach { point ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = CardElevatedDark,
                                            border = BorderStroke(1.dp, BorderDark),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "• $point",
                                                fontSize = 12.5.sp,
                                                color = Slate200,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Cited Memories
                            if (currentAnswer.citedMemories.isNotEmpty()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Cited Memory References (tap to view):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                    currentAnswer.citedMemories.forEach { mem ->
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = CardElevatedDark,
                                            border = BorderStroke(1.dp, BorderDark),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onMemoryClick(mem) }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(mem.appName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SkyBlue500)
                                                        Text("·", color = TextMuted)
                                                        Text(TimeUtils.formatPostDateTime(mem.timestamp), fontSize = 11.sp, color = TextMuted)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = mem.title.ifBlank { mem.text },
                                                        fontSize = 13.sp,
                                                        color = Color.White,
                                                        maxLines = 2
                                                    )
                                                }
                                                Icon(AppIcons.Back, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // History Header
            if (answerHistory.size > 1) {
                item(key = "history_header") {
                    Text(
                        text = "Recent Questions & Answers",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(
                    items = answerHistory.drop(1),
                    key = { it.generatedAt }
                ) { hist ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
                        border = BorderStroke(1.dp, BorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setQnaQuery(hist.query)
                                viewModel.askQuestion(hist.query)
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Q: ${hist.query}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = SkyBlue500
                            )
                            Text(
                                text = hist.answer,
                                fontSize = 12.5.sp,
                                color = TextMuted,
                                maxLines = 3
                            )
                        }
                    }
                }
            }
        }
    }
}
