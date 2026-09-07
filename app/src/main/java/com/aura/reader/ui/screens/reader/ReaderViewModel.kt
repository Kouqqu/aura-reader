package com.aura.reader.ui.screens.reader

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val bookRepository: BookRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val currentBook: StateFlow<Book?> = bookRepository.currentBook
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readerSettings: StateFlow<ReaderSettings> = preferencesManager.readerSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReaderSettings())

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    private val _savedScrollOffset = MutableStateFlow(0)
    val savedScrollOffset: StateFlow<Int> = _savedScrollOffset.asStateFlow()

    init {
        currentBook.value?.let {
            _currentChapterIndex.value = it.currentChapterIndex
            _savedScrollOffset.value = it.currentScrollOffset
        }
    }

    fun openBook(book: Book) {
        _currentChapterIndex.value = book.currentChapterIndex
        _savedScrollOffset.value = book.currentScrollOffset
    }

    fun setChapter(index: Int) {
        val book = currentBook.value ?: return
        if (index in book.chapters.indices) {
            _currentChapterIndex.value = index
            _savedScrollOffset.value = 0
            saveProgress(index, 0)
        }
    }

    fun nextChapter() {
        val book = currentBook.value ?: return
        val next = _currentChapterIndex.value + 1
        if (next < book.chapters.size) {
            setChapter(next)
        }
    }

    fun prevChapter() {
        val prev = _currentChapterIndex.value - 1
        if (prev >= 0) {
            setChapter(prev)
        }
    }

    fun updateScrollProgress(scrollItemIndex: Int, totalItems: Int) {
        val book = currentBook.value ?: return
        val chapterIdx = _currentChapterIndex.value
        val totalChapters = book.chapters.size.coerceAtLeast(1)

        val intraChapterProgress = if (totalItems > 1) {
            (scrollItemIndex.toFloat() / (totalItems - 1)).coerceIn(0f, 1f)
        } else 0f

        val overallPercent = (((chapterIdx + intraChapterProgress) / totalChapters) * 100).toInt().coerceIn(0, 100)

        _savedScrollOffset.value = scrollItemIndex

        viewModelScope.launch {
            bookRepository.updateReadingProgress(chapterIdx, scrollItemIndex, overallPercent)
        }
    }

    private fun saveProgress(chapterIdx: Int, scrollOffset: Int) {
        val book = currentBook.value ?: return
        val totalChapters = book.chapters.size.coerceAtLeast(1)
        val overallPercent = ((chapterIdx.toFloat() / totalChapters) * 100).toInt().coerceIn(0, 100)
        viewModelScope.launch {
            bookRepository.updateReadingProgress(chapterIdx, scrollOffset, overallPercent)
        }
    }

    fun setFontSize(sizeSp: Float) {
        viewModelScope.launch {
            preferencesManager.updateFontSize(sizeSp)
        }
    }

    fun setLineHeight(multiplier: Float) {
        viewModelScope.launch {
            preferencesManager.updateLineHeight(multiplier)
        }
    }

    fun setThemeMode(mode: ReaderThemeMode) {
        viewModelScope.launch {
            preferencesManager.updateThemeMode(mode)
        }
    }

    fun setFontFamily(family: ReaderFontFamily) {
        viewModelScope.launch {
            preferencesManager.updateFontFamily(family)
        }
    }

    fun setLightImageBackground(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateLightImageBackground(enabled)
        }
    }

    fun setPagingMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updatePagingMode(enabled)
        }
    }

    // --- Active Reading Timer ---
    private var readingStartTime: Long = 0L

    fun onResumeReading() {
        readingStartTime = System.currentTimeMillis()
    }

    fun onPauseReading() {
        if (readingStartTime > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - readingStartTime) / 1000
            if (elapsedSeconds in 1..7200) {
                viewModelScope.launch {
                    bookRepository.addReadingSeconds(elapsedSeconds)
                }
            }
            readingStartTime = 0L
        }
    }

    // --- In-Book Search ---
    data class SearchMatch(
        val chapterIndex: Int,
        val chapterTitle: String,
        val blockIndex: Int,
        val snippet: String
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchMatch>>(emptyList())
    val searchResults: StateFlow<List<SearchMatch>> = _searchResults.asStateFlow()

    private val _currentSearchIndex = MutableStateFlow(0)
    val currentSearchIndex: StateFlow<Int> = _currentSearchIndex.asStateFlow()

    private val _targetScrollOffset = MutableStateFlow<Int?>(null)
    val targetScrollOffset: StateFlow<Int?> = _targetScrollOffset.asStateFlow()

    fun performSearch(query: String) {
        _searchQuery.value = query
        val q = query.trim()
        if (q.length < 2) {
            _searchResults.value = emptyList()
            _currentSearchIndex.value = 0
            return
        }

        val book = currentBook.value ?: return
        val results = mutableListOf<SearchMatch>()
        for ((chIdx, chapter) in book.chapters.withIndex()) {
            for ((bIdx, block) in chapter.blocks.withIndex()) {
                if (block.text.contains(q, ignoreCase = true)) {
                    val idx = block.text.indexOf(q, ignoreCase = true)
                    val start = (idx - 25).coerceAtLeast(0)
                    val end = (idx + q.length + 35).coerceAtMost(block.text.length)
                    val snippet = "…" + block.text.substring(start, end).replace("\n", " ") + "…"
                    results.add(SearchMatch(chIdx, chapter.title, bIdx, snippet))
                }
            }
        }
        _searchResults.value = results
        _currentSearchIndex.value = 0
        if (results.isNotEmpty()) {
            jumpToMatch(results[0])
        }
    }

    fun nextSearchResult() {
        val list = _searchResults.value
        if (list.isEmpty()) return
        val next = (_currentSearchIndex.value + 1) % list.size
        _currentSearchIndex.value = next
        jumpToMatch(list[next])
    }

    fun prevSearchResult() {
        val list = _searchResults.value
        if (list.isEmpty()) return
        val prev = if (_currentSearchIndex.value - 1 < 0) list.size - 1 else _currentSearchIndex.value - 1
        _currentSearchIndex.value = prev
        jumpToMatch(list[prev])
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _currentSearchIndex.value = 0
        _targetScrollOffset.value = null
    }

    private fun jumpToMatch(match: SearchMatch) {
        if (match.chapterIndex != _currentChapterIndex.value) {
            setChapter(match.chapterIndex)
        }
        _targetScrollOffset.value = match.blockIndex
    }

    fun consumeTargetScrollOffset() {
        _targetScrollOffset.value = null
    }

    // --- Bookmarks and Quotes ---
    val bookmarks = bookRepository.bookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quotes = bookRepository.quotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleBookmark(previewText: String) {
        val book = currentBook.value ?: return
        val chapterIdx = _currentChapterIndex.value
        val existing = bookmarks.value.find { it.bookId == book.id && it.chapterIndex == chapterIdx }
        viewModelScope.launch {
            if (existing != null) {
                bookRepository.removeBookmark(existing.id)
            } else {
                val chapter = book.chapters.getOrNull(chapterIdx)
                bookRepository.addBookmark(
                    com.aura.reader.data.model.Bookmark(
                        bookId = book.id,
                        chapterIndex = chapterIdx,
                        scrollOffset = _savedScrollOffset.value,
                        chapterTitle = chapter?.title ?: "Глава ${chapterIdx + 1}",
                        previewText = previewText.take(120)
                    )
                )
            }
        }
    }

    fun removeBookmark(id: String) {
        viewModelScope.launch {
            bookRepository.removeBookmark(id)
        }
    }

    fun addQuote(text: String) {
        val book = currentBook.value ?: return
        viewModelScope.launch {
            bookRepository.addQuote(
                com.aura.reader.data.model.Quote(
                    bookId = book.id,
                    bookTitle = book.title,
                    chapterIndex = _currentChapterIndex.value,
                    text = text.trim()
                )
            )
        }
    }

    fun removeQuote(id: String) {
        viewModelScope.launch {
            bookRepository.removeQuote(id)
        }
    }
}
