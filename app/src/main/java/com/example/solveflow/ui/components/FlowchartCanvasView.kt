package com.example.solveflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solveflow.data.model.FlowBranch
import com.example.solveflow.data.model.FlowNode
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import com.example.solveflow.ui.theme.Amber500
import com.example.solveflow.ui.theme.Emerald400
import com.example.solveflow.ui.theme.Emerald500
import com.example.solveflow.ui.theme.Rose500
import com.example.solveflow.ui.theme.Violet400

@Composable
fun FlowchartListView(
    flowchart: FlowchartData,
    activeNodeId: String? = null,
    onNodeClick: (FlowNode) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(flowchart.nodes, key = { it.id }) { node ->
            val isActive = node.id == activeNodeId
            FlowNodeCard(
                node = node,
                flowchart = flowchart,
                isActive = isActive,
                onClick = { onNodeClick(node) }
            )
        }
    }
}

@Composable
fun FlowNodeCard(
    node: FlowNode,
    flowchart: FlowchartData,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val (typeColor, typeIcon, typeName) = getNodePresentation(node.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = if (isActive) 2.5.dp else 1.dp,
                color = if (isActive) Emerald400 else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = typeName,
                        tint = typeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Step ${node.stepNumber} • $typeName",
                        color = typeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "#${node.id}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (node.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (node.branches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DECISION BRANCHES & NEXT PATHS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    node.branches.forEach { branch ->
                        BranchRow(branch = branch, flowchart = flowchart)
                    }
                }
            }
        }
    }
}

@Composable
fun BranchRow(branch: FlowBranch, flowchart: FlowchartData) {
    val target = flowchart.nodes.find { it.id == branch.targetNodeId }
    val targetTitle = target?.title ?: branch.targetNodeId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Emerald400.copy(alpha = 0.2f)
        ) {
            Text(
                text = branch.label,
                color = Emerald400,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = targetTitle,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

fun getNodePresentation(type: NodeType): Triple<Color, ImageVector, String> {
    return when (type) {
        NodeType.START -> Triple(Emerald400, Icons.Default.PlayArrow, "Problem Statement")
        NodeType.DECISION -> Triple(Violet400, Icons.Default.HelpOutline, "Decision / Check")
        NodeType.ACTION -> Triple(Amber500, Icons.Default.Warning, "Diagnostic Action")
        NodeType.OUTCOME_SUCCESS -> Triple(Emerald500, Icons.Default.CheckCircle, "Problem Resolved")
        NodeType.OUTCOME_ESCALATE -> Triple(Rose500, Icons.Default.Warning, "Escalate / Failover")
        NodeType.NOTE -> Triple(Color(0xFF94A3B8), Icons.Default.HelpOutline, "Note")
    }
}
