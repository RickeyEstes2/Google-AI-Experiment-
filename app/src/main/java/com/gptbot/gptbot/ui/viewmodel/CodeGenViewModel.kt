package com.gptbot.gptbot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gptbot.gptbot.data.model.*
import com.gptbot.gptbot.data.repository.CodeGenRepository
import com.gptbot.gptbot.engine.dbscan.DBSCANResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CodeGenTab(val title: String) {
    GENERATE("Generate & Explain"),
    SNIPPETS("Snippets"),
    KNOWLEDGE("Knowledge Base"),
    LANGUAGES("Languages"),
    DBSCAN_RL("DBSCAN & RL"),
    GITHUB_APK("GitHub APK")
}

class CodeGenViewModel(
    private val repository: CodeGenRepository
) : ViewModel() {

    val languages: StateFlow<List<ProgrammingLanguage>> = repository.languages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val snippets: StateFlow<List<CodeSnippet>> = repository.snippets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val knowledgeItems: StateFlow<List<KnowledgeItem>> = repository.knowledgeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyRecords: StateFlow<List<GenerationRecord>> = repository.records
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val policyEntries: StateFlow<List<RlPolicyEntry>> = repository.policyEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeTab = MutableStateFlow(CodeGenTab.GENERATE)
    val activeTab: StateFlow<CodeGenTab> = _activeTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<ProgrammingLanguage?>(null)
    val selectedLanguage: StateFlow<ProgrammingLanguage?> = _selectedLanguage.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _selectedStrategy = MutableStateFlow<RlStrategy?>(null)
    val selectedStrategy: StateFlow<RlStrategy?> = _selectedStrategy.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentRecord = MutableStateFlow<GenerationRecord?>(null)
    val currentRecord: StateFlow<GenerationRecord?> = _currentRecord.asStateFlow()

    private val _dbscanResult = MutableStateFlow<DBSCANResult?>(null)
    val dbscanResult: StateFlow<DBSCANResult?> = _dbscanResult.asStateFlow()

    private val _dbscanEps = MutableStateFlow(0.45)
    val dbscanEps: StateFlow<Double> = _dbscanEps.asStateFlow()

    private val _dbscanMinPts = MutableStateFlow(2)
    val dbscanMinPts: StateFlow<Int> = _dbscanMinPts.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _showFineTuningDialog = MutableStateFlow(false)
    val showFineTuningDialog: StateFlow<Boolean> = _showFineTuningDialog.asStateFlow()

    private val _showNewLanguageDialog = MutableStateFlow(false)
    val showNewLanguageDialog: StateFlow<Boolean> = _showNewLanguageDialog.asStateFlow()

    private val _showNewSnippetDialog = MutableStateFlow(false)
    val showNewSnippetDialog: StateFlow<Boolean> = _showNewSnippetDialog.asStateFlow()

    private val _showNewKnowledgeDialog = MutableStateFlow(false)
    val showNewKnowledgeDialog: StateFlow<Boolean> = _showNewKnowledgeDialog.asStateFlow()

    private val _showGitHubApkDialog = MutableStateFlow(false)
    val showGitHubApkDialog: StateFlow<Boolean> = _showGitHubApkDialog.asStateFlow()

    init {
        viewModelScope.launch {
            languages.collect { list ->
                if (_selectedLanguage.value == null && list.isNotEmpty()) {
                    _selectedLanguage.value = list.first()
                }
            }
        }
    }

    fun setActiveTab(tab: CodeGenTab) {
        _activeTab.value = tab
    }

    fun setSelectedLanguage(language: ProgrammingLanguage?) {
        _selectedLanguage.value = language
    }

    fun setPromptText(text: String) {
        _promptText.value = text
    }

    fun setSelectedStrategy(strategy: RlStrategy?) {
        _selectedStrategy.value = strategy
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun openFineTuningDialog() {
        _showFineTuningDialog.value = true
    }

    fun closeFineTuningDialog() {
        _showFineTuningDialog.value = false
    }

    fun openNewLanguageDialog() {
        _showNewLanguageDialog.value = true
    }

    fun closeNewLanguageDialog() {
        _showNewLanguageDialog.value = false
    }

    fun openNewSnippetDialog() {
        _showNewSnippetDialog.value = true
    }

    fun closeNewSnippetDialog() {
        _showNewSnippetDialog.value = false
    }

    fun openNewKnowledgeDialog() {
        _showNewKnowledgeDialog.value = true
    }

    fun closeNewKnowledgeDialog() {
        _showNewKnowledgeDialog.value = false
    }

    fun openGitHubApkDialog() {
        _showGitHubApkDialog.value = true
    }

    fun closeGitHubApkDialog() {
        _showGitHubApkDialog.value = false
    }

    fun selectHistoryRecord(record: GenerationRecord) {
        _currentRecord.value = record
    }

    fun setDBSCANParams(eps: Double, minPts: Int) {
        _dbscanEps.value = eps
        _dbscanMinPts.value = minPts
    }

    fun recalculateDBSCAN() {
        viewModelScope.launch {
            try {
                val result = repository.runDBSCAN(eps = _dbscanEps.value, minPts = _dbscanMinPts.value)
                _dbscanResult.value = result
            } catch (e: Exception) {
                _statusMessage.value = "Error computing DBSCAN: ${e.message}"
            }
        }
    }

    fun generateCode() {
        val prompt = _promptText.value.trim()
        val lang = _selectedLanguage.value ?: return

        if (prompt.isBlank()) {
            _statusMessage.value = "Please enter a code description or task prompt."
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val record = repository.generateCode(
                    prompt = prompt,
                    language = lang,
                    explicitStrategy = _selectedStrategy.value,
                    geminiApiKey = _geminiApiKey.value
                )
                _currentRecord.value = record
            } catch (e: Exception) {
                _statusMessage.value = "Error generating code: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun submitFeedback(score: Int) {
        val record = _currentRecord.value ?: return
        viewModelScope.launch {
            try {
                repository.submitFeedback(record, score)
                _currentRecord.value = record.copy(userFeedback = score)
                _statusMessage.value = if (score > 0) {
                    "Positive feedback recorded (+reward)"
                } else {
                    "Negative feedback recorded (-reward)"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error submitting feedback: ${e.message}"
            }
        }
    }

    fun applyErrorMitigationAndFineTune(
        editedCode: String,
        mitigationType: String,
        notes: String,
        saveAsGoldenExemplar: Boolean
    ) {
        val record = _currentRecord.value ?: return
        viewModelScope.launch {
            try {
                val updated = repository.applyErrorMitigationAndFineTune(
                    record = record,
                    editedCode = editedCode,
                    mitigationType = mitigationType,
                    mitigationNotes = notes,
                    saveAsGoldenExemplar = saveAsGoldenExemplar
                )
                _currentRecord.value = updated
                closeFineTuningDialog()
                _statusMessage.value = "Applied fine-tuning and updated RL policy"
            } catch (e: Exception) {
                _statusMessage.value = "Error during fine-tuning: ${e.message}"
            }
        }
    }

    fun addCustomLanguage(
        name: String,
        extension: String,
        paradigm: String,
        boilerplate: String,
        keywords: String
    ) {
        viewModelScope.launch {
            try {
                val lang = ProgrammingLanguage(
                    id = name.lowercase().replace(" ", "_"),
                    name = name,
                    extension = extension,
                    paradigm = paradigm,
                    sampleBoilerplate = boilerplate,
                    syntaxKeywords = keywords,
                    isCustom = true
                )
                repository.addLanguage(lang)
                _selectedLanguage.value = lang
                closeNewLanguageDialog()
                _statusMessage.value = "Added custom language: $name"
            } catch (e: Exception) {
                _statusMessage.value = "Error adding language: ${e.message}"
            }
        }
    }

    fun addSnippet(
        title: String,
        languageId: String,
        code: String,
        tags: String,
        category: String,
        explanation: String
    ) {
        viewModelScope.launch {
            try {
                val snippet = CodeSnippet(
                    title = title,
                    languageId = languageId,
                    code = code,
                    tags = tags,
                    category = category,
                    explanation = explanation
                )
                repository.addSnippet(snippet)
                closeNewSnippetDialog()
                _statusMessage.value = "Saved snippet: $title"
            } catch (e: Exception) {
                _statusMessage.value = "Error saving snippet: ${e.message}"
            }
        }
    }

    fun updateSnippet(snippet: CodeSnippet) {
        viewModelScope.launch {
            try {
                repository.updateSnippet(snippet)
                _statusMessage.value = "Updated snippet: ${snippet.title}"
            } catch (e: Exception) {
                _statusMessage.value = "Error updating snippet: ${e.message}"
            }
        }
    }

    fun deleteSnippet(snippet: CodeSnippet) {
        viewModelScope.launch {
            try {
                repository.deleteSnippet(snippet)
                _statusMessage.value = "Deleted snippet: ${snippet.title}"
            } catch (e: Exception) {
                _statusMessage.value = "Error deleting snippet: ${e.message}"
            }
        }
    }

    fun addKnowledge(
        title: String,
        topic: String,
        languageScope: String,
        content: String,
        tags: String
    ) {
        viewModelScope.launch {
            try {
                val item = KnowledgeItem(
                    title = title,
                    topic = topic,
                    languageScope = languageScope,
                    content = content,
                    tags = tags
                )
                repository.addKnowledge(item)
                closeNewKnowledgeDialog()
                _statusMessage.value = "Saved knowledge: $title"
            } catch (e: Exception) {
                _statusMessage.value = "Error saving knowledge: ${e.message}"
            }
        }
    }
}

class CodeGenViewModelFactory(
    private val repository: CodeGenRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodeGenViewModel::class.java)) {
            return CodeGenViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
