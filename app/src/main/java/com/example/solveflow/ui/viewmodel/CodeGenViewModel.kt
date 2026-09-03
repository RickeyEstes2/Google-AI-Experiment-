package com.example.solveflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.solveflow.data.model.*
import com.example.solveflow.data.repository.CodeGenRepository
import com.example.solveflow.engine.dbscan.DBSCANResult
import kotlinx.coroutines.flow.*
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

    val languages = repository.languages.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val snippets = repository.snippets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val knowledgeItems = repository.knowledgeItems.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val historyRecords = repository.records.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val policyEntries = repository.policyEntries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // UI Interactive States
    private val _activeTab = MutableStateFlow(CodeGenTab.GENERATE)
    val activeTab: StateFlow<CodeGenTab> = _activeTab.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<ProgrammingLanguage?>(null)
    val selectedLanguage: StateFlow<ProgrammingLanguage?> = _selectedLanguage.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _selectedStrategy = MutableStateFlow<RlStrategy?>(null) // null = Automatic RL
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

    // Dialog Visibility States
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
        // Auto-select Kotlin as default language when languages load
        viewModelScope.launch {
            languages.collect { list ->
                if (_selectedLanguage.value == null && list.isNotEmpty()) {
                    _selectedLanguage.value = list.find { it.id == "kotlin" } ?: list.first()
                }
            }
        }
        // Run initial DBSCAN clustering
        recalculateDBSCAN()
    }

    fun setActiveTab(tab: CodeGenTab) {
        _activeTab.value = tab
    }

    fun setSelectedLanguage(language: ProgrammingLanguage) {
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

    fun setDBSCANParams(eps: Double, minPts: Int) {
        _dbscanEps.value = eps
        _dbscanMinPts.value = minPts
        recalculateDBSCAN()
    }

    fun recalculateDBSCAN() {
        viewModelScope.launch {
            val result = repository.runDBSCAN(_dbscanEps.value, _dbscanMinPts.value)
            _dbscanResult.value = result
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
                _statusMessage.value = "Generated successfully with ${record.strategy.title}!"
                // Refresh DBSCAN clustering view
                recalculateDBSCAN()
            } catch (e: Exception) {
                _statusMessage.value = "Generation failed: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun submitFeedback(score: Int) {
        val record = _currentRecord.value ?: return
        viewModelScope.launch {
            repository.submitFeedback(record, score)
            _currentRecord.value = record.copy(userFeedback = score)
            _statusMessage.value = if (score > 0) "Reinforcement Learning: +1.0 Reward recorded!" else "Reinforcement Learning: Policy penalized for flaw."
        }
    }

    fun openFineTuningDialog() {
        _showFineTuningDialog.value = true
    }

    fun closeFineTuningDialog() {
        _showFineTuningDialog.value = false
    }

    fun applyErrorMitigationAndFineTune(
        editedCode: String,
        mitigationType: String,
        notes: String,
        saveAsGoldenExemplar: Boolean
    ) {
        val record = _currentRecord.value ?: return
        viewModelScope.launch {
            val updated = repository.applyErrorMitigationAndFineTune(
                record = record,
                editedCode = editedCode,
                mitigationType = mitigationType,
                mitigationNotes = notes,
                saveAsGoldenExemplar = saveAsGoldenExemplar
            )
            _currentRecord.value = updated
            _showFineTuningDialog.value = false
            _statusMessage.value = "Error mitigated & fine-tuned! RL policy updated."
            recalculateDBSCAN()
        }
    }

    // Modal dialog controls
    fun openNewLanguageDialog() { _showNewLanguageDialog.value = true }
    fun closeNewLanguageDialog() { _showNewLanguageDialog.value = false }

    fun addCustomLanguage(name: String, extension: String, paradigm: String, boilerplate: String, keywords: String) {
        viewModelScope.launch {
            val id = name.lowercase().replace(Regex("[^a-z0-9]"), "_")
            val newLang = ProgrammingLanguage(
                id = id,
                name = name,
                extension = if (extension.startsWith(".")) extension else ".$extension",
                paradigm = paradigm,
                sampleBoilerplate = boilerplate,
                syntaxKeywords = keywords,
                isCustom = true
            )
            repository.addLanguage(newLang)
            _selectedLanguage.value = newLang
            _showNewLanguageDialog.value = false
            _statusMessage.value = "Added new language: $name"
        }
    }

    fun openNewSnippetDialog() { _showNewSnippetDialog.value = true }
    fun closeNewSnippetDialog() { _showNewSnippetDialog.value = false }

    fun addSnippet(title: String, languageId: String, code: String, tags: String, category: String, explanation: String) {
        viewModelScope.launch {
            val snippet = CodeSnippet(
                title = title,
                languageId = languageId,
                code = code,
                tags = tags,
                category = category,
                explanation = explanation
            )
            repository.addSnippet(snippet)
            _showNewSnippetDialog.value = false
            _statusMessage.value = "Snippet saved to retrievable knowledge base!"
            recalculateDBSCAN()
        }
    }

    fun openNewKnowledgeDialog() { _showNewKnowledgeDialog.value = true }
    fun closeNewKnowledgeDialog() { _showNewKnowledgeDialog.value = false }

    fun addKnowledge(title: String, topic: String, languageScope: String, content: String, tags: String) {
        viewModelScope.launch {
            val item = KnowledgeItem(
                title = title,
                topic = topic,
                languageScope = languageScope,
                content = content,
                tags = tags
            )
            repository.addKnowledge(item)
            _showNewKnowledgeDialog.value = false
            _statusMessage.value = "Knowledge rule added for code generation RAG!"
            recalculateDBSCAN()
        }
    }

    fun openGitHubApkDialog() { _showGitHubApkDialog.value = true }
    fun closeGitHubApkDialog() { _showGitHubApkDialog.value = false }

    fun selectHistoryRecord(record: GenerationRecord) {
        _currentRecord.value = record
        _promptText.value = record.prompt
        _activeTab.value = CodeGenTab.GENERATE
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
