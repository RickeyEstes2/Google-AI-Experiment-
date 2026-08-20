package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class ChartType(val displayName: String) {
    BAR("Bar Chart"),
    LINE("Line Chart"),
    AREA("Area Chart"),
    PIE("Pie / Donut")
}

data class ChartDataModel(
    val type: ChartType = ChartType.BAR,
    val title: String = "Growth Metric",
    val labels: List<String> = listOf("Jan", "Feb", "Mar", "Apr", "May"),
    val values: List<Float> = listOf(25f, 40f, 65f, 50f, 85f),
    val unit: String = ""
)

object ChartParser {

    private val CHART_BLOCK_REGEX = Regex("```chart:(bar|line|area|pie)\\s*([\\s\\S]*?)```", RegexOption.IGNORE_CASE)

    fun containsChart(text: String): Boolean = CHART_BLOCK_REGEX.containsMatchIn(text)

    fun parseCharts(text: String): List<ChartBlockItem> {
        val items = mutableListOf<ChartBlockItem>()
        var lastIndex = 0

        CHART_BLOCK_REGEX.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            if (start > lastIndex) {
                items.add(ChartBlockItem.Text(text.substring(lastIndex, start)))
            }

            val typeStr = match.groupValues[1].lowercase()
            val body = match.groupValues[2]

            val type = when (typeStr) {
                "bar" -> ChartType.BAR
                "line" -> ChartType.LINE
                "area" -> ChartType.AREA
                "pie" -> ChartType.PIE
                else -> ChartType.BAR
            }

            var title = "Chart"
            var labels = listOf<String>()
            var values = listOf<Float>()
            var unit = ""

            body.lines().forEach { line ->
                val trimmed = line.trim()
                when {
                    trimmed.startsWith("title:", ignoreCase = true) -> {
                        title = trimmed.substringAfter(":").trim()
                    }
                    trimmed.startsWith("labels:", ignoreCase = true) -> {
                        labels = trimmed.substringAfter(":").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    trimmed.startsWith("values:", ignoreCase = true) -> {
                        values = trimmed.substringAfter(":").split(",").mapNotNull { it.trim().toFloatOrNull() }
                    }
                    trimmed.startsWith("unit:", ignoreCase = true) -> {
                        unit = trimmed.substringAfter(":").trim()
                    }
                }
            }

            if (labels.isNotEmpty() && values.isNotEmpty()) {
                val safeValues = if (values.size < labels.size) {
                    values + List(labels.size - values.size) { 0f }
                } else {
                    values.take(labels.size)
                }
                items.add(ChartBlockItem.Chart(ChartDataModel(type, title, labels, safeValues, unit)))
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            items.add(ChartBlockItem.Text(text.substring(lastIndex)))
        }

        return if (items.isEmpty()) listOf(ChartBlockItem.Text(text)) else items
    }

    fun toMarkdown(chart: ChartDataModel): String {
        val typeStr = chart.type.name.lowercase()
        return "```chart:$typeStr\n" +
                "title: ${chart.title}\n" +
                "labels: ${chart.labels.joinToString(", ")}\n" +
                "values: ${chart.values.map { if (it % 1f == 0f) it.toInt().toString() else it.toString() }.joinToString(", ")}\n" +
                (if (chart.unit.isNotBlank()) "unit: ${chart.unit}\n" else "") +
                "```\n"
    }
}

sealed class ChartBlockItem {
    data class Text(val content: String) : ChartBlockItem()
    data class Chart(val model: ChartDataModel) : ChartBlockItem()
}

val CHART_PALETTE = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF6366F1)  // Indigo
)

