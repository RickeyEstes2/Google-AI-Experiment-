package com.example.solveflow.ui.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.solveflow.data.model.FlowBranch
import com.example.solveflow.data.model.FlowNode
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.model.NodeType
import com.example.solveflow.ui.theme.Sky400
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFlowchartDialog(
    initialFlowchart: FlowchartData? = null,
    onDismiss: () -> Unit,
    onSave: (FlowchartData) -> Unit
) {
    var title by remember { mutableStateOf(initialFlowchart?.title ?: "") }
    var description by remember { mutableStateOf(initialFlowchart?.description ?: "") }
    var category by remember { mutableStateOf(initialFlowchart?.category ?: "Custom Diagnostic") }

    val nodes = remember {
        mutableStateListOf<FlowNode>().apply {
            if (initialFlowchart != null) {
                addAll(initialFlowchart.nodes)
            } else {
                // Default starter nodes
                add(
                    FlowNode(
                        id = "node_start",
                        type = NodeType.START,
                        title = "Problem Statement: Describe Symptom",
                        description = "Initial observation or failure trigger",
                        branches = listOf(FlowBranch("b1", "Proceed", "node_check")),
                        stepNumber = 1
                    )
                )
                add(
                    FlowNode(
                        id = "node_check",
                        type = NodeType.DECISION,
                        title = "Check Primary Indicator: Is it functioning?",
                        description = "Perform initial verification test",
                        branches = listOf(
                            FlowBranch("b2_yes", "Yes (Working)", "node_resolved"),
                            FlowBranch("b2_no", "No (Failing)", "node_action")
                        ),
                        stepNumber = 2
                    )
                )
                add(
                    FlowNode(
                        id = "node_action",
                        type = NodeType.ACTION,
                        title = "Execute Remedial Action / Reset",
                        description = "Perform standard corrective step",
                        branches = listOf(
                            FlowBranch("b3_fixed", "Fixed", "node_resolved"),
                            FlowBranch("b3_persist", "Still Failing", "node_escalate")
                        ),
                        stepNumber = 3
                    )
                )
                add(
                    FlowNode(
                        id = "node_resolved",
                        type = NodeType.OUTCOME_SUCCESS,
                        title = "Problem Successfully Resolved",
                        description = "Verification complete",
                        branches = emptyList(),
                        stepNumber = 4
                    )
                )
                add(
                    FlowNode(
                        id = "node_escalate",
                        type = NodeType.OUTCOME_ESCALATE,
                        title = "Escalate to Senior Specialist",
                        description = "Unresolved via standard procedures",
                        branches = emptyList(),
                        stepNumber = 5
                    )
                )
            }
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
                            text = if (initialFlowchart == null) "NEW FLOWCHART" else "EDIT FLOWCHART",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Sky400,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (initialFlowchart == null) "Create Diagnostic Tree" else "Modify Flowchart",
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

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Flowchart Title") },
                    placeholder = { Text("e.g. Database Connection Failure") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Problem Description & Scope") },
                    placeholder = { Text("Symptoms, affected components, and troubleshooting goals") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Software, IT, Operations, Hardware") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLOWCHART NODES (${nodes.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            val newId = "node_${UUID.randomUUID().toString().take(6)}"
                            nodes.add(
                                FlowNode(
                                    id = newId,
                                    type = NodeType.ACTION,
                                    title = "New Diagnostic Step",
                                    description = "",
                                    branches = emptyList(),
                                    stepNumber = nodes.size + 1
                                )
                            )
                        },
                        modifier = Modifier.height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Node", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(nodes) { index, node ->
                        EditNodeCard(
                            node = node,
                            stepIndex = index + 1,
                            onUpdateNode = { updated -> nodes[index] = updated },
                            onDeleteNode = { if (nodes.size > 1) nodes.removeAt(index) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && nodes.isNotEmpty()) {
                                val flowchart = FlowchartData(
                                    id = initialFlowchart?.id ?: "fc_${UUID.randomUUID().toString().take(8)}",
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = category.trim(),
                                    rootNodeId = nodes.firstOrNull()?.id ?: "start",
                                    nodes = nodes.mapIndexed { idx, n -> n.copy(stepNumber = idx + 1) },
                                    isTemplate = false,
                                    createdAt = initialFlowchart?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(flowchart)
                            }
                        },
                        enabled = title.isNotBlank() && nodes.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Sky400)
                    ) {
                        Text("Save Flowchart", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNodeCard(
    node: FlowNode,
    stepIndex: Int,
    onUpdateNode: (FlowNode) -> Unit,
    onDeleteNode: () -> Unit
) {
    var expandedType by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $stepIndex",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Sky400
                )
                Spacer(modifier = Modifier.width(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = node.type.name,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        NodeType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name, fontSize = 12.sp) },
                                onClick = {
                                    onUpdateNode(node.copy(type = type))
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                IconButton(onClick = onDeleteNode) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Node", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = node.title,
                onValueChange = { onUpdateNode(node.copy(title = it)) },
                label = { Text("Step Title / Query") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = node.description,
                onValueChange = { onUpdateNode(node.copy(description = it)) },
                label = { Text("Diagnostic Details / Action Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}
