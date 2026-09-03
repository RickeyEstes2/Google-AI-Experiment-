package com.example.solveflow.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flowcharts")
data class FlowchartEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val rootNodeId: String,
    val nodesJson: String,
    val isTemplate: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "diagnostic_runs")
data class DiagnosticRunEntity(
    @PrimaryKey val id: String,
    val flowchartId: String,
    val flowchartTitle: String,
    val stepsJson: String,
    val finalStatus: String,
    val notes: String,
    val completedAt: Long
)
