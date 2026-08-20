package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.hypot
import kotlin.math.sqrt

private fun dist(a: Offset, b: Offset): Float = hypot(a.x - b.x, a.y - b.y)

data class Venn2SetModel(
    val title: String = "2-Set Venn Diagram",
    val setAName: String = "Frontend",
    val setBName: String = "Backend",
    val setAItems: List<String> = listOf("React", "Jetpack Compose", "CSS"),
    val setBItems: List<String> = listOf("Kotlin", "PostgreSQL", "Docker"),
    val intersectionAB: List<String> = listOf("TypeScript", "GraphQL", "REST APIs")
)

data class Venn3SetModel(
    val title: String = "Product Sweet Spot",
    val setAName: String = "Desirability (Users)",
    val setBName: String = "Viability (Business)",
    val setCName: String = "Feasibility (Tech)",
    val setAOnly: List<String> = listOf("Delightful UX", "Accessibility"),
    val setBOnly: List<String> = listOf("Monetization", "Market Growth"),
    val setCOnly: List<String> = listOf("Scalable Cloud", "Clean Code"),
    val intersectAB: List<String> = listOf("Product Market Fit"),
    val intersectAC: List<String> = listOf("Working Prototype"),
    val intersectBC: List<String> = listOf("Profitable Tech"),
    val intersectABC: List<String> = listOf("★ Innovation Sweet Spot ★")
)

sealed class VennModel {
    data class TwoSet(val data: Venn2SetModel) : VennModel()
    data class ThreeSet(val data: Venn3SetModel) : VennModel()
}

object VennParser {

