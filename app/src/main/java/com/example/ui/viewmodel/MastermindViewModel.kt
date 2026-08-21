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
    RECENT,
    HAS_NOTES
}

class MastermindViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ArticleRepository

    val allArticles: StateFlow<List<Article>>

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(LinkFilter.ALL)
    val selectedFilter: StateFlow<LinkFilter> = _selectedFilter.asStateFlow()

    private val _selectedHashtag = MutableStateFlow<String?>(null)
    val selectedHashtag: StateFlow<String?> = _selectedHashtag.asStateFlow()

    private val _readingArticle = MutableStateFlow<Article?>(null)
    val readingArticle: StateFlow<Article?> = _readingArticle.asStateFlow()

    private val _editingArticle = MutableStateFlow<Article?>(null)
    val editingArticle: StateFlow<Article?> = _editingArticle.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _linkedArticles = MutableStateFlow<List<Article>>(emptyList())
    val linkedArticles: StateFlow<List<Article>> = _linkedArticles.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ArticleRepository(database.articleDao())

        allArticles = repository.allArticles.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed initial sample data if empty
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
        }
    }

    // Filtered articles list based on search, category filter, and hashtag filter
    val filteredArticles: StateFlow<List<Article>> = combine(
        allArticles,
        _searchQuery,
        _selectedFilter,
        _selectedHashtag
    ) { list, query, filter, tag ->
        var result = list

        // Tag filter
        if (!tag.isNullOrBlank()) {
            result = result.filter { article ->
                article.hashtags.any { it.equals(tag, ignoreCase = true) }
            }
        }

        // Category filter
        result = when (filter) {
            LinkFilter.ALL -> result
            LinkFilter.FAVORITES -> result.filter { it.isFavorite }
            LinkFilter.RECENT -> result.sortedByDescending { it.addedAt }
            LinkFilter.HAS_NOTES -> result.filter { it.notes.isNotBlank() }
        }

        // Search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            result = result.filter { article ->
                article.title.lowercase().contains(q) ||
                        article.url.lowercase().contains(q) ||
                        article.domain.lowercase().contains(q) ||
                        article.summary.lowercase().contains(q) ||
                        article.notes.lowercase().contains(q) ||
                        article.hashtags.any { it.lowercase().contains(q) }
            }
        }

        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All unique hashtags in the database
    val availableHashtags: StateFlow<List<String>> = allArticles.map { articles ->
        articles.flatMap { it.hashtags }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: LinkFilter) {
        _selectedFilter.value = filter
    }

    fun setHashtag(tag: String?) {
        _selectedHashtag.value = if (_selectedHashtag.value == tag) null else tag
    }

    fun openReader(article: Article) {
        _readingArticle.value = article
        loadLinkedArticles(article.linkedArticleIds)
    }

    fun closeReader() {
        _readingArticle.value = null
        _linkedArticles.value = emptyList()
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun openEditDialog(article: Article) {
        _editingArticle.value = article
    }

    fun closeEditDialog() {
        _editingArticle.value = null
    }

    fun addLink(
        url: String,
        title: String,
        thumbnailUrl: String,
        summary: String,
        notes: String,
        hashtags: List<String>,
        linkedIds: List<Long>
    ) {
        viewModelScope.launch {
            repository.insertArticle(url, title, thumbnailUrl, summary, notes, hashtags, linkedIds)
            _snackbarMessage.value = "Note added successfully"
        }
    }

    fun updateLink(article: Article) {
        viewModelScope.launch {
            repository.updateArticle(article)
            if (_readingArticle.value?.id == article.id) {
                _readingArticle.value = article
                loadLinkedArticles(article.linkedArticleIds)
            }
            _snackbarMessage.value = "Note updated"
        }
    }

    fun toggleFavorite(article: Article) {
        viewModelScope.launch {
            repository.toggleFavorite(article)
            if (_readingArticle.value?.id == article.id) {
                _readingArticle.value = _readingArticle.value?.copy(isFavorite = !article.isFavorite)
            }
        }
    }

    fun updateHashtags(articleId: Long, hashtags: List<String>) {
        viewModelScope.launch {
            repository.updateHashtags(articleId, hashtags)
            if (_readingArticle.value?.id == articleId) {
                _readingArticle.value = _readingArticle.value?.copy(hashtags = hashtags)
            }
            _snackbarMessage.value = "Tags updated"
        }
    }

    fun deleteLink(article: Article) {
        viewModelScope.launch {
            repository.deleteArticle(article)
            if (_readingArticle.value?.id == article.id) {
                closeReader()
            }
            _snackbarMessage.value = "Note deleted"
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun loadLinkedArticles(ids: List<Long>) {
        if (ids.isEmpty()) {
            _linkedArticles.value = emptyList()
            return
        }
        viewModelScope.launch {
            _linkedArticles.value = repository.getArticlesByIds(ids)
        }
    }
}