@Composable
fun InteractiveChartCard(
    chart: ChartDataModel,
    modifier: Modifier = Modifier,
    onEditChart: (() -> Unit)? = null
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface

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
            // Header: Title & Chart type badge
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
                                imageVector = when (chart.type) {
                                    ChartType.BAR -> Icons.Default.BarChart
                                    ChartType.LINE -> Icons.Default.ShowChart
                                    ChartType.AREA -> Icons.Default.MultilineChart
                                    ChartType.PIE -> Icons.Default.PieChart
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = chart.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = chart.type.displayName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Interactive Tooltip Info Banner
            AnimatedVisibility(visible = selectedIndex != null) {
                selectedIndex?.let { idx ->
                    if (idx in chart.labels.indices && idx in chart.values.indices) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${chart.labels[idx]}:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${chart.values[idx]}${if (chart.unit.isNotBlank()) " " + chart.unit else ""}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Canvas Chart Rendering
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                when (chart.type) {
                    ChartType.BAR -> BarChartCanvas(
                        chart = chart,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { selectedIndex = it }
                    )
                    ChartType.LINE -> LineChartCanvas(
                        chart = chart,
                        isArea = false,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { selectedIndex = it }
                    )
                    ChartType.AREA -> LineChartCanvas(
                        chart = chart,
                        isArea = true,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { selectedIndex = it }
                    )
                    ChartType.PIE -> PieChartCanvas(
                        chart = chart,
                        selectedIndex = selectedIndex,
                        onSelectIndex = { selectedIndex = it }
                    )
                }
            }

            // Chart Legends
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                chart.labels.forEachIndexed { index, label ->
                    val color = CHART_PALETTE[index % CHART_PALETTE.size]
                    val isSelected = selectedIndex == index
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable {
                                selectedIndex = if (selectedIndex == index) null else index
                            }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color, CircleShape)
                                .then(if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                        )
                        Text(
                            text = "$label (${chart.values.getOrNull(index) ?: 0f}${chart.unit})",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChartCanvas(
    chart: ChartDataModel,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit
) {
    val labels = chart.labels
    val values = chart.values
    val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(labels, values) {
                detectTapGestures { offset ->
                    val barSpacing = size.width / labels.size
                    val clickedIdx = (offset.x / barSpacing).toInt().coerceIn(0, labels.size - 1)
                    onSelectIndex(clickedIdx)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val barCount = labels.size
        if (barCount == 0) return@Canvas

        val slotWidth = width / barCount
        val barWidth = slotWidth * 0.58f

        // Draw horizontal grid lines
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height - (height * (i.toFloat() / gridLines))
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw Bars
        values.forEachIndexed { i, value ->
            val barHeight = (value / maxVal) * (height - 20.dp.toPx())
            val x = i * slotWidth + (slotWidth - barWidth) / 2f
            val y = height - barHeight
            val color = CHART_PALETTE[i % CHART_PALETTE.size]
            val isSelected = selectedIndex == i

            // Bar shape
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = if (isSelected) listOf(color, color.copy(alpha = 0.7f)) else listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.5f))
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
            )

            if (isSelected) {
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun LineChartCanvas(
    chart: ChartDataModel,
    isArea: Boolean,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit
) {
    val labels = chart.labels
    val values = chart.values
    val maxVal = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val minVal = (values.minOrNull() ?: 0f).coerceAtMost(0f)
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(labels, values) {
                detectTapGestures { offset ->
                    val stepX = size.width / (labels.size - 1).coerceAtLeast(1)
                    val clickedIdx = ((offset.x + stepX / 2f) / stepX).toInt().coerceIn(0, labels.size - 1)
                    onSelectIndex(clickedIdx)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val count = values.size
        if (count < 2) return@Canvas

        val stepX = width / (count - 1)
        val path = Path()
        val areaPath = Path()

        val points = values.mapIndexed { index, value ->
            val normY = (value - minVal) / range
            val x = index * stepX
            val y = height - (normY * (height - 30.dp.toPx())) - 10.dp.toPx()
            Offset(x, y)
        }

        // Draw smooth line
        path.moveTo(points[0].x, points[0].y)
        areaPath.moveTo(points[0].x, points[0].y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
            path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            areaPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
        }

        if (isArea) {
            areaPath.lineTo(points.last().x, height)
            areaPath.lineTo(points.first().x, height)
            areaPath.close()

            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF3B82F6).copy(alpha = 0.45f), Color(0xFF3B82F6).copy(alpha = 0.05f))
                )
            )
        }

        // Draw Line Stroke
        drawPath(
            path = path,
            color = Color(0xFF2563EB),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw Dots
        points.forEachIndexed { i, pt ->
            val isSelected = selectedIndex == i
            val dotColor = CHART_PALETTE[i % CHART_PALETTE.size]
            drawCircle(
                color = if (isSelected) Color.White else dotColor,
                radius = if (isSelected) 7.dp.toPx() else 4.5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = dotColor,
                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
private fun PieChartCanvas(
    chart: ChartDataModel,
    selectedIndex: Int?,
    onSelectIndex: (Int?) -> Unit
) {
    val labels = chart.labels
    val values = chart.values
    val total = values.sum().coerceAtLeast(1f)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(labels, values) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    var angle = Math.toDegrees(Math.atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angle < 0) angle += 360f

                    var currentAngle = -90f
                    if (currentAngle < 0) currentAngle += 360f

                    var found: Int? = null
                    var sweepStart = 0f
                    values.forEachIndexed { idx, v ->
                        val sweep = (v / total) * 360f
                        val end = sweepStart + sweep
                        val normAngle = (angle + 90f) % 360f
                        if (normAngle >= sweepStart && normAngle < end) {
                            found = idx
                        }
                        sweepStart = end
                    }
                    onSelectIndex(found)
                }
            }
    ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = min(size.width, size.height) / 2f - 10.dp.toPx()
        val innerRadius = radius * 0.52f

        var startAngle = -90f

        values.forEachIndexed { index, value ->
            val sweepAngle = (value / total) * 360f
            val color = CHART_PALETTE[index % CHART_PALETTE.size]
            val isSelected = selectedIndex == index

            val currentRadius = if (isSelected) radius + 6.dp.toPx() else radius

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - currentRadius, center.y - currentRadius),
                size = Size(currentRadius * 2, currentRadius * 2)
            )

            startAngle += sweepAngle
        }

        // Cut out center for Donut hole
        drawCircle(
            color = Color(0xFF1E293B).copy(alpha = 0.9f),
            radius = innerRadius,
            center = center
        )
    }
}

/**
 * Insert / Edit Chart Dialog with Live Preview and Presets
 */
val CHART_PRESETS = listOf(
    ChartDataModel(ChartType.BAR, "Monthly Pageviews", listOf("Jan", "Feb", "Mar", "Apr", "May"), listOf(120f, 190f, 300f, 250f, 420f), "k"),
    ChartDataModel(ChartType.LINE, "Active Users Trend", listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"), listOf(45f, 60f, 55f, 85f, 90f, 110f, 130f), "users"),
    ChartDataModel(ChartType.AREA, "Bandwidth Usage", listOf("00:00", "06:00", "12:00", "18:00", "23:59"), listOf(15f, 35f, 80f, 95f, 40f), "GB"),
    ChartDataModel(ChartType.PIE, "Browser Market Share", listOf("Chrome", "Safari", "Edge", "Firefox"), listOf(65f, 20f, 10f, 5f), "%")
)

@Composable
fun InsertChartDialog(
    onDismiss: () -> Unit,
    onInsertChart: (markdown: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(ChartType.BAR) }
    var titleInput by remember { mutableStateOf("Metrics Overview") }
    var labelsInput by remember { mutableStateOf("Jan, Feb, Mar, Apr, May") }
    var valuesInput by remember { mutableStateOf("25, 45, 60, 80, 110") }
    var unitInput by remember { mutableStateOf("") }

    val currentModel = remember(selectedType, titleInput, labelsInput, valuesInput, unitInput) {
        val labels = labelsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val values = valuesInput.split(",").mapNotNull { it.trim().toFloatOrNull() }
        val safeValues = if (values.size < labels.size) {
            values + List(labels.size - values.size) { 10f }
        } else {
            values.take(labels.size)
        }
        ChartDataModel(selectedType, titleInput.ifBlank { "Chart" }, labels, safeValues, unitInput)
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
                                    imageVector = Icons.Default.InsertChart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Text(
                            text = "Insert Interactive Chart",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Chart Type Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChartType.values().forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CHART_PRESETS.forEach { preset ->
                        SuggestionChip(
                            onClick = {
                                selectedType = preset.type
                                titleInput = preset.title
                                labelsInput = preset.labels.joinToString(", ")
                                valuesInput = preset.values.map { it.toInt() }.joinToString(", ")
                                unitInput = preset.unit
                            },
                            label = { Text(preset.title, fontSize = 11.sp) }
                        )
                    }
                }

                // Title & Unit Fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Chart Title") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(2f)
                    )
                    OutlinedTextField(
                        value = unitInput,
                        onValueChange = { unitInput = it },
                        label = { Text("Unit (e.g. $, %, k)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Labels & Values Input
                OutlinedTextField(
                    value = labelsInput,
                    onValueChange = { labelsInput = it },
                    label = { Text("Labels (comma separated)") },
                    placeholder = { Text("e.g. Chrome, Safari, Edge, Firefox") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = valuesInput,
                    onValueChange = { valuesInput = it },
                    label = { Text("Values (comma separated numbers)") },
                    placeholder = { Text("e.g. 65, 20, 10, 5") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Interactive Preview
                Text("Live Interactive Preview:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                InteractiveChartCard(chart = currentModel)

                // Action Button
                Button(
                    onClick = {
                        val markdown = ChartParser.toMarkdown(currentModel)
                        onInsertChart(markdown)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Insert Chart into Post")
                }
            }
        }
    }
}
