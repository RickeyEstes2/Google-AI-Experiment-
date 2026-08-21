package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartBarData(val label: String, val value: Float, val color: Color)

@Composable
fun InteractiveBarChart(
    title: String,
    data: List<ChartBarData>,
    modifier: Modifier = Modifier
) {
    var selectedBar by remember { mutableStateOf<ChartBarData?>(null) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                selectedBar?.let { bar ->
                    Text(
                        text = "${bar.label}: ${bar.value.toInt()}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            val maxValue = (data.maxOfOrNull { it.value } ?: 100f).coerceAtLeast(10f)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(data) {
                        detectTapGestures { offset ->
                            val barSlotWidth = size.width / data.size
                            val index = (offset.x / barSlotWidth).toInt().coerceIn(0, data.size - 1)
                            selectedBar = data.getOrNull(index)
                        }
                    }
            ) {
                val barWidth = (size.width / (data.size * 1.6f)).coerceAtMost(48.dp.toPx())
                val slotWidth = size.width / data.size
                val chartHeight = size.height - 30.dp.toPx()

                // Baseline
                drawLine(
                    color = Color.Gray.copy(alpha = 0.3f),
                    start = Offset(0f, chartHeight),
                    end = Offset(size.width, chartHeight),
                    strokeWidth = 2f
                )

                data.forEachIndexed { i, item ->
                    val barHeight = (item.value / maxValue) * (chartHeight - 15.dp.toPx())
                    val x = i * slotWidth + (slotWidth - barWidth) / 2
                    val y = chartHeight - barHeight

                    val isSelected = selectedBar == item

                    drawRoundRect(
                        color = if (isSelected) primaryColor else item.color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                    )
                }
            }

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { item ->
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        color = if (selectedBar == item) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (selectedBar == item) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
