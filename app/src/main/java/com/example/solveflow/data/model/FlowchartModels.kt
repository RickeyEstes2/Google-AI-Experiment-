package com.example.solveflow.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NodeType {
    START,
    DECISION,
    ACTION,
    OUTCOME_SUCCESS,
    OUTCOME_ESCALATE,
    NOTE
}

@Serializable
data class FlowBranch(
    val id: String,
    val label: String,
    val targetNodeId: String,
    val isRecommended: Boolean = false
)

@Serializable
data class FlowNode(
    val id: String,
    val type: NodeType,
    val title: String,
    val description: String = "",
    val branches: List<FlowBranch> = emptyList(),
    val stepNumber: Int = 1
)

@Serializable
data class FlowchartData(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rootNodeId: String,
    val nodes: List<FlowNode>,
    val isTemplate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class DiagnosticStep(
    val nodeId: String,
    val nodeTitle: String,
    val nodeType: NodeType,
    val chosenBranchLabel: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class DiagnosticSession(
    val id: String,
    val flowchartId: String,
    val flowchartTitle: String,
    val steps: List<DiagnosticStep>,
    val finalStatus: String,
    val notes: String = "",
    val completedAt: Long = System.currentTimeMillis()
)
