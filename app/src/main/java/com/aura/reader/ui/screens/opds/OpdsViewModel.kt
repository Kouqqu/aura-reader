package com.aura.reader.ui.screens.opds

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.OpdsBook
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CatalogSortOption {
    DEFAULT,
    POPULAR_DESC,
    POPULAR_ASC,
    TITLE_ASC,
    TITLE_DESC,
    AUTHOR_ASC,
    AUTHOR_DESC,
    YEAR_DESC,
    YEAR_ASC
}

sealed interface OpdsUiState {
    object Idle : OpdsUiState
    object Loading : OpdsUiState
    data class Success(val books: List<OpdsBook>, val currentTitle: String = "") : OpdsUiState
    data class Error(val message: String, val isConnectionError: Boolean = false) : OpdsUiState
}

class OpdsViewModel(
    private val bookRepository: BookRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _sortOption = MutableStateFlow(CatalogSortOption.DEFAULT)
    val sortOption: StateFlow<CatalogSortOption> = _sortOption.asStateFlow()

    fun setSortOption(option: CatalogSortOption) {
        _sortOption.value = option
    }

    fun getSortedBooks(
        books: List<OpdsBook>,
        option: CatalogSortOption
    ): List<OpdsBook> {
        val cats = books.filter { it.isCategory }
        val nonCats = books.filter { !it.isCategory }

        val sortedNonCats = when (option) {
            CatalogSortOption.DEFAULT -> nonCats
            CatalogSortOption.POPULAR_DESC -> nonCats.sortedWith(
                compareByDescending<OpdsBook> { it.downloadsCount }
                    .thenBy { it.title.lowercase() }
            )
            CatalogSortOption.POPULAR_ASC -> nonCats.sortedWith(
                compareBy<OpdsBook> { it.downloadsCount }
                    .thenBy { it.title.lowercase() }
            )
            CatalogSortOption.TITLE_ASC -> nonCats.sortedBy { it.title.lowercase() }
            CatalogSortOption.TITLE_DESC -> nonCats.sortedByDescending { it.title.lowercase() }
            CatalogSortOption.AUTHOR_ASC -> nonCats.sortedBy { it.author.lowercase() }
            CatalogSortOption.AUTHOR_DESC -> nonCats.sortedByDescending { it.author.lowercase() }
            CatalogSortOption.YEAR_DESC -> nonCats.sortedByDescending { it.year?.toIntOrNull() ?: 0 }
            CatalogSortOption.YEAR_ASC -> nonCats.sortedBy { it.year?.toIntOrNull() ?: 9999 }
        }
        return cats + sortedNonCats
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchHistory: StateFlow<List<String>> = preferencesManager.catalogSearchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customOpdsEnabled: StateFlow<Boolean> = preferencesManager.customOpdsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val baseUrl: StateFlow<String> = combine(
        preferencesManager.customOpdsEnabled,
        preferencesManager.catalogBaseUrl
    ) { customEnabled, url ->
        if (customEnabled && url.isNotBlank()) url.trim() else OpdsService.DEFAULT_BASE_URL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OpdsService.DEFAULT_BASE_URL)

    private val _uiState = MutableStateFlow<OpdsUiState>(OpdsUiState.Idle)
    val uiState: StateFlow<OpdsUiState> = _uiState.asStateFlow()

    private val _activeDownloads = MutableStateFlow<Map<String, Int>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Int>> = _activeDownloads.asStateFlow()

    private val _downloadedBooks = MutableStateFlow<Map<String, Book>>(emptyMap())
    val downloadedBooks: StateFlow<Map<String, Book>> = _downloadedBooks.asStateFlow()

    private val _selectedBookForDetails = MutableStateFlow<OpdsBook?>(null)
    val selectedBookForDetails: StateFlow<OpdsBook?> = _selectedBookForDetails.asStateFlow()

    data class CatalogHistoryEntry(
        val path: String? = null,
        val query: String? = null,
        val title: String,
        val isHome: Boolean = false
    )

    private val navStack = mutableListOf<CatalogHistoryEntry>()
    private var currentEntry = CatalogHistoryEntry(title = "Главная", isHome = true)

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        // Start in clean Home state without forcibly loading anything
        _uiState.value = OpdsUiState.Idle
    }

    fun selectBookForDetails(book: OpdsBook?) {
        _selectedBookForDetails.value = book
    }

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            preferencesManager.setCatalogBaseUrl(url)
            retry()
        }
    }

    fun search(query: String = _searchQuery.value) {
        val q = query.trim()
        if (q.isEmpty()) return
        _searchQuery.value = q
        viewModelScope.launch {
            preferencesManager.saveCatalogSearchQuery(q)
        }

        if (currentEntry.query != q) {
            navStack.add(currentEntry)
            _canGoBack.value = true
        }
        currentEntry = CatalogHistoryEntry(query = q, title = "Результаты поиска: $q")
        executeSearch(q)
    }

    fun loadCategory(path: String, categoryTitle: String) {
        if (currentEntry.path != path || currentEntry.query != null) {
            navStack.add(currentEntry)
            _canGoBack.value = true
        }
        currentEntry = CatalogHistoryEntry(path = path, title = categoryTitle)
        executeCategory(path, categoryTitle)
    }

    fun navigateBack(): Boolean {
        if (navStack.isEmpty()) return false
        val prev = navStack.removeAt(navStack.size - 1)
        currentEntry = prev
        _canGoBack.value = navStack.isNotEmpty()
        if (prev.isHome) {
            _searchQuery.value = ""
            _uiState.value = OpdsUiState.Idle
            lastAction = null
        } else if (prev.query != null) {
            _searchQuery.value = prev.query
            executeSearch(prev.query)
        } else {
            _searchQuery.value = ""
            executeCategory(prev.path ?: "/opds/new", prev.title)
        }
        return true
    }

    fun clearSearchAndReturnHome() {
        _searchQuery.value = ""
        navStack.clear()
        _canGoBack.value = false
        currentEntry = CatalogHistoryEntry(title = "Главная", isHome = true)
        _uiState.value = OpdsUiState.Idle
        lastAction = null
    }

    private fun executeCategory(path: String, categoryTitle: String) {
        lastAction = { executeCategory(path, categoryTitle) }
        viewModelScope.launch {
            _uiState.value = OpdsUiState.Loading
            val currentBase = baseUrl.value
            val result = OpdsService.getCategory(path, currentBase)
            result.onSuccess { list ->
                _uiState.value = OpdsUiState.Success(list, categoryTitle)
            }.onFailure { error ->
                val isConn = OpdsService.isConnectionError(error)
                _uiState.value = OpdsUiState.Error(
                    message = error.localizedMessage ?: "Ошибка связи с каталогом",
                    isConnectionError = isConn
                )
            }
        }
    }

    private fun executeSearch(q: String) {
        lastAction = { executeSearch(q) }
        viewModelScope.launch {
            _uiState.value = OpdsUiState.Loading
            val currentBase = baseUrl.value
            val result = OpdsService.searchBooks(q, currentBase)
            result.onSuccess { list ->
                _uiState.value = OpdsUiState.Success(list, "Результаты поиска: $q")
            }.onFailure { error ->
                val isConn = OpdsService.isConnectionError(error)
                _uiState.value = OpdsUiState.Error(
                    message = error.localizedMessage ?: "Ошибка связи с каталогом",
                    isConnectionError = isConn
                )
            }
        }
    }

    fun downloadBook(
        context: Context,
        book: OpdsBook,
        format: BookFormat,
        onComplete: ((Book) -> Unit)? = null
    ) {
        val downloadUrl = if (format == BookFormat.FB2) book.fb2Url else book.epubUrl
        if (downloadUrl.isNullOrBlank()) return

        val bookId = book.id
        viewModelScope.launch {
            _activeDownloads.value = _activeDownloads.value + (bookId to 0)
            val result = OpdsService.downloadAndExtractBook(
                context = context,
                book = book,
                format = format,
                downloadUrl = downloadUrl,
                onProgress = { progress ->
                    _activeDownloads.value = _activeDownloads.value + (bookId to progress)
                }
            )

            _activeDownloads.value = _activeDownloads.value - bookId

            result.onSuccess { file ->
                val openResult = bookRepository.openBookFromUri(Uri.fromFile(file))
                openResult.onSuccess { loadedBook ->
                    _downloadedBooks.value = _downloadedBooks.value + (bookId to loadedBook)
                    onComplete?.invoke(loadedBook)
                }
            }.onFailure { e ->
                _uiState.value = OpdsUiState.Error(
                    message = "Ошибка загрузки: ${e.localizedMessage}",
                    isConnectionError = OpdsService.isConnectionError(e)
                )
            }
        }
    }

    fun removeHistoryQuery(query: String) {
        viewModelScope.launch {
            preferencesManager.removeCatalogSearchQuery(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            preferencesManager.clearCatalogSearchHistory()
        }
    }

    fun retry() {
        lastAction?.invoke()
    }
}
