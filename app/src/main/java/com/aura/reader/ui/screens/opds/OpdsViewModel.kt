package com.aura.reader.ui.screens.opds

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.OpdsBook
import com.aura.reader.data.model.OpdsSearchResult
import com.aura.reader.data.model.OpdsSearchType
import com.aura.reader.data.opds.OpdsService
import com.aura.reader.data.opds.OpdsUrl
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    data class Success(
        val books: List<OpdsBook>,
        val currentTitle: String = "",
        val searchResult: OpdsSearchResult? = null
    ) : OpdsUiState
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

    private val _searchScope = MutableStateFlow(OpdsSearchType.ALL)
    val searchScope: StateFlow<OpdsSearchType> = _searchScope.asStateFlow()

    fun setSearchScope(scope: OpdsSearchType) {
        if (_searchScope.value == scope) return
        _searchScope.value = scope
        if (_searchQuery.value.isNotBlank() && currentEntry.query != null) {
            executeSearch(_searchQuery.value, scope)
        }
    }

    val searchHistory: StateFlow<List<String>> = preferencesManager.catalogSearchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customOpdsEnabled: StateFlow<Boolean> = preferencesManager.customOpdsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val baseUrl: StateFlow<String> = combine(
        preferencesManager.customOpdsEnabled,
        preferencesManager.catalogBaseUrl
    ) { customEnabled, url ->
        // Ignore values that the user cannot have entered on purpose (old builds
        // stored raw text), otherwise the catalog stays broken forever.
        val storedUrl = OpdsUrl.normalize(url)
        if (customEnabled && storedUrl != null) storedUrl else OpdsService.DEFAULT_BASE_URL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OpdsService.DEFAULT_BASE_URL)

    val vpnNoticeDismissed: StateFlow<Boolean> = preferencesManager.vpnNoticeDismissed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun dismissVpnNotice() {
        viewModelScope.launch { preferencesManager.setVpnNoticeDismissed(true) }
    }

    private val _uiState = MutableStateFlow<OpdsUiState>(OpdsUiState.Idle)
    val uiState: StateFlow<OpdsUiState> = _uiState.asStateFlow()

    private val _activeDownloads = MutableStateFlow<Map<String, Int>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Int>> = _activeDownloads.asStateFlow()
    private val downloadJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    private val _downloadedBooks = MutableStateFlow<Map<String, Book>>(emptyMap())
    val downloadedBooks: StateFlow<Map<String, Book>> = _downloadedBooks.asStateFlow()

    private val _selectedBookForDetails = MutableStateFlow<OpdsBook?>(null)
    val selectedBookForDetails: StateFlow<OpdsBook?> = _selectedBookForDetails.asStateFlow()

    data class CatalogHistoryEntry(
        val path: String? = null,
        val query: String? = null,
        val searchScope: OpdsSearchType = OpdsSearchType.ALL,
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
        viewModelScope.launch {
            bookRepository.recentBooks.collect { libraryBooks ->
                syncDownloadedBooks(libraryBooks)
            }
        }
    }

    private fun syncDownloadedBooks(libraryBooks: List<Book>) {
        _downloadedBooks.update { currentMap ->
            currentMap.filter { (_, book) ->
                libraryBooks.any { it.id == book.id } &&
                (book.uriString.isBlank() || !book.uriString.startsWith("file://") || File(Uri.parse(book.uriString).path ?: "").exists())
            }
        }
    }

    private fun matchCatalogWithLibrary(books: List<OpdsBook>) {
        val library = bookRepository.recentBooks.value
        if (library.isEmpty()) return
        val newMatches = mutableMapOf<String, Book>()
        for (opdsBook in books) {
            val safeBookId = opdsBook.id.replace(Regex("[^a-zA-Z0-9_-]"), "_").takeLast(20)
            val match = library.firstOrNull { lib ->
                (lib.uriString.contains(safeBookId) ||
                 (lib.title.isNotBlank() && lib.title.trim().equals(opdsBook.title.trim(), ignoreCase = true) &&
                  (lib.author.isBlank() || opdsBook.author.isBlank() || lib.author.trim().equals(opdsBook.author.trim(), ignoreCase = true)))) &&
                (!lib.uriString.startsWith("file://") || File(Uri.parse(lib.uriString).path ?: "").exists())
            }
            if (match != null) {
                newMatches[opdsBook.id] = match
            }
        }
        if (newMatches.isNotEmpty()) {
            _downloadedBooks.update { it + newMatches }
        }
    }

    fun selectBookForDetails(book: OpdsBook?) {
        _selectedBookForDetails.value = book
    }

    fun setBaseUrl(url: String) {
        applyBaseUrl(OpdsUrl.normalize(url) ?: OpdsService.DEFAULT_BASE_URL)
    }

    fun resetBaseUrl() {
        applyBaseUrl(OpdsService.DEFAULT_BASE_URL)
    }

    private fun applyBaseUrl(normalizedUrl: String) {
        viewModelScope.launch {
            preferencesManager.setCatalogBaseUrl(normalizedUrl)
            // A custom address only takes effect together with this flag.
            preferencesManager.setCustomOpdsEnabled(normalizedUrl != OpdsService.DEFAULT_BASE_URL)
            // Reload the root of the new catalog right away instead of retrying
            // the previous request against the address that just failed.
            navStack.clear()
            _canGoBack.value = false
            _searchQuery.value = ""
            currentEntry = CatalogHistoryEntry(title = "Главная", isHome = true)
            executeCategory("", "Главная", normalizedUrl)
        }
    }

    fun search(query: String = _searchQuery.value, scope: OpdsSearchType = _searchScope.value) {
        val q = query.trim()
        if (q.isEmpty()) return
        _searchQuery.value = q
        _searchScope.value = scope
        viewModelScope.launch {
            preferencesManager.saveCatalogSearchQuery(q)
        }

        if (currentEntry.query != q || currentEntry.searchScope != scope) {
            navStack.add(currentEntry)
            _canGoBack.value = true
        }
        currentEntry = CatalogHistoryEntry(query = q, searchScope = scope, title = "Результаты поиска: $q")
        executeSearch(q, scope)
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
            _searchScope.value = prev.searchScope
            executeSearch(prev.query, prev.searchScope)
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

    private fun executeCategory(path: String, categoryTitle: String, baseUrlOverride: String? = null) {
        lastAction = { executeCategory(path, categoryTitle, baseUrlOverride) }
        viewModelScope.launch {
            _uiState.value = OpdsUiState.Loading
            val currentBase = baseUrlOverride ?: baseUrl.value
            val result = OpdsService.getCategory(path, currentBase)
            result.onSuccess { list ->
                matchCatalogWithLibrary(list)
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

    private fun executeSearch(q: String, scope: OpdsSearchType = _searchScope.value) {
        lastAction = { executeSearch(q, scope) }
        viewModelScope.launch {
            _uiState.value = OpdsUiState.Loading
            val currentBase = baseUrl.value
            val result = OpdsService.searchCatalog(q, scope, currentBase)
            result.onSuccess { searchRes ->
                val flatBooks = when (scope) {
                    OpdsSearchType.ALL -> searchRes.books + searchRes.series + searchRes.authors
                    OpdsSearchType.BOOKS -> searchRes.books
                    OpdsSearchType.SERIES -> searchRes.series
                    OpdsSearchType.AUTHORS -> searchRes.authors
                }
                matchCatalogWithLibrary(flatBooks)
                _uiState.value = OpdsUiState.Success(
                    books = flatBooks,
                    currentTitle = "Результаты поиска: $q",
                    searchResult = searchRes
                )
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
        val downloadUrl = when (format) {
            BookFormat.FB2 -> book.fb2Url
            BookFormat.EPUB -> book.epubUrl
            BookFormat.PDF -> book.pdfUrl
            BookFormat.MOBI -> book.mobiUrl
            else -> book.fb2Url ?: book.epubUrl ?: book.mobiUrl ?: book.pdfUrl
        }
        if (downloadUrl.isNullOrBlank()) return

        val bookId = book.id
        val job = viewModelScope.launch {
            try {
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
                downloadJobs.remove(bookId)

                result.onSuccess { file ->
                    val openResult = bookRepository.openBookFromUri(Uri.fromFile(file))
                    openResult.onSuccess { loadedBook ->
                        _downloadedBooks.update { it + (bookId to loadedBook) }
                        onComplete?.invoke(loadedBook)
                    }
                }.onFailure { e ->
                    if (e !is kotlinx.coroutines.CancellationException) {
                        _uiState.value = OpdsUiState.Error(
                            message = "Ошибка загрузки: ${e.localizedMessage}",
                            isConnectionError = OpdsService.isConnectionError(e)
                        )
                    }
                }
            } finally {
                _activeDownloads.value = _activeDownloads.value - bookId
                downloadJobs.remove(bookId)
            }
        }
        downloadJobs[bookId] = job
    }

    fun cancelDownload(bookId: String) {
        downloadJobs[bookId]?.cancel()
        downloadJobs.remove(bookId)
        _activeDownloads.value = _activeDownloads.value - bookId
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
