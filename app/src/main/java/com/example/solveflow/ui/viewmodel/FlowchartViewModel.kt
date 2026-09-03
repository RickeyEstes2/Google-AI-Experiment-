package com.example.solveflow.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.solveflow.data.model.DiagnosticSession
import com.example.solveflow.data.model.FlowchartData
import com.example.solveflow.data.repository.FlowchartRepository
import com.example.solveflow.export.ExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlowchartViewModel(private val repository: FlowchartRepository) : ViewModel() {

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
    ) { flowcharts, query, category ->
        flowcharts.filter { fc ->
            val matchesQuery = query.isBlank() ||
                    fc.title.contains(query, ignoreCase = true) ||
                    fc.description.contains(query, ignoreCase = true) ||
                    fc.nodes.any { it.title.contains(query, ignoreCase = true) }

            val matchesCategory = category == "All" || fc.category.equals(category, ignoreCase = true)

            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.ensureDefaultTemplates()
        }
    }

    fun setCategory(category: String) {
        selectedCategory.value = category
    }

    fun setSearch(query: String) {
        searchQuery.value = query
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

    fun resetTemplates() {
        viewModelScope.launch {
            repository.resetToDefaultTemplates()
        }
    }

    fun saveDiagnosticRun(session: DiagnosticSession) {
        viewModelScope.launch {
            repository.saveDiagnosticRun(session)
        }
    }

    fun exportAndShareHtml(context: Context, flowchart: FlowchartData, session: DiagnosticSession? = null) {
        val file = ExportHelper.exportHtml(context, flowchart, session)
        ExportHelper.shareFile(context, file, "text/html", "${flowchart.title} (HTML Flowchart)")
    }

    fun exportAndSharePdf(context: Context, flowchart: FlowchartData, session: DiagnosticSession? = null) {
        val file = ExportHelper.exportPdf(context, flowchart, session)
        ExportHelper.shareFile(context, file, "application/pdf", "${flowchart.title} (PDF Specification)")
    }
}

class FlowchartViewModelFactory(private val repository: FlowchartRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlowchartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlowchartViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
