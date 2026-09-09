package com.gptbot.gptbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gptbot.gptbot.data.model.SearchResultItem
import com.gptbot.gptbot.ui.theme.*

@Composable
fun SearchGroundingCard(
    results: List<SearchResultItem>,
    modifier: Modifier = Modifier
) {
    if (results.isEmpty()) return

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Zinc900,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Zinc700, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Language,
                    contentDescription = "Google Search",
                    tint = Emerald400,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GOOGLE SEARCH GROUNDING & CITATIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            results.forEach { item ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Zinc800.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, Zinc700.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                                    color = Emerald500.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "[${item.citationIndex}]",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFAFAFA),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Text(
                                text = item.domain,
                                fontSize = 10.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.snippet,
                            fontSize = 11.sp,
                            color = Color(0xFFD4D4D8),
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
