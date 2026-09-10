package com.gptbot.gptbot.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gptbot.gptbot.data.model.DiagnosticSession
import com.gptbot.gptbot.data.model.FlowchartData
import com.gptbot.gptbot.data.repository.FlowchartRepository
import com.gptbot.gptbot.export.ExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlowchartViewModel(
    private val repository: FlowchartRepository
) : ViewModel() {

    val allFlowcharts: StateFlow<List<FlowchartData>> = repository.getAllFlowcharts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diagnosticHistory: StateFlow<List<DiagnosticSession>> = repository.getAllDiagnosticRuns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    val filteredFlowcharts: StateFlow<List<FlowchartData>> = combine(
        allFlowcharts,
        searchQuery,
        selectedCategory
    ) { charts, query, category ->
        charts.filter { chart ->
            val matchesCategory = (category == "All" || chart.category.equals(category, ignoreCase = true))
            val matchesQuery = query.isBlank() ||
                chart.title.contains(query, ignoreCase = true) ||
                chart.description.contains(query, ignoreCase = true) ||
                chart.nodes.any { it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearch(query: String) {
        searchQuery.value = query
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    fun saveFlowchart(flowchart: FlowchartData) {
        viewModelScope.launch {
            repository.saveFlowchart(flowchart)
        }
    }

    fun deleteFlowchart(id: String) {
        viewModelScope.launch {
            repository.deleteFlowchart(id)
        }
    }

    fun saveDiagnosticRun(session: DiagnosticSession) {
        viewModelScope.launch {
            repository.saveDiagnosticRun(session)
        }
    }

    fun resetTemplates() {
        viewModelScope.launch {
            repository.resetToDefaultTemplates()
        }
    }

    fun exportAndShareHtml(
        context: Context,
        flowchart: FlowchartData,
        session: DiagnosticSession? = null
    ) {
        try {
            val file = ExportHelper.exportHtml(context, flowchart, session)
            ExportHelper.shareFile(context, file, "text/html", flowchart.title)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportAndSharePdf(
        context: Context,
        flowchart: FlowchartData,
        session: DiagnosticSession? = null
    ) {
        try {
            val file = ExportHelper.exportPdf(context, flowchart, session)
            ExportHelper.shareFile(context, file, "application/pdf", flowchart.title)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class FlowchartViewModelFactory(
    private val repository: FlowchartRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlowchartViewModel::class.java)) {
            return FlowchartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
