package com.example.solveflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solveflow.data.db.ChatMessageEntity
import com.example.solveflow.data.db.ChatSessionEntity
import com.example.solveflow.data.model.*
import com.example.solveflow.data.repository.ChatRepository
import com.example.solveflow.engine.cot.SelfImprovingCoTEngine
import com.example.solveflow.engine.search.SearchGroundingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String>("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentMessages.asStateFlow()

    private val _activeMode = MutableStateFlow(ChatMode.GENERAL_COT)
    val activeMode: StateFlow<ChatMode> = _activeMode.asStateFlow()

    private val _isSearchEnabled = MutableStateFlow(true)
    val isSearchEnabled: StateFlow<Boolean> = _isSearchEnabled.asStateFlow()

    private val _attachedContext = MutableStateFlow<String?>(null)
    val attachedContext: StateFlow<String?> = _attachedContext.asStateFlow()

    private val _attachedTitle = MutableStateFlow<String?>(null)
    val attachedTitle: StateFlow<String?> = _attachedTitle.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStep = MutableStateFlow("")
    val generationStep: StateFlow<String> = _generationStep.asStateFlow()

    init {
        viewModelScope.launch {
            sessions.collect { sessionList ->
                if (sessionList.isNotEmpty() && _currentSessionId.value.isBlank()) {
                    selectSession(sessionList.first().id)
                } else if (sessionList.isEmpty() && _currentSessionId.value.isBlank()) {
                    createNewSession()
                }
            }
        }
    }

    fun setMode(mode: ChatMode) {
        _activeMode.value = mode
    }

    fun toggleSearch() {
        _isSearchEnabled.value = !_isSearchEnabled.value
    }

    fun attachContext(title: String, content: String) {
        _attachedTitle.value = title
        _attachedContext.value = content
    }

    fun clearAttachedContext() {
        _attachedTitle.value = null
        _attachedContext.value = null
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { list ->
                _currentMessages.value = list
            }
        }
    }

    fun createNewSession(initialTitle: String = "Self-Improving Reasoning Session") {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val session = ChatSessionEntity(
                id = newId,
                title = initialTitle,
                initialMode = _activeMode.value.name
            )
            repository.saveSession(session)
            selectSession(newId)

            // Seed introductory system welcome message
            val welcomeMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = newId,
                role = "SYSTEM",
                content = "👋 Welcome to **GPT-Bot: Self-Improving CoT Assistant**.\n\n" +
                        "I am equipped with a 5-stage self-improving Chain of Thought reasoning engine, LaTeX equation rendering, real-time Google Search grounding, log file root-cause analysis, and PDF knowledge extraction.\n\n" +
                        "Choose a mode above or tap a preset to begin!",
                mode = _activeMode.value.name
            )
            repository.saveMessage(welcomeMessage)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = ""
            }
        }
    }

    fun sendMessage(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val sessionId = _currentSessionId.value.ifBlank {
            val newId = UUID.randomUUID().toString()
            viewModelScope.launch {
                repository.saveSession(ChatSessionEntity(id = newId, title = trimmed.take(30)))
            }
            _currentSessionId.value = newId
            newId
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true

            // 1. Save User Message
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "USER",
                content = trimmed,
                attachedSnippetTitle = _attachedTitle.value,
                mode = _activeMode.value.name
            )
            repository.saveMessage(userMsg)

            val contextSnippet = _attachedContext.value
            clearAttachedContext()

            // 2. Search Grounding (if enabled)
            _generationStep.value = "Searching Google & external sources..."
            val searchResults = if (_isSearchEnabled.value) {
                SearchGroundingEngine.searchWeb(trimmed)
            } else emptyList()

            // 3. Stage 1-5 Chain of Thought
            _generationStep.value = "Executing 5-Stage Self-Improving CoT..."
            val (synthesis, cot) = SelfImprovingCoTEngine.generateCoTResponse(
                prompt = trimmed,
                mode = _activeMode.value,
                contextAttachment = contextSnippet,
                currentIteration = 1
            )

            // 4. Save Assistant Response
            val assistantMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "ASSISTANT",
                content = synthesis,
                chainOfThoughtJson = json.encodeToString(cot),
                searchResultsJson = if (searchResults.isNotEmpty()) json.encodeToString(searchResults) else null,
                iterationCount = 1,
                mode = _activeMode.value.name
            )
            repository.saveMessage(assistantMsg)

            // Update session timestamp & title if first user prompt
            repository.getSessionById(sessionId)?.let { s ->
                val newTitle = if (s.title == "Self-Improving Reasoning Session" || s.title.startsWith("New")) {
                    trimmed.take(35)
                } else s.title
                repository.updateSession(s.copy(title = newTitle, lastUpdatedAt = System.currentTimeMillis()))
            }

            _isGenerating.value = false
            _generationStep.value = ""
        }
    }

    /**
     * Self-Improvement Loop: Refines an existing assistant response by incrementing iteration count,
     * re-critiquing the previous output, and updating the message.
     */
    fun refineAndImprove(message: ChatMessageEntity) {
        if (_isGenerating.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            _generationStep.value = "Running Self-Improvement Reflection (Iteration #${message.iterationCount + 1})..."

            val nextIteration = message.iterationCount + 1
            val mode = runCatching { ChatMode.valueOf(message.mode) }.getOrDefault(ChatMode.GENERAL_COT)

            val (synthesis, cot) = SelfImprovingCoTEngine.generateCoTResponse(
                prompt = message.content.take(150),
                mode = mode,
                contextAttachment = null,
                currentIteration = nextIteration,
                previousCritique = "Iteration #${message.iterationCount} established foundations. Iteration #$nextIteration improves analytical rigor, verifies edge-case robustness, and refines notation symmetry."
            )

            val updatedMessage = message.copy(
                content = synthesis,
                chainOfThoughtJson = json.encodeToString(cot),
                iterationCount = nextIteration,
                timestamp = System.currentTimeMillis()
            )

            repository.updateMessage(updatedMessage)

            _isGenerating.value = false
            _generationStep.value = ""
        }
    }
}
