package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composite Rich Content View that renders:
 * 1. Interactive Charts (```chart:bar ... ```, ```chart:line ... ```, etc.)
 * 2. Interactive Venn Diagrams (```venn:2 ... ```, ```venn:3 ... ```)
 * 3. LaTeX Math Display Blocks ($$ ... $$) and inline math
 * 4. Rich Formatted Text, Custom Fonts/Colors/Highlights, and Clickable Hyperlinks
 */
@Composable
fun RichArticleContent(
    rawContent: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    onHashtagClick: ((String) -> Unit)? = null,
    onTextFormatted: ((String) -> Unit)? = null
) {
    if (rawContent.isBlank()) {
        Text(
            text = "No content provided.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            modifier = modifier
        )
        return
    }

    // Step 1: Parse Charts
    val chartItems = remember(rawContent) {
        ChartParser.parseCharts(rawContent)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chartItems.forEach { chartBlock ->
            when (chartBlock) {
                is ChartBlockItem.Chart -> {
                    InteractiveChartCard(chart = chartBlock.model)
                }
                is ChartBlockItem.Text -> {
                    // Step 2: Parse Venn Diagrams inside the text segment
                    val vennItems = remember(chartBlock.content) {
                        VennParser.parseVennDiagrams(chartBlock.content)
                    }

                    vennItems.forEach { vennBlock ->
                        when (vennBlock) {
                            is VennBlockItem.Diagram -> {
                                InteractiveVennCard(venn = vennBlock.model)
                            }
                            is VennBlockItem.Text -> {
                                // Step 3: Parse LaTeX Math Blocks
                                val latexSegments = remember(vennBlock.content) {
                                    LatexParser.extractLatexBlocks(vennBlock.content)
                                }

                                latexSegments.forEach { seg ->
                                    when (seg) {
                                        is LatexSegment.DisplayMath -> {
                                            LatexMathBlock(
                                                latex = seg.latex,
                                                isDisplayMode = true,
                                                fontSize = 17
                                            )
                                        }
                                        is LatexSegment.InlineMath -> {
                                            LatexMathBlock(
                                                latex = seg.latex,
                                                isDisplayMode = false,
                                                fontSize = 15
                                            )
                                        }
                                        is LatexSegment.PlainText -> {
                                            if (seg.text.isNotBlank()) {
                                                HyperlinkText(
                                                    text = seg.text,
                                                    style = style,
                                                    onHashtagClick = onHashtagClick,
                                                    onTextFormatted = { updatedSpan ->
                                                        // Replace in whole raw content
                                                        if (rawContent.contains(seg.text)) {
                                                            val updated = rawContent.replace(seg.text, updatedSpan)
                                                            onTextFormatted?.invoke(updated)
                                                        } else {
                                                            onTextFormatted?.invoke(updatedSpan)
                                                        }
                                                    }
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
    }
}
