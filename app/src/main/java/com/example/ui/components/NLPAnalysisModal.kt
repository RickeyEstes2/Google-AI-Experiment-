package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.NLPAnalysisResult
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NLPAnalysisModal(
    analysis: NLPAnalysisResult?,
    targetTitle: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var filterQuery by remember { mutableStateOf("") }

    val tabs = listOf(
        "Term Freq",
        "Named Entities",
        "Verb 'to' Verb",
        "Noun Phrases",
        "Bigrams",
        "Trigrams",
        "POS Tags"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "NLP Linguistic Analysis",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = targetTitle.take(38) + if (targetTitle.length > 38) "..." else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_nlp_modal")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats Overview Row
                if (analysis != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatPill("Total Tokens", "${analysis.totalWords}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        StatPill("Unique Words", "${analysis.uniqueWords}", MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                        StatPill("Readability", "%.1f".format(analysis.readabilityScore), ForceStrongGreen, Modifier.weight(1f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter search
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = { Text("Filter terms, entities, or n-grams...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(onClick = { filterQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Main Content Body based on selected tab
                if (analysis == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (selectedTabIndex) {
                            0 -> {
                                // 1. Term Frequency
                                val filtered = analysis.termFrequencies.filter {
                                    filterQuery.isBlank() || it.term.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No matching terms found") }
                                } else {
                                    val maxCount = analysis.termFrequencies.maxOfOrNull { it.count } ?: 1
                                    items(filtered) { item ->
                                        TermFreqRow(term = item.term, count = item.count, percentage = item.percentage, maxCount = maxCount)
                                    }
                                }
                            }
                            1 -> {
                                // 2. Named Entities
                                val filtered = analysis.namedEntities.filter {
                                    filterQuery.isBlank() || it.entity.contains(filterQuery, ignoreCase = true) || it.type.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No named entities identified") }
                                } else {
                                    items(filtered) { item ->
                                        NamedEntityRow(entity = item.entity, type = item.type, count = item.count)
                                    }
                                }
                            }
                            2 -> {
                                // 3. Verb 'to' Verb
                                val filtered = analysis.verbToVerbs.filter {
                                    filterQuery.isBlank() || it.phrase.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No Verb 'to' Verb collocations found (e.g. 'want to learn')") }
                                } else {
                                    items(filtered) { item ->
                                        VerbToVerbRow(phrase = item.phrase, verb1 = item.verb1, verb2 = item.verb2, count = item.count)
                                    }
                                }
                            }
                            3 -> {
                                // 4. Noun Phrases
                                val filtered = analysis.nounPhrases.filter {
                                    filterQuery.isBlank() || it.phrase.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No multi-word noun phrases detected") }
                                } else {
                                    items(filtered) { item ->
                                        NounPhraseRow(phrase = item.phrase, count = item.count)
                                    }
                                }
                            }
                            4 -> {
                                // 5. Bigrams
                                val filtered = analysis.bigrams.filter {
                                    filterQuery.isBlank() || it.bigram.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No bigrams found") }
                                } else {
                                    items(filtered) { item ->
                                        NgramRow(ngram = item.bigram, count = item.count, label = "Bigram")
                                    }
                                }
                            }
                            5 -> {
                                // 6. Trigrams
                                val filtered = analysis.trigrams.filter {
                                    filterQuery.isBlank() || it.trigram.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No trigrams found") }
                                } else {
                                    items(filtered) { item ->
                                        NgramRow(ngram = item.trigram, count = item.count, label = "Trigram")
                                    }
                                }
                            }
                            6 -> {
                                // 7. POS Tag All Terms
                                val filtered = analysis.posTaggedTerms.filter {
                                    filterQuery.isBlank() || it.term.contains(filterQuery, ignoreCase = true) || it.tag.contains(filterQuery, ignoreCase = true) || it.tagDescription.contains(filterQuery, ignoreCase = true)
                                }
                                if (filtered.isEmpty()) {
                                    item { EmptyStateNotice("No POS tagged terms found") }
                                } else {
                                    items(filtered) { item ->
                                        PosTagRow(term = item.term, tag = item.tag, description = item.tagDescription, count = item.count)
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
private fun StatPill(title: String, value: String, accentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Black, color = accentColor)
        }
    }
}

@Composable
private fun TermFreqRow(term: String, count: Int, percentage: Float, maxCount: Int) {
    val progress = (count.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = term, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = "$count times (%.1f%%)".format(percentage), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun NamedEntityRow(entity: String, type: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = entity, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = type, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "$count occ",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun VerbToVerbRow(phrase: String, verb1: String, verb2: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = verb1, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "to", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = verb2, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Text(text = "Verb-to-Verb Intentional Collocation", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "$count ×", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
        }
    }
}

@Composable
private fun NounPhraseRow(phrase: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = phrase, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(text = "Syntagmatic Noun Chunk", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "$count", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun NgramRow(ngram: String, count: Int, label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = ngram, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(text = label, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                Text(text = "$count", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun PosTagRow(term: String, tag: String, description: String, count: Int) {
    val tagColor = when {
        tag.startsWith("VB") -> MaterialTheme.colorScheme.primary
        tag.startsWith("NN") -> MaterialTheme.colorScheme.secondary
        tag.startsWith("JJ") -> ForceStrongGreen
        tag.startsWith("RB") -> ForceWeakBlue
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = term, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = tagColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = tag,
                    color = tagColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStateNotice(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
