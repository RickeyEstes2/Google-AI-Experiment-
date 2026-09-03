package com.example.solveflow.data.repository

import com.example.solveflow.data.db.DiagnosticRunEntity
import com.example.solveflow.data.db.FlowchartDao
import com.example.solveflow.data.db.FlowchartEntity
import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.FlowNode
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.templates.DefaultFlowcharts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FlowchartRepository(private val dao: FlowchartDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    suspend fun ensureDefaultTemplates() {
        val count = dao.getCount()
        if (count == 0) {
            val entities = DefaultFlowcharts.allTemplates.map { flowchartToEntity(it) }
            dao.insertAll(entities)
        }
    }

    fun getAllFlowcharts(): Flow<List<FlowchartData>> {
        return dao.getAllFlowchartsFlow().map { entities ->
            entities.map { entityToFlowchart(it) }
        }
    }

    suspend fun getFlowchartById(id: String): FlowchartData? {
        val entity = dao.getFlowchartById(id) ?: return null
        return entityToFlowchart(entity)
    }

    suspend fun saveFlowchart(flowchart: FlowchartData) {
        dao.insert(flowchartToEntity(flowchart))
    }

    suspend fun deleteFlowchart(id: String) {
        dao.deleteById(id)
    }

    suspend fun resetToDefaultTemplates() {
        dao.deleteAllCustom()
        val entities = DefaultFlowcharts.allTemplates.map { flowchartToEntity(it) }
        dao.insertAll(entities)
    }

    fun getAllDiagnosticRuns(): Flow<List<DiagnosticSession>> {
        return dao.getAllDiagnosticRunsFlow().map { list ->
            list.map { entity ->
                DiagnosticSession(
                    id = entity.id,
                    flowchartId = entity.flowchartId,
                    flowchartTitle = entity.flowchartTitle,
                    steps = runCatching { json.decodeFromString<List<com.example.solveflow.data.model.DiagnosticStep>>(entity.stepsJson) }.getOrDefault(emptyList()),
                    finalStatus = entity.finalStatus,
                    notes = entity.notes,
                    completedAt = entity.completedAt
                )
            }
        }
    }

    suspend fun saveDiagnosticRun(session: DiagnosticSession) {
        val entity = DiagnosticRunEntity(
            id = session.id,
            flowchartId = session.flowchartId,
            flowchartTitle = session.flowchartTitle,
            stepsJson = json.encodeToString(session.steps),
            finalStatus = session.finalStatus,
            notes = session.notes,
            completedAt = session.completedAt
        )
        dao.insertRun(entity)
    }

    private fun flowchartToEntity(data: FlowchartData): FlowchartEntity {
        return FlowchartEntity(
            id = data.id,
            title = data.title,
            description = data.description,
            category = data.category,
            rootNodeId = data.rootNodeId,
            nodesJson = json.encodeToString(data.nodes),
            isTemplate = data.isTemplate,
            createdAt = data.createdAt,
            updatedAt = data.updatedAt
        )
    }

    private fun entityToFlowchart(entity: FlowchartEntity): FlowchartData {
        val nodes = runCatching {
            json.decodeFromString<List<FlowNode>>(entity.nodesJson)
        }.getOrDefault(emptyList())

        return FlowchartData(
            id = entity.id,
            title = entity.title,
            description = entity.description,
            category = entity.category,
            rootNodeId = entity.rootNodeId,
            nodes = nodes,
            isTemplate = entity.isTemplate,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }
}
