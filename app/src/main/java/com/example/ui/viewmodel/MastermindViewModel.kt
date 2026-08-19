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

    val displayedArticles: StateFlow<List<Article>> = combine(
        allArticles,
        _selectedFilter,
        _searchQuery
    ) { articles, filter, query ->
        articles.filter { article ->
            val matchesFilter = when (filter) {
                LinkFilter.ALL -> !article.isArchived
                LinkFilter.FAVORITES -> article.isFavorite && !article.isArchived
                LinkFilter.ARCHIVED -> article.isArchived
            }
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                article.title.contains(query, ignoreCase = true) ||
                article.url.contains(query, ignoreCase = true) ||
                article.sourceDomain.contains(query, ignoreCase = true) ||
                article.content.contains(query, ignoreCase = true)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: LinkFilter) {
        _selectedFilter.value = filter
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

    fun addNewLink(url: String, title: String, content: String = "") {
        viewModelScope.launch {
            repository.saveArticle(url = url, title = title, content = content)
            _isAddDialogOpen.value = false
            _snackbarMessage.value = "Link saved successfully!"
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            repository.toggleFavorite(article.id, article.isFavorite)
        }
    }

    fun toggleArchive(article: Article) {
        viewModelScope.launch {
            repository.toggleArchive(article.id, article.isArchived)
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
