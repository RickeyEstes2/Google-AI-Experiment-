package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MemoryRepository
import com.example.service.MemoryAccessibilityService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

enum class NavigationTab(val label: String, val title: String) {
    TIMELINE("Memories", "Chronological Memories"),
    ANSWERS("Answers", "Ask Your Memories"),
    SUMMARIES("Summaries", "Smart Summaries"),
    INSIGHTS("Insights & Stats", "Personal Insights & Stats"),
    EXPORT("CSV & Settings", "CSV Export & System Integration")
}

class MemoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MemoryRepository(application)

    // Navigation & Tabs
    private val _selectedTab = MutableStateFlow(NavigationTab.TIMELINE)
    val selectedTab: StateFlow<NavigationTab> = _selectedTab.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedAppFilter = MutableStateFlow<String?>(null)
    val selectedAppFilter: StateFlow<String?> = _selectedAppFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _starredOnly = MutableStateFlow(false)
    val starredOnly: StateFlow<Boolean> = _starredOnly.asStateFlow()

    // Distinct App names
    val distinctApps: StateFlow<List<String>> = repository.distinctApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All memories flow with filter logic
    val memories: StateFlow<List<MemoryEntity>> = combine(
        repository.allMemories,
        _searchQuery,
        _selectedAppFilter,
        _selectedCategoryFilter,
        _starredOnly
    ) { all, query, appFilter, catFilter, starredOnly ->
        var list = all

        if (starredOnly) {
            list = list.filter { it.isStarred }
        }

        if (!appFilter.isNullOrBlank()) {
            list = list.filter { it.appName.equals(appFilter, ignoreCase = true) }
        }

        if (!catFilter.isNullOrBlank()) {
            list = list.filter { it.appCategory.equals(catFilter, ignoreCase = true) }
        }

        if (query.isNotBlank()) {
            val q = query.lowercase().trim()
            list = list.filter { mem ->
                mem.text.lowercase().contains(q) ||
                mem.title.lowercase().contains(q) ||
                mem.appName.lowercase().contains(q) ||
                mem.tags.any { it.lowercase().contains(q) } ||
                mem.addendums.any { it.content.lowercase().contains(q) }
            }
        }

        list.sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active detail & dialog states
    private val _selectedMemoryDetail = MutableStateFlow<MemoryEntity?>(null)
    val selectedMemoryDetail: StateFlow<MemoryEntity?> = _selectedMemoryDetail.asStateFlow()

    private val _showAddEditDialog = MutableStateFlow(false)
    val showAddEditDialog: StateFlow<Boolean> = _showAddEditDialog.asStateFlow()

    private val _memoryToEdit = MutableStateFlow<MemoryEntity?>(null)
    val memoryToEdit: StateFlow<MemoryEntity?> = _memoryToEdit.asStateFlow()

    // Q&A / Answers Engine state
    private val _qnaQuery = MutableStateFlow("")
    val qnaQuery: StateFlow<String> = _qnaQuery.asStateFlow()

    private val _currentAnswer = MutableStateFlow<AnswerResult?>(null)
    val currentAnswer: StateFlow<AnswerResult?> = _currentAnswer.asStateFlow()

    private val _isAnswering = MutableStateFlow(false)
    val isAnswering: StateFlow<Boolean> = _isAnswering.asStateFlow()

    private val _answerHistory = MutableStateFlow<List<AnswerResult>>(emptyList())
    val answerHistory: StateFlow<List<AnswerResult>> = _answerHistory.asStateFlow()

    // Summaries Engine state
    private val _summaryResult = MutableStateFlow<SummaryResult?>(null)
    val summaryResult: StateFlow<SummaryResult?> = _summaryResult.asStateFlow()

    private val _isSummarizing = MutableStateFlow(false)
    val isSummarizing: StateFlow<Boolean> = _isSummarizing.asStateFlow()

    private val _summaryTimeframe = MutableStateFlow("Today")
    val summaryTimeframe: StateFlow<String> = _summaryTimeframe.asStateFlow()

    // Personal Stats & Insights
    private val _personalStats = MutableStateFlow(PersonalStats())
    val personalStats: StateFlow<PersonalStats> = _personalStats.asStateFlow()

    // CSV Export state
    private val _exportedCsv = MutableStateFlow<Pair<File, Uri>?>(null)
    val exportedCsv: StateFlow<Pair<File, Uri>?> = _exportedCsv.asStateFlow()

    private val _exportStatusMessage = MutableStateFlow<String?>(null)
    val exportStatusMessage: StateFlow<String?> = _exportStatusMessage.asStateFlow()

    val isAccessibilityActive = MemoryAccessibilityService.isRunning

    init {
        // Refresh stats on load
        refreshStats()
        // If empty, preload sample data for instant exploration
        viewModelScope.launch {
            repository.allMemories.firstOrNull()?.let {
                if (it.isEmpty()) {
                    repository.populateSampleMemories()
                    refreshStats()
                    generateDefaultSummary()
                }
            }
        }
    }

    fun setTab(tab: NavigationTab) {
        _selectedTab.value = tab
        if (tab == NavigationTab.INSIGHTS) {
            refreshStats()
        } else if (tab == NavigationTab.SUMMARIES && _summaryResult.value == null) {
            generateSummary(_summaryTimeframe.value)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setAppFilter(appName: String?) {
        _selectedAppFilter.value = if (_selectedAppFilter.value == appName) null else appName
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun toggleStarredOnly() {
        _starredOnly.value = !_starredOnly.value
    }

    fun openMemoryDetail(memory: MemoryEntity) {
        _selectedMemoryDetail.value = memory
    }

    fun closeMemoryDetail() {
        _selectedMemoryDetail.value = null
    }

    fun openAddDialog() {
        _memoryToEdit.value = null
        _showAddEditDialog.value = true
    }

    fun openEditDialog(memory: MemoryEntity) {
        _memoryToEdit.value = memory
        _showAddEditDialog.value = true
    }

    fun closeAddEditDialog() {
        _showAddEditDialog.value = false
        _memoryToEdit.value = null
    }

    fun saveMemory(
        id: Long = 0,
        text: String,
        appName: String,
        packageName: String,
        category: String,
        title: String,
        tags: List<String>,
        sourceType: String = "MANUAL",
        sentiment: String = "NEUTRAL"
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                repository.insertMemory(
                    text = text,
                    appName = appName,
                    packageName = packageName,
                    appCategory = category,
                    title = title,
                    tags = tags,
                    sourceType = sourceType,
                    sentiment = sentiment
                )
            } else {
                val existing = repository.getMemoryById(id)
                if (existing != null) {
                    repository.updateMemory(
                        existing.copy(
                            text = text,
                            appName = appName,
                            packageName = packageName,
                            appCategory = category,
                            title = title,
                            tags = tags,
                            sentiment = sentiment
                        )
                    )
                }
            }
            closeAddEditDialog()
            refreshStats()
        }
    }

    fun toggleStar(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.toggleStar(memory)
            if (_selectedMemoryDetail.value?.id == memory.id) {
                _selectedMemoryDetail.value = _selectedMemoryDetail.value?.copy(isStarred = !memory.isStarred)
            }
            refreshStats()
        }
    }

    fun addAddendum(memoryId: Long, content: String) {
        viewModelScope.launch {
            repository.addAddendum(memoryId, content)
            val updated = repository.getMemoryById(memoryId)
            if (_selectedMemoryDetail.value?.id == memoryId) {
                _selectedMemoryDetail.value = updated
            }
        }
    }

    fun removeAddendum(memoryId: Long, addendumId: String) {
        viewModelScope.launch {
            repository.removeAddendum(memoryId, addendumId)
            val updated = repository.getMemoryById(memoryId)
            if (_selectedMemoryDetail.value?.id == memoryId) {
                _selectedMemoryDetail.value = updated
            }
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.deleteMemory(memory)
            if (_selectedMemoryDetail.value?.id == memory.id) {
                _selectedMemoryDetail.value = null
            }
            refreshStats()
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedMemoryDetail.value = null
            _answerHistory.value = emptyList()
            _currentAnswer.value = null
            _summaryResult.value = null
            refreshStats()
        }
    }

    fun populateSampleData() {
        viewModelScope.launch {
            repository.populateSampleMemories()
            refreshStats()
            generateSummary(_summaryTimeframe.value)
        }
    }

    // -------------------------------------------------------------
    // Q&A / ANSWERS
    // -------------------------------------------------------------
    fun setQnaQuery(query: String) {
        _qnaQuery.value = query
    }

    fun askQuestion(query: String = _qnaQuery.value) {
        val q = query.trim()
        if (q.isBlank()) return

        viewModelScope.launch {
            _isAnswering.value = true
            val result = repository.answerQuestion(q)
            _currentAnswer.value = result
            _answerHistory.value = listOf(result) + _answerHistory.value.filterNot { it.query.equals(q, true) }
            _isAnswering.value = false
        }
    }

    // -------------------------------------------------------------
    // SUMMARIES
    // -------------------------------------------------------------
    fun setSummaryTimeframe(timeframe: String) {
        _summaryTimeframe.value = timeframe
        generateSummary(timeframe)
    }

    fun generateSummary(timeframe: String = _summaryTimeframe.value) {
        viewModelScope.launch {
            _isSummarizing.value = true
            val result = repository.generateSummary(timeframe, _selectedAppFilter.value, _selectedCategoryFilter.value)
            _summaryResult.value = result
            _isSummarizing.value = false
        }
    }

    private fun generateDefaultSummary() {
        viewModelScope.launch {
            _summaryResult.value = repository.generateSummary("Today")
        }
    }

    // -------------------------------------------------------------
    // INSIGHTS & STATS
    // -------------------------------------------------------------
    fun refreshStats() {
        viewModelScope.launch {
            _personalStats.value = repository.getPersonalStats()
        }
    }

    // -------------------------------------------------------------
    // CSV EXPORT
    // -------------------------------------------------------------
    fun exportToCsv(onSuccess: (Uri) -> Unit) {
        viewModelScope.launch {
            val result = repository.exportMemoriesToCsv()
            if (result != null) {
                _exportedCsv.value = result
                _exportStatusMessage.value = "CSV exported successfully (${result.first.name})"
                onSuccess(result.second)
            } else {
                _exportStatusMessage.value = "Failed to export CSV"
            }
        }
    }

    fun dismissExportMessage() {
        _exportStatusMessage.value = null
    }

    fun handleIncomingShare(url: String, title: String, text: String) {
        viewModelScope.launch {
            val content = buildString {
                if (title.isNotBlank()) append(title).append("\n\n")
                if (text.isNotBlank()) append(text).append("\n")
                if (url.isNotBlank()) append(url)
            }.trim()

            if (content.isNotBlank()) {
                repository.insertMemory(
                    text = content,
                    appName = "Shared Content",
                    appCategory = "Reading & Articles",
                    title = title.ifBlank { "Shared Link / Note" },
                    sourceType = "SHARE_INTENT",
                    sentiment = "IMPORTANT"
                )
                refreshStats()
            }
        }
    }
}
