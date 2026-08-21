package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.Article

@Composable
fun RichArticleContent(
    article: Article,
    linkedArticles: List<Article> = emptyList(),
    onLinkedArticleClick: (Article) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary
        if (article.summary.isNotBlank()) {
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = article.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Main formatted notes
        if (article.notes.isNotBlank()) {
            RenderFormattedMarkdown(content = article.notes)
        }

        // Sample interactive charts or Venn diagrams if relevant topics are mentioned
        if (article.hashtags.any { it.contains("AI", ignoreCase = true) || it.contains("Research", ignoreCase = true) }) {
            InteractiveBarChart(
                title = "Model Compute & Accuracy Benchmark",
                data = listOf(
                    ChartBarData("Base", 45f, Color(0xFF94A3B8)),
                    ChartBarData("v1.5", 72f, Color(0xFF38BDF8)),
                    ChartBarData("v2.0", 94f, Color(0xFF0284C7)),
                    ChartBarData("v3.0", 118f, Color(0xFF0F172A))
                )
            )
        }

        if (article.hashtags.any { it.contains("Design", ignoreCase = true) || it.contains("Work", ignoreCase = true) }) {
            InteractiveVennDiagram(
                title = "Design System & Engineering Synergy",
                setALabel = "Design Patterns",
                setBLabel = "Composable Code",
                intersectionLabel = "M3 Component Architecture"
            )
        }

        // Related Links / Connected Knowledge Notes
        if (linkedArticles.isNotEmpty()) {
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            Text(
                text = "Connected Knowledge Links (${linkedArticles.size})",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            linkedArticles.forEach { linked ->
                OutlinedCard(
                    onClick = { onLinkedArticleClick(linked) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔗",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = linked.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                                maxLines = 1
                            )
                            Text(
                                text = linked.domain,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
