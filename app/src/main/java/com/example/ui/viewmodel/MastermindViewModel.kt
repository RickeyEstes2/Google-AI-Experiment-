package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.Article
import com.example.data.repository.ArticleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LinkFilter {
    ALL,
    FAVORITES
}

class MastermindViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArticleRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = ArticleRepository(db.articleDao(), application)
        viewModelScope.launch {
            repository.seedInitialArticlesIfEmpty()
        }
    }

    val cloudSyncManager = repository.cloudSyncManager

    // Cloud Sync Dialog State
    private val _isCloudSyncDialogOpen = MutableStateFlow(false)
    val isCloudSyncDialogOpen: StateFlow<Boolean> = _isCloudSyncDialogOpen.asStateFlow()

    // Google Drive Specific Folder Settings Dialog State
    private val _isGoogleDriveFolderSettingsOpen = MutableStateFlow(false)
    val isGoogleDriveFolderSettingsOpen: StateFlow<Boolean> = _isGoogleDriveFolderSettingsOpen.asStateFlow()

    fun openCloudSyncDialog() {
        _isCloudSyncDialogOpen.value = true
    }

    fun closeCloudSyncDialog() {
        _isCloudSyncDialogOpen.value = false
    }

    fun openGoogleDriveFolderSettings() {
        _isGoogleDriveFolderSettingsOpen.value = true
    }

    fun closeGoogleDriveFolderSettings() {
        _isGoogleDriveFolderSettingsOpen.value = false
    }

    fun triggerCloudSyncNow() {
        cloudSyncManager.triggerSyncNow()
    }

    fun createCloudSnapshot(note: String) {
        viewModelScope.launch {
            val entities = repository.getAllEntitiesDirect()
            cloudSyncManager.createSnapshot(entities, note)
        }
    }

    fun restoreCloudSnapshot(snapshot: com.example.data.sync.CloudSnapshot) {
        viewModelScope.launch {
            cloudSyncManager.restoreSnapshot(snapshot)
            _snackbarMessage.value = "Cloud snapshot restored!"
        }
    }

    fun exportCloudBackup(): String {
        var result = ""
        // Blocking run for clipboard copy or async export
        kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            val entities = repository.getAllEntitiesDirect()
            result = cloudSyncManager.exportBackupJson(entities)
        }
        return result
    }

    fun importCloudBackup(json: String) {
        viewModelScope.launch {
            val success = cloudSyncManager.importBackupJson(json)
            _snackbarMessage.value = if (success) "Backup restored successfully!" else "Failed to parse backup JSON"
        }
    }

    // Filter & Search state
    private val _selectedFilter = MutableStateFlow(LinkFilter.ALL)
    val selectedFilter: StateFlow<LinkFilter> = _selectedFilter.asStateFlow()

    private val _selectedHashtag = MutableStateFlow<String?>(null)
    val selectedHashtag: StateFlow<String?> = _selectedHashtag.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen: StateFlow<Boolean> = _isAddDialogOpen.asStateFlow()

    private val _activeArticle = MutableStateFlow<Article?>(null)
    val activeArticle: StateFlow<Article?> = _activeArticle.asStateFlow()

    // Editing Dialog State
    private val _editingArticle = MutableStateFlow<Article?>(null)
    val editingArticle: StateFlow<Article?> = _editingArticle.asStateFlow()

    // Linking Picker Dialog State
    private val _isLinkPickerOpen = MutableStateFlow(false)
    val isLinkPickerOpen: StateFlow<Boolean> = _isLinkPickerOpen.asStateFlow()

    private val _targetArticleForLinking = MutableStateFlow<Article?>(null)
    val targetArticleForLinking: StateFlow<Article?> = _targetArticleForLinking.asStateFlow()

    // Navigation history stack for linked posts browsing
    private val _articleBackStack = MutableStateFlow<List<Article>>(emptyList())
    val articleBackStack: StateFlow<List<Article>> = _articleBackStack.asStateFlow()

    // Linked posts for active article
    private val _activeLinkedArticles = MutableStateFlow<List<Article>>(emptyList())
    val activeLinkedArticles: StateFlow<List<Article>> = _activeLinkedArticles.asStateFlow()
    val activeArticleLinkedPosts: StateFlow<List<Article>> = _activeLinkedArticles.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Data streams
    val allArticles: Flow<List<Article>> = repository.allArticles

    // Collect all distinct hashtags across all articles
    val allAvailableHashtags: StateFlow<List<String>> = allArticles.map { list ->
        list.flatMap { it.hashtags }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedArticles: StateFlow<List<Article>> = combine(
        allArticles,
        _selectedFilter,
        _selectedHashtag,
        _searchQuery
    ) { articles, filter, hashtag, query ->
        articles.filter { article ->
            val matchesFilter = when (filter) {
                LinkFilter.ALL -> true
                LinkFilter.FAVORITES -> article.isFavorite
            }
            val matchesHashtag = hashtag == null || article.hashtags.any { it.equals(hashtag, ignoreCase = true) }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                article.title.contains(query, ignoreCase = true) ||
                article.url.contains(query, ignoreCase = true) ||
                article.sourceDomain.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true) ||
                article.notes.contains(query, ignoreCase = true) ||
                article.hashtags.any { it.contains(query, ignoreCase = true) } ||
                article.comments.any { it.text.contains(query, ignoreCase = true) }
            }
            matchesFilter && matchesHashtag && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: LinkFilter) {
        _selectedFilter.value = filter
    }

    fun setHashtag(hashtag: String?) {
        _selectedHashtag.value = hashtag
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAddDialog() {
        _isAddDialogOpen.value = true
    }

    fun closeAddDialog() {
        _isAddDialogOpen.value = false
    }

    fun openEditDialog(article: Article) {
        _editingArticle.value = article
    }

    fun closeEditDialog() {
        _editingArticle.value = null
    }

    fun openLinkPickerForArticle(article: Article) {
        _targetArticleForLinking.value = article
        _isLinkPickerOpen.value = true
    }

    fun closeLinkPicker() {
        _isLinkPickerOpen.value = false
        _targetArticleForLinking.value = null
    }

    fun openArticle(article: Article) {
        _articleBackStack.value = emptyList()
        _activeArticle.value = article
        loadLinkedArticles(article.linkedPostIds)
    }

    fun navigateToLinkedArticle(linkedArticle: Article) {
        val current = _activeArticle.value
        if (current != null) {
            _articleBackStack.value = _articleBackStack.value + current
        }
        _activeArticle.value = linkedArticle
        loadLinkedArticles(linkedArticle.linkedPostIds)
    }

    fun navigateBackInStack(): Boolean {
        val stack = _articleBackStack.value
        if (stack.isNotEmpty()) {
            val previous = stack.last()
            _articleBackStack.value = stack.dropLast(1)
            _activeArticle.value = previous
            loadLinkedArticles(previous.linkedPostIds)
            return true
        } else {
            closeArticle()
            return false
        }
    }

    fun closeArticle() {
        _activeArticle.value = null
        _articleBackStack.value = emptyList()
        _activeLinkedArticles.value = emptyList()
    }

    private fun loadLinkedArticles(ids: List<Long>) {
        if (ids.isEmpty()) {
            _activeLinkedArticles.value = emptyList()
            return
        }
        viewModelScope.launch {
            _activeLinkedArticles.value = repository.getArticlesByIds(ids)
        }
    }

    fun addNewLink(
        url: String,
        title: String,
        thumbnailUrl: String = "",
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedPostIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            repository.saveArticle(
                url = url,
                title = title,
                thumbnailUrl = thumbnailUrl,
                summary = summary,
                notes = notes,
                hashtags = hashtags,
                linkedPostIds = linkedPostIds
            )
            _isAddDialogOpen.value = false
            _snackbarMessage.value = "Link saved!"
        }
    }

    fun saveEditedArticle(
        id: Long,
        title: String,
        url: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedPostIds: List<Long>
    ) {
        viewModelScope.launch {
            repository.updateLinkDetails(
                id = id,
                title = title,
                url = url,
                thumbnailUrl = thumbnailUrl,
                summary = summary,
                notes = notes,
                hashtags = hashtags,
                linkedPostIds = linkedPostIds
            )
            val updated = repository.getArticleById(id)
            if (_activeArticle.value?.id == id && updated != null) {
                _activeArticle.value = updated
                loadLinkedArticles(updated.linkedPostIds)
            }
            _editingArticle.value = null
            _snackbarMessage.value = "Post updated!"
        }
    }

    fun updateLinkedPosts(articleId: Long, linkedPostIds: List<Long>) {
        viewModelScope.launch {
            val article = repository.getArticleById(articleId) ?: return@launch
            repository.updateLinkDetails(
                id = article.id,
                title = article.title,
                url = article.url,
                thumbnailUrl = article.thumbnailUrl,
                summary = article.summary,
                notes = article.notes,
                hashtags = article.hashtags,
                linkedPostIds = linkedPostIds
            )
            val updated = repository.getArticleById(articleId)
            if (_activeArticle.value?.id == articleId && updated != null) {
                _activeArticle.value = updated
                loadLinkedArticles(updated.linkedPostIds)
            }
            if (_editingArticle.value?.id == articleId && updated != null) {
                _editingArticle.value = updated
            }
            closeLinkPicker()
            _snackbarMessage.value = "Linked posts updated"
        }
    }

    fun updateNotes(articleId: Long, newNotes: String) {
        viewModelScope.launch {
            val article = repository.getArticleById(articleId) ?: return@launch
            repository.updateLinkDetails(
                id = article.id,
                title = article.title,
                url = article.url,
                thumbnailUrl = article.thumbnailUrl,
                summary = article.summary,
                notes = newNotes,
                hashtags = article.hashtags,
                linkedPostIds = article.linkedPostIds
            )
            val updated = repository.getArticleById(articleId)
            if (_activeArticle.value?.id == articleId && updated != null) {
                _activeArticle.value = updated
            }
            _snackbarMessage.value = "Notes updated"
        }
    }

    fun updateHashtags(articleId: Long, newHashtags: List<String>) {
        viewModelScope.launch {
            val article = repository.getArticleById(articleId) ?: return@launch
            repository.updateLinkDetails(
                id = article.id,
                title = article.title,
                url = article.url,
                thumbnailUrl = article.thumbnailUrl,
                summary = article.summary,
                notes = article.notes,
                hashtags = newHashtags,
                linkedPostIds = article.linkedPostIds
            )
            val updated = repository.getArticleById(articleId)
            if (_activeArticle.value?.id == articleId && updated != null) {
                _activeArticle.value = updated
            }
            _snackbarMessage.value = "Hashtags updated"
        }
    }

    fun updateCommentText(articleId: Long, commentId: String, newText: String) {
        viewModelScope.launch {
            val updated = repository.updateCommentText(articleId, commentId, newText)
            if (_activeArticle.value?.id == articleId && updated != null) {
                _activeArticle.value = updated
            }
            _snackbarMessage.value = "Comment updated"
        }
    }

    fun addCommentToActiveLink(commentText: String) {
        val current = _activeArticle.value ?: return
        if (commentText.isBlank()) return
        viewModelScope.launch {
            val updated = repository.addComment(current.id, commentText)
            if (updated != null) {
                _activeArticle.value = updated
                _snackbarMessage.value = "Comment added"
            }
        }
    }

    fun deleteCommentFromActiveLink(commentId: String) {
        val current = _activeArticle.value ?: return
        viewModelScope.launch {
            val updated = repository.deleteComment(current.id, commentId)
            if (updated != null) {
                _activeArticle.value = updated
                _snackbarMessage.value = "Comment removed"
            }
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            repository.toggleFavorite(article.id, article.isFavorite)
            val updated = article.copy(isFavorite = !article.isFavorite)
            if (_activeArticle.value?.id == article.id) {
                _activeArticle.value = updated
            }
        }
    }

    fun deleteLink(article: Article) {
        viewModelScope.launch {
            repository.deleteArticle(article.id)
            if (_activeArticle.value?.id == article.id) {
                navigateBackInStack()
            }
            _snackbarMessage.value = "Link deleted"
        }
    }

    fun handleChromeShare(sharedText: String) {
        viewModelScope.launch {
            repository.ingestSharedContent(sharedText)
            _snackbarMessage.value = "Saved link from Chrome!"
        }
    }

    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }
}
