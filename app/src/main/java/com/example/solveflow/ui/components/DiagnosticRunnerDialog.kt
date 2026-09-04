package com.example.solveflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.DiagnosticStep
import com.example.solveflow.data.model.FlowBranch
import com.example.solveflow.data.model.FlowNode
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import com.example.solveflow.ui.theme.Emerald400
import com.example.solveflow.ui.theme.Emerald500
import com.example.solveflow.ui.theme.Rose500
import java.util.UUID

@Composable
fun DiagnosticRunnerDialog(
    flowchart: FlowchartData,
    onDismiss: () -> Unit,
    onSaveSession: (DiagnosticSession) -> Unit,
    onExportHtml: (DiagnosticSession) -> Unit,
    onExportPdf: (DiagnosticSession) -> Unit
) {
    var currentNodeId by remember { mutableStateOf(flowchart.rootNodeId) }
    val steps = remember { mutableStateListOf<DiagnosticStep>() }

    val currentNode = flowchart.nodes.find { it.id == currentNodeId }
        ?: flowchart.nodes.firstOrNull()

    val isFinished = currentNode?.type == NodeType.OUTCOME_SUCCESS || currentNode?.type == NodeType.OUTCOME_ESCALATE || currentNode?.branches.isNullOrEmpty()

    fun resetDiagnostic() {
        currentNodeId = flowchart.rootNodeId
        steps.clear()
    }

    fun makeChoice(branch: FlowBranch) {
        if (currentNode != null) {
            steps.add(
                DiagnosticStep(
                    nodeId = currentNode.id,
                    nodeTitle = currentNode.title,
                    nodeType = currentNode.type,
                    chosenBranchLabel = branch.label
                )
            )
            currentNodeId = branch.targetNodeId
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DIAGNOSTIC SOLVER MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = flowchart.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stepper progress indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step Path Depth: ${steps.size + 1}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { resetDiagnostic() },
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restart", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentNode != null) {
                    val (typeColor, typeIcon, typeName) = getNodePresentation(currentNode.type)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, typeColor, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(typeColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = typeIcon,
                                        contentDescription = null,
                                        tint = typeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = typeName,
                                    color = typeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentNode.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (currentNode.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentNode.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isFinished) {
                                Spacer(modifier = Modifier.height(16.dp))
                                val isSuccess = currentNode.type == NodeType.OUTCOME_SUCCESS
                                val bannerColor = if (isSuccess) Emerald500 else Rose500

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = bannerColor.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, bannerColor.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = bannerColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = if (isSuccess) "DIAGNOSTIC RESOLUTION REACHED" else "ESCALATION REQUIRED",
                                                fontWeight = FontWeight.Bold,
                                                color = bannerColor,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Audit trail verified with ${steps.size} investigation steps.",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val session = remember(currentNodeId, steps.size) {
                                    DiagnosticSession(
                                        id = UUID.randomUUID().toString(),
                                        flowchartId = flowchart.id,
                                        flowchartTitle = flowchart.title,
                                        steps = steps.toList() + DiagnosticStep(
                                            nodeId = currentNode.id,
                                            nodeTitle = currentNode.title,
                                            nodeType = currentNode.type,
                                            chosenBranchLabel = null
                                        ),
                                        finalStatus = if (isSuccess) "Resolved" else "Escalated"
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onSaveSession(session)
                                            onExportHtml(session)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Emerald500,
                                            contentColor = Color(0xFF022C22)
                                        )
                                    ) {
                                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Export HTML", color = Color(0xFF022C22), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            onSaveSession(session)
                                            onExportPdf(session)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Emerald500,
                                            contentColor = Color(0xFF022C22)
                                        )
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Export PDF", color = Color(0xFF022C22), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    if (!isFinished && currentNode.branches.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "SELECT DIAGNOSTIC OBSERVATION / OUTCOME:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            currentNode.branches.forEach { branch ->
                                Button(
                                    onClick = { makeChoice(branch) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = branch.label,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = Emerald400,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Audit trail history
                if (steps.isNotEmpty()) {
                    Text(
                        text = "INVESTIGATION AUDIT TRAIL (${steps.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(steps) { idx, step ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Emerald400.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald400
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = step.nodeTitle,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (step.chosenBranchLabel != null) {
                                        Text(
                                            text = "Selected: ${step.chosenBranchLabel}",
                                            fontSize = 11.sp,
                                            color = Emerald400
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
