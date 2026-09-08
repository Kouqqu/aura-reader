package com.aura.reader.ui.screens.flibusta

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.FlibustaBook
import com.aura.reader.data.opds.FlibustaService
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
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

sealed interface FlibustaUiState {
    object Idle : FlibustaUiState
    object Loading : FlibustaUiState
    data class Success(val books: List<FlibustaBook>, val currentTitle: String = "") : FlibustaUiState
    data class Error(val message: String, val isConnectionError: Boolean = false) : FlibustaUiState
}

class FlibustaViewModel(
    private val bookRepository: BookRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _sortOption = MutableStateFlow(CatalogSortOption.DEFAULT)
    val sortOption: StateFlow<CatalogSortOption> = _sortOption.asStateFlow()

    fun setSortOption(option: CatalogSortOption) {
        _sortOption.value = option
    }

    fun getSortedBooks(books: List<FlibustaBook>, option: CatalogSortOption): List<FlibustaBook> {
        val nonCats = books.filter { !it.isCategory }
        val cats = books.filter { it.isCategory }
        val sortedNonCats = when (option) {
            CatalogSortOption.DEFAULT -> nonCats
            CatalogSortOption.POPULAR_DESC -> nonCats.sortedWith(
                compareByDescending<FlibustaBook> { it.downloadsCount }
                    .thenBy { it.title.lowercase() }
            )
            CatalogSortOption.POPULAR_ASC -> nonCats.sortedWith(
                compareBy<FlibustaBook> { it.downloadsCount }
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

    val searchHistory: StateFlow<List<String>> = preferencesManager.flibustaSearchHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val baseUrl: StateFlow<String> = preferencesManager.flibustaBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FlibustaService.DEFAULT_BASE_URL)

    private val _uiState = MutableStateFlow<FlibustaUiState>(FlibustaUiState.Idle)
    val uiState: StateFlow<FlibustaUiState> = _uiState.asStateFlow()

    private val _activeDownloads = MutableStateFlow<Map<String, Int>>(emptyMap())
    val activeDownloads: StateFlow<Map<String, Int>> = _activeDownloads.asStateFlow()

    private val _downloadedBooks = MutableStateFlow<Map<String, Book>>(emptyMap())
    val downloadedBooks: StateFlow<Map<String, Book>> = _downloadedBooks.asStateFlow()

    private val _selectedBookForDetails = MutableStateFlow<FlibustaBook?>(null)
    val selectedBookForDetails: StateFlow<FlibustaBook?> = _selectedBookForDetails.asStateFlow()

    private var lastAction: (() -> Unit)? = null

    init {
        // Load initial catalog (new books)
        loadCategory("/opds/new", "Новинки")
    }

    fun selectBookForDetails(book: FlibustaBook?) {
        _selectedBookForDetails.value = book
    }

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            preferencesManager.setFlibustaBaseUrl(url)
            retry()
        }
    }

    fun search(query: String = _searchQuery.value) {
        val q = query.trim()
        if (q.isEmpty()) return
        _searchQuery.value = q
        viewModelScope.launch {
            preferencesManager.saveFlibustaSearchQuery(q)
        }

        lastAction = { search(q) }
        viewModelScope.launch {
            _uiState.value = FlibustaUiState.Loading
            val currentBase = baseUrl.value
            val result = FlibustaService.searchBooks(q, currentBase)
            result.onSuccess { list ->
                _uiState.value = FlibustaUiState.Success(list, "Результаты поиска: $q")
            }.onFailure { error ->
                val isConn = FlibustaService.isConnectionError(error)
                _uiState.value = FlibustaUiState.Error(
                    message = error.localizedMessage ?: "Ошибка связи с каталогом",
                    isConnectionError = isConn
                )
            }
        }
    }

    fun loadCategory(path: String, categoryTitle: String) {
        lastAction = { loadCategory(path, categoryTitle) }
        viewModelScope.launch {
            _uiState.value = FlibustaUiState.Loading
            val currentBase = baseUrl.value
            val result = FlibustaService.getCategory(path, currentBase)
            result.onSuccess { list ->
                _uiState.value = FlibustaUiState.Success(list, categoryTitle)
            }.onFailure { error ->
                val isConn = FlibustaService.isConnectionError(error)
                _uiState.value = FlibustaUiState.Error(
                    message = error.localizedMessage ?: "Ошибка связи с каталогом",
                    isConnectionError = isConn
                )
            }
        }
    }

    fun downloadBook(
        context: Context,
        book: FlibustaBook,
        format: BookFormat,
        onComplete: ((Book) -> Unit)? = null
    ) {
        val downloadUrl = if (format == BookFormat.FB2) book.fb2Url else book.epubUrl
        if (downloadUrl.isNullOrBlank()) return

        val bookId = book.id
        viewModelScope.launch {
            _activeDownloads.value = _activeDownloads.value + (bookId to 0)
            val result = FlibustaService.downloadAndExtractBook(
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
                _uiState.value = FlibustaUiState.Error(
                    message = "Ошибка загрузки: ${e.localizedMessage}",
                    isConnectionError = FlibustaService.isConnectionError(e)
                )
            }
        }
    }

    fun removeHistoryQuery(query: String) {
        viewModelScope.launch {
            preferencesManager.removeFlibustaSearchQuery(query)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            preferencesManager.clearFlibustaSearchHistory()
        }
    }

    fun retry() {
        lastAction?.invoke() ?: loadCategory("/opds/new", "Новинки")
    }
}