    private val VENN_BLOCK_REGEX = Regex("```venn:(2|3)\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    fun containsVenn(text: String): Boolean = VENN_BLOCK_REGEX.containsMatchIn(text)

    fun parseVennDiagrams(text: String): List<VennBlockItem> {
        val items = mutableListOf<VennBlockItem>()
        var lastIndex = 0

        VENN_BLOCK_REGEX.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                items.add(VennBlockItem.Text(text.substring(lastIndex, start)))
            }

            val typeStr = match.groupValues[1]
            val body = match.groupValues[2]
            val map = mutableMapOf<String, String>()

            body.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.contains(":")) {
                    val k = trimmed.substringBefore(":").trim().lowercase()
                    val v = trimmed.substringAfter(":").trim()
                    map[k] = v
                }
            }

            if (typeStr == "2") {
                val title = map["title"] ?: "2-Way Overlap"
                val setA = map["seta"] ?: "Set A"
                val setB = map["setb"] ?: "Set B"
                val itemsA = (map["itemsa"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val itemsB = (map["itemsb"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val overlap = (map["overlap"] ?: map["intersection"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }

                items.add(VennBlockItem.Diagram(VennModel.TwoSet(Venn2SetModel(title, setA, setB, itemsA, itemsB, overlap))))
            } else {
                val title = map["title"] ?: "3-Way Overlap"
                val setA = map["seta"] ?: "Set A"
                val setB = map["setb"] ?: "Set B"
                val setC = map["setc"] ?: "Set C"
                val itemsA = (map["itemsa"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val itemsB = (map["itemsb"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val itemsC = (map["itemsc"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val ab = (map["ab"] ?: map["intersectab"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val ac = (map["ac"] ?: map["intersectac"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val bc = (map["bc"] ?: map["intersectbc"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val abc = (map["abc"] ?: map["intersectabc"] ?: map["center"] ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }

                items.add(VennBlockItem.Diagram(VennModel.ThreeSet(Venn3SetModel(title, setA, setB, setC, itemsA, itemsB, itemsC, ab, ac, bc, abc))))
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            items.add(VennBlockItem.Text(text.substring(lastIndex)))
        }

        return if (items.isEmpty()) listOf(VennBlockItem.Text(text)) else items
    }

    fun toMarkdown(model: VennModel): String {
        return when (model) {
            is VennModel.TwoSet -> {
                val d = model.data
                "```venn:2\n" +
                        "title: ${d.title}\n" +
                        "setA: ${d.setAName}\n" +
                        "setB: ${d.setBName}\n" +
                        "itemsA: ${d.setAItems.joinToString(", ")}\n" +
                        "itemsB: ${d.setBItems.joinToString(", ")}\n" +
                        "overlap: ${d.intersectionAB.joinToString(", ")}\n" +
                        "```\n"
            }
            is VennModel.ThreeSet -> {
                val d = model.data
                "```venn:3\n" +
                        "title: ${d.title}\n" +
                        "setA: ${d.setAName}\n" +
                        "setB: ${d.setBName}\n" +
                        "setC: ${d.setCName}\n" +
                        "itemsA: ${d.setAOnly.joinToString(", ")}\n" +
                        "itemsB: ${d.setBOnly.joinToString(", ")}\n" +
                        "itemsC: ${d.setCOnly.joinToString(", ")}\n" +
                        "ab: ${d.intersectAB.joinToString(", ")}\n" +
                        "ac: ${d.intersectAC.joinToString(", ")}\n" +
                        "bc: ${d.intersectBC.joinToString(", ")}\n" +
                        "abc: ${d.intersectABC.joinToString(", ")}\n" +
                        "```\n"
            }
        }
    }
}

sealed class VennBlockItem {
    data class Text(val content: String) : VennBlockItem()
    data class Diagram(val model: VennModel) : VennBlockItem()
}

@Composable
fun InteractiveVennCard(
    venn: VennModel,
    modifier: Modifier = Modifier
) {
    var selectedSectionName by remember { mutableStateOf<String?>(null) }
    var selectedSectionItems by remember { mutableStateOf<List<String>>(emptyList()) }

    val colorA = Color(0xFF3B82F6) // Blue
    val colorB = Color(0xFF10B981) // Green
    val colorC = Color(0xFFF59E0B) // Amber
    val colorOverlap = Color(0xFF8B5CF6) // Purple

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            val title = when (venn) {
                is VennModel.TwoSet -> venn.data.title
                is VennModel.ThreeSet -> venn.data.title
            }
            val subtitle = when (venn) {
                is VennModel.TwoSet -> "2-Set Venn Diagram"
                is VennModel.ThreeSet -> "3-Set Venn Diagram"
            }

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
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Interactive Details Box on Region Click
            AnimatedVisibility(visible = selectedSectionName != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedSectionName ?: "",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                text = "${selectedSectionItems.size} items",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (selectedSectionItems.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                selectedSectionItems.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = item,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No specific elements in this region",
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Venn Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                when (venn) {
                    is VennModel.TwoSet -> {
                        TwoSetVennCanvas(
                            data = venn.data,
                            colorA = colorA,
                            colorB = colorB,
                            colorOverlap = colorOverlap,
                            onSelectRegion = { name, items ->
                                selectedSectionName = name
                                selectedSectionItems = items
                            }
                        )
                    }
                    is VennModel.ThreeSet -> {
                        ThreeSetVennCanvas(
                            data = venn.data,
                            colorA = colorA,
                            colorB = colorB,
                            colorC = colorC,
                            onSelectRegion = { name, items ->
                                selectedSectionName = name
                                selectedSectionItems = items
                            }
                        )
                    }
                }
            }

            // Quick Selector Chips for Venn Regions
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tap any region to inspect contents:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (venn) {
                        is VennModel.TwoSet -> {
                            val d = venn.data
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "${d.setAName} (Only)"
                                    selectedSectionItems = d.setAItems
                                },
                                label = { Text("${d.setAName} (${d.setAItems.size})", fontSize = 11.sp) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorA, CircleShape)) }
                            )
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "Overlap: ${d.setAName} ∩ ${d.setBName}"
                                    selectedSectionItems = d.intersectionAB
                                },
                                label = { Text("Overlap (${d.intersectionAB.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorOverlap, CircleShape)) }
                            )
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "${d.setBName} (Only)"
                                    selectedSectionItems = d.setBItems
                                },
                                label = { Text("${d.setBName} (${d.setBItems.size})", fontSize = 11.sp) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorB, CircleShape)) }
                            )
                        }
                        is VennModel.ThreeSet -> {
                            val d = venn.data
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "${d.setAName} (Only)"
                                    selectedSectionItems = d.setAOnly
                                },
                                label = { Text(d.setAName, fontSize = 11.sp) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorA, CircleShape)) }
                            )
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "${d.setBName} (Only)"
                                    selectedSectionItems = d.setBOnly
                                },
                                label = { Text(d.setBName, fontSize = 11.sp) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorB, CircleShape)) }
                            )
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "${d.setCName} (Only)"
                                    selectedSectionItems = d.setCOnly
                                },
                                label = { Text(d.setCName, fontSize = 11.sp) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(colorC, CircleShape)) }
                            )
                            AssistChip(
                                onClick = {
                                    selectedSectionName = "Triple Overlap (A ∩ B ∩ C)"
                                    selectedSectionItems = d.intersectABC
                                },
                                label = { Text("Center (${d.intersectABC.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = { Box(Modifier.size(8.dp).background(Color(0xFFEC4899), CircleShape)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoSetVennCanvas(
    data: Venn2SetModel,
    colorA: Color,
    colorB: Color,
    colorOverlap: Color,
    onSelectRegion: (name: String, items: List<String>) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val r = h * 0.42f
                    val centerLeft = Offset(w * 0.38f, h * 0.5f)
                    val centerRight = Offset(w * 0.62f, h * 0.5f)

                    val dLeft = dist(offset, centerLeft)
                    val dRight = dist(offset, centerRight)

                    when {
                        dLeft <= r && dRight <= r -> {
                            onSelectRegion("Overlap (${data.setAName} ∩ ${data.setBName})", data.intersectionAB)
                        }
                        dLeft <= r -> {
                            onSelectRegion("${data.setAName} (Only)", data.setAItems)
                        }
                        dRight <= r -> {
                            onSelectRegion("${data.setBName} (Only)", data.setBItems)
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val r = h * 0.42f

        val centerLeft = Offset(w * 0.38f, h * 0.5f)
        val centerRight = Offset(w * 0.62f, h * 0.5f)

        // Circle A
        drawCircle(
            color = colorA.copy(alpha = 0.35f),
            radius = r,
            center = centerLeft
        )
        drawCircle(
            color = colorA,
            radius = r,
            center = centerLeft,
            style = Stroke(width = 2.dp.toPx())
        )

        // Circle B
        drawCircle(
            color = colorB.copy(alpha = 0.35f),
            radius = r,
            center = centerRight
        )
        drawCircle(
            color = colorB,
            radius = r,
            center = centerRight,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
private fun ThreeSetVennCanvas(
    data: Venn3SetModel,
    colorA: Color,
    colorB: Color,
    colorC: Color,
    onSelectRegion: (name: String, items: List<String>) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val w = size.width
                    val h = size.height
                    val r = h * 0.35f
                    val cTop = Offset(w * 0.5f, h * 0.38f)
                    val cLeft = Offset(w * 0.36f, h * 0.62f)
                    val cRight = Offset(w * 0.64f, h * 0.62f)

                    val dTop = dist(offset, cTop)
                    val dLeft = dist(offset, cLeft)
                    val dRight = dist(offset, cRight)

                    when {
                        dTop <= r && dLeft <= r && dRight <= r -> {
                            onSelectRegion("Triple Overlap (A ∩ B ∩ C)", data.intersectABC)
                        }
                        dTop <= r && dLeft <= r -> {
                            onSelectRegion("Overlap (A ∩ B)", data.intersectAB)
                        }
                        dTop <= r && dRight <= r -> {
                            onSelectRegion("Overlap (A ∩ C)", data.intersectAC)
                        }
                        dLeft <= r && dRight <= r -> {
                            onSelectRegion("Overlap (B ∩ C)", data.intersectBC)
                        }
                        dTop <= r -> {
                            onSelectRegion("${data.setAName} (Only)", data.setAOnly)
                        }
                        dLeft <= r -> {
                            onSelectRegion("${data.setBName} (Only)", data.setBOnly)
                        }
                        dRight <= r -> {
                            onSelectRegion("${data.setCName} (Only)", data.setCOnly)
                        }
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val r = h * 0.35f

        val cTop = Offset(w * 0.5f, h * 0.38f)
        val cLeft = Offset(w * 0.36f, h * 0.62f)
        val cRight = Offset(w * 0.64f, h * 0.62f)

        // Circle A (Top)
        drawCircle(
            color = colorA.copy(alpha = 0.30f),
            radius = r,
            center = cTop
        )
        drawCircle(
            color = colorA,
            radius = r,
            center = cTop,
            style = Stroke(width = 2.dp.toPx())
        )

        // Circle B (Bottom Left)
        drawCircle(
            color = colorB.copy(alpha = 0.30f),
            radius = r,
            center = cLeft
        )
        drawCircle(
            color = colorB,
            radius = r,
            center = cLeft,
            style = Stroke(width = 2.dp.toPx())
        )

        // Circle C (Bottom Right)
        drawCircle(
            color = colorC.copy(alpha = 0.30f),
            radius = r,
            center = cRight
        )
        drawCircle(
            color = colorC,
            radius = r,
            center = cRight,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Insert / Edit Venn Diagram Dialog
 */
val VENN_PRESETS = listOf(
    VennModel.TwoSet(
        Venn2SetModel(
            title = "Tech Stack Overlap",
            setAName = "Frontend Web",
            setBName = "Mobile Android",
            setAItems = listOf("HTML5", "CSS", "React", "Next.js"),
            setBItems = listOf("Kotlin", "Jetpack Compose", "Room DB", "Coroutines"),
            intersectionAB = listOf("REST APIs", "GraphQL", "UI State", "OAuth2")
        )
    ),
    VennModel.ThreeSet(
        Venn3SetModel(
            title = "Innovation Sweet Spot",
            setAName = "Desirability (People)",
            setBName = "Viability (Business)",
            setCName = "Feasibility (Technology)",
            setAOnly = listOf("Intuitive UX", "Community Delight"),
            setBOnly = listOf("Profitable ROI", "Sustainable CAC"),
            setCOnly = listOf("Scalable Backend", "Reliable Infrastructure"),
            intersectAB = listOf("Market Fit"),
            intersectAC = listOf("Prototype"),
            intersectBC = listOf("Sound Engineering"),
            intersectABC = listOf("Breakthrough Innovation")
        )
    )
)

@Composable
fun InsertVennDialog(
    onDismiss: () -> Unit,
    onInsertVenn: (markdown: String) -> Unit
) {
    var is3Set by remember { mutableStateOf(false) }

    // 2-Set state
    var title2 by remember { mutableStateOf("Tech Stack Overlap") }
    var setA2 by remember { mutableStateOf("Frontend") }
    var setB2 by remember { mutableStateOf("Backend") }
    var itemsA2 by remember { mutableStateOf("React, Jetpack Compose, CSS") }
    var itemsB2 by remember { mutableStateOf("Kotlin, PostgreSQL, Docker") }
    var overlap2 by remember { mutableStateOf("TypeScript, GraphQL, REST APIs") }

    // 3-Set state
    var title3 by remember { mutableStateOf("Innovation Sweet Spot") }
    var setA3 by remember { mutableStateOf("Desirability") }
    var setB3 by remember { mutableStateOf("Viability") }
    var setC3 by remember { mutableStateOf("Feasibility") }
    var itemsA3 by remember { mutableStateOf("Delightful UX, Accessibility") }
    var itemsB3 by remember { mutableStateOf("Monetization, High Retention") }
    var itemsC3 by remember { mutableStateOf("Scalable Cloud, Clean Architecture") }
    var overlapAB3 by remember { mutableStateOf("Market Fit") }
    var overlapAC3 by remember { mutableStateOf("Interactive Demo") }
    var overlapBC3 by remember { mutableStateOf("Profitable Service") }
    var overlapABC3 by remember { mutableStateOf("★ Innovation Sweet Spot ★") }

    val currentModel: VennModel = remember(
        is3Set, title2, setA2, setB2, itemsA2, itemsB2, overlap2,
        title3, setA3, setB3, setC3, itemsA3, itemsB3, itemsC3, overlapAB3, overlapAC3, overlapBC3, overlapABC3
    ) {
        if (!is3Set) {
            VennModel.TwoSet(
                Venn2SetModel(
                    title = title2.ifBlank { "2-Way Venn" },
                    setAName = setA2.ifBlank { "Set A" },
                    setBName = setB2.ifBlank { "Set B" },
                    setAItems = itemsA2.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    setBItems = itemsB2.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    intersectionAB = overlap2.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                )
            )
        } else {
            VennModel.ThreeSet(
                Venn3SetModel(
                    title = title3.ifBlank { "3-Way Venn" },
                    setAName = setA3.ifBlank { "Set A" },
                    setBName = setB3.ifBlank { "Set B" },
                    setCName = setC3.ifBlank { "Set C" },
                    setAOnly = itemsA3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    setBOnly = itemsB3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    setCOnly = itemsC3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    intersectAB = overlapAB3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    intersectAC = overlapAC3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    intersectBC = overlapBC3.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    intersectABC = overlapABC3.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                )
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AllInclusive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Insert Venn Diagram",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Mode Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !is3Set,
                        onClick = { is3Set = false },
                        label = { Text("2-Set Diagram", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = is3Set,
                        onClick = { is3Set = true },
                        label = { Text("3-Set Diagram", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (!is3Set) {
                    OutlinedTextField(
                        value = title2,
                        onValueChange = { title2 = it },
                        label = { Text("Diagram Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = setA2,
                            onValueChange = { setA2 = it },
                            label = { Text("Set A Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = setB2,
                            onValueChange = { setB2 = it },
                            label = { Text("Set B Name") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = itemsA2,
                        onValueChange = { itemsA2 = it },
                        label = { Text("Set A Items (comma separated)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = itemsB2,
                        onValueChange = { itemsB2 = it },
                        label = { Text("Set B Items (comma separated)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = overlap2,
                        onValueChange = { overlap2 = it },
                        label = { Text("Overlap (Intersection A ∩ B)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    OutlinedTextField(
                        value = title3,
                        onValueChange = { title3 = it },
                        label = { Text("Diagram Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = setA3,
                            onValueChange = { setA3 = it },
                            label = { Text("Set A") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = setB3,
                            onValueChange = { setB3 = it },
                            label = { Text("Set B") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = setC3,
                            onValueChange = { setC3 = it },
                            label = { Text("Set C") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = overlapABC3,
                        onValueChange = { overlapABC3 = it },
                        label = { Text("Center Triple Intersection (A ∩ B ∩ C)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Live Preview
                Text("Live Interactive Preview:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                InteractiveVennCard(venn = currentModel)

                // Insert button
                Button(
                    onClick = {
                        val markdown = VennParser.toMarkdown(currentModel)
                        onInsertVenn(markdown)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert Venn Diagram into Post")
                }
            }
        }
    }
}
