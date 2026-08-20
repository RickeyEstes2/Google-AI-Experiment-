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
    FAVORITES,
    ARCHIVED
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
                LinkFilter.ALL -> !article.isArchived
                LinkFilter.FAVORITES -> article.isFavorite && !article.isArchived
                LinkFilter.ARCHIVED -> article.isArchived
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

    fun openArticle(article: Article) {
        _activeArticle.value = article
    }

    fun closeArticle() {
        _activeArticle.value = null
    }

    fun addNewLink(
        url: String,
        title: String,
        summary: String,
        notes: String,
        hashtags: List<String>
    ) {
        viewModelScope.launch {
            repository.saveArticle(
                url = url,
                title = title,
                summary = summary,
                notes = notes,
                hashtags = hashtags
            )
            _isAddDialogOpen.value = false
            _snackbarMessage.value = "Link saved with notes & summary!"
        }
    }

    fun updateLink(
        id: Long,
        title: String,
        summary: String,
        notes: String,
        hashtags: List<String>
    ) {
        viewModelScope.launch {
            repository.updateLinkDetails(
                id = id,
                title = title,
                summary = summary,
                notes = notes,
                hashtags = hashtags
            )
            // Refresh active article if currently open
            val updated = repository.getArticleById(id)
            if (_activeArticle.value?.id == id) {
                _activeArticle.value = updated
            }
            _snackbarMessage.value = "Changes saved!"
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

    fun toggleArchive(article: Article) {
        viewModelScope.launch {
            repository.toggleArchive(article.id, article.isArchived)
            val updated = article.copy(isArchived = !article.isArchived)
            if (_activeArticle.value?.id == article.id) {
                _activeArticle.value = updated
            }
            val msg = if (!article.isArchived) "Link archived" else "Link unarchived"
            _snackbarMessage.value = msg
        }
    }

    fun deleteLink(article: Article) {
        viewModelScope.launch {
            repository.deleteArticle(article.id)
            if (_activeArticle.value?.id == article.id) {
                _activeArticle.value = null
            }
            _snackbarMessage.value = "Link deleted"
        }
    }

    /**
     * Ingests incoming share intent from Google Chrome
     */
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
