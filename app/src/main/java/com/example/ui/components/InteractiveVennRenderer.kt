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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InteractiveVennDiagram(
    title: String = "Set Relationship (A ∩ B)",
    setALabel: String = "Set A",
    setBLabel: String = "Set B",
    intersectionLabel: String = "Intersection (A ∩ B)",
    modifier: Modifier = Modifier
) {
    var selectedRegion by remember { mutableStateOf<String?>(null) }
    val colorA = Color(0xFF3B82F6).copy(alpha = 0.4f)
    val colorB = Color(0xFF10B981).copy(alpha = 0.4f)
    val borderA = Color(0xFF2563EB)
    val borderB = Color(0xFF059669)

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
                Text(
                    text = selectedRegion ?: "Tap a region to inspect",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val radius = size.height * 0.42f
                            val distance = radius * 0.65f

                            val cAX = centerX - distance
                            val cBX = centerX + distance

                            val dA = kotlin.math.hypot(offset.x - cAX, offset.y - centerY)
                            val dB = kotlin.math.hypot(offset.x - cBX, offset.y - centerY)

                            selectedRegion = when {
                                dA <= radius && dB <= radius -> intersectionLabel
                                dA <= radius -> "$setALabel (Exclusive)"
                                dB <= radius -> "$setBLabel (Exclusive)"
                                else -> "Outside"
                            }
                        }
                    }
            ) {
                val centerX = size.width / 2f
                val centerY = size.height / 2f
                val radius = size.height * 0.42f
                val distance = radius * 0.65f

                val centerA = Offset(centerX - distance, centerY)
                val centerB = Offset(centerX + distance, centerY)

                // Fill Set A
                drawCircle(color = colorA, radius = radius, center = centerA)
                // Fill Set B
                drawCircle(color = colorB, radius = radius, center = centerB)

                // Stroke Set A
                drawCircle(color = borderA, radius = radius, center = centerA, style = Stroke(width = 3f))
                // Stroke Set B
                drawCircle(color = borderB, radius = radius, center = centerB, style = Stroke(width = 3f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(text = "🔵 $setALabel", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "🟢 $setBLabel", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = "🟡 Intersection", fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
