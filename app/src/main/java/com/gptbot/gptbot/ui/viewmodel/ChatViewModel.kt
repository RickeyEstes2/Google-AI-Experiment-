package com.gptbot.gptbot.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gptbot.gptbot.data.db.ChatMessageEntity
import com.gptbot.gptbot.data.db.ChatSessionEntity
import com.gptbot.gptbot.data.model.ChainOfThought
import com.gptbot.gptbot.data.model.ChatMode
import com.gptbot.gptbot.data.model.SearchResultItem
import com.gptbot.gptbot.data.repository.ChatRepository
import com.gptbot.gptbot.engine.cot.SelfImprovingCoTEngine
import com.gptbot.gptbot.engine.search.SearchGroundingEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    val sessions: StateFlow<List<ChatSessionEntity>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow("")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _currentMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentMessages.asStateFlow()

    private val _activeMode = MutableStateFlow(ChatMode.GENERAL_COT)
    val activeMode: StateFlow<ChatMode> = _activeMode.asStateFlow()

    private val _isSearchEnabled = MutableStateFlow(false)
    val isSearchEnabled: StateFlow<Boolean> = _isSearchEnabled.asStateFlow()

    private val _attachedContext = MutableStateFlow<String?>(null)
    val attachedContext: StateFlow<String?> = _attachedContext.asStateFlow()

    private val _attachedTitle = MutableStateFlow<String?>(null)
    val attachedTitle: StateFlow<String?> = _attachedTitle.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStep = MutableStateFlow<String?>(null)
    val generationStep: StateFlow<String?> = _generationStep.asStateFlow()

    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            sessions.collect { list ->
                if (_currentSessionId.value.isBlank()) {
                    if (list.isNotEmpty()) {
                        selectSession(list.first().id)
                    } else {
                        createNewSession()
                    }
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
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessagesForSession(sessionId).collect { list ->
                _currentMessages.value = list
            }
        }
    }

    fun createNewSession(initialTitle: String = "Self-Improving Reasoning Session") {
        viewModelScope.launch {
            val sessionId = UUID.randomUUID().toString()
            val session = ChatSessionEntity(
                id = sessionId,
                title = initialTitle,
                initialMode = _activeMode.value.name
            )
            repository.saveSession(session)
            selectSession(sessionId)

            val welcomeMessage = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                role = "SYSTEM",
                content = " Welcome to **GPT-Bot: Self-Improving CoT Assistant**.\n\nI am equipped with a 5-stage self-improving Chain of Thought reasoning engine, LaTeX equation rendering, real-time Google Search grounding, log file root-cause analysis, and PDF knowledge extraction.\n\nChoose a mode above or tap a preset to begin!",
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
                _currentMessages.value = emptyList()
            }
        }
    }

    fun sendMessage(promptText: String) {
        val prompt = promptText.trim()
        if (prompt.isBlank() || _isGenerating.value) return

        var sessionId = _currentSessionId.value
        if (sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString()
            viewModelScope.launch {
                val newSession = ChatSessionEntity(
                    id = sessionId,
                    title = if (prompt.length > 35) prompt.take(35) + "..." else prompt,
                    initialMode = _activeMode.value.name
                )
                repository.saveSession(newSession)
                selectSession(sessionId)
            }
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val attachedCtx = _attachedContext.value
                val attachedTtl = _attachedTitle.value

                val userMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "USER",
                    content = prompt,
                    attachedSnippetTitle = attachedTtl,
                    mode = _activeMode.value.name
                )
                repository.saveMessage(userMsg)
                clearAttachedContext()

                var searchResults = emptyList<SearchResultItem>()
                if (_isSearchEnabled.value) {
                    _generationStep.value = "Searching Google for verified citations..."
                    try {
                        searchResults = SearchGroundingEngine.searchWeb(prompt)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _generationStep.value = "Running 5-stage self-improving CoT decomposition..."
                val (response, cot) = SelfImprovingCoTEngine.generateCoTResponse(
                    prompt = prompt,
                    mode = _activeMode.value,
                    contextAttachment = attachedCtx
                )

                val cotJson = json.encodeToString(ChainOfThought.serializer(), cot)
                val searchJson = if (searchResults.isNotEmpty()) {
                    json.encodeToString(ListSerializer(SearchResultItem.serializer()), searchResults)
                } else null

                val assistantMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "ASSISTANT",
                    content = response,
                    chainOfThoughtJson = cotJson,
                    searchResultsJson = searchJson,
                    mode = _activeMode.value.name
                )
                repository.saveMessage(assistantMsg)

                val currentSession = repository.getSessionById(sessionId)
                if (currentSession != null &&
                    (currentSession.title == "Self-Improving Reasoning Session" || currentSession.title.startsWith("Self-Improving"))
                ) {
                    val smartTitle = if (prompt.length > 35) prompt.take(35) + "..." else prompt
                    repository.saveSession(currentSession.copy(title = smartTitle))
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    role = "ASSISTANT",
                    content = "⚠️ Error processing request: ${e.message}",
                    mode = _activeMode.value.name
                )
                repository.saveMessage(errorMsg)
            } finally {
                _isGenerating.value = false
                _generationStep.value = null
            }
        }
    }

    fun refineAndImprove(message: ChatMessageEntity) {
        if (_isGenerating.value) return
        viewModelScope.launch {
            _isGenerating.value = true
            _generationStep.value = "Self-refining reasoning step with critique..."
            try {
                val (improvedText, newCot) = SelfImprovingCoTEngine.generateCoTResponse(
                    prompt = message.content,
                    mode = _activeMode.value,
                    currentIteration = message.iterationCount + 1,
                    previousCritique = "Refine logical consistency and verify LaTeX notation"
                )
                val cotJson = json.encodeToString(ChainOfThought.serializer(), newCot)
                val refinedMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = message.sessionId,
                    role = "ASSISTANT",
                    content = improvedText,
                    chainOfThoughtJson = cotJson,
                    iterationCount = message.iterationCount + 1,
                    mode = message.mode
                )
                repository.saveMessage(refinedMsg)
            } catch (e: Exception) {
                val errorMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = message.sessionId,
                    role = "ASSISTANT",
                    content = "⚠️ Error during refinement: ${e.message}",
                    mode = message.mode
                )
                repository.saveMessage(errorMsg)
            } finally {
                _isGenerating.value = false
                _generationStep.value = null
            }
        }
    }
}

class ChatViewModelFactory(
    private val repository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
