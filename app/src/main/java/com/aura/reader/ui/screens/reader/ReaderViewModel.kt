package com.aura.reader.ui.screens.reader

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    val materialYouEnabled: StateFlow<Boolean> = preferencesManager.materialYouEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val appLanguage: StateFlow<com.aura.reader.ui.theme.AppLanguage> = preferencesManager.appLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.aura.reader.ui.theme.AppLanguage.RU)

    val nextBookInSeries: StateFlow<Book?> = combine(
        bookRepository.currentBook,
        bookRepository.recentBooks
    ) { current, recents ->
        if (current == null || current.series.isNullOrBlank()) null
        else {
            val currentNum = current.seriesNumber
            if (currentNum != null) {
                recents.find {
                    it.id != current.id &&
                    it.series.equals(current.series, ignoreCase = true) &&
                    it.seriesNumber == currentNum + 1
                }
            } else {
                null
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun openNextBookInSeries(nextBook: Book) {
        isAtBookEnd = false
        viewModelScope.launch {
            val result = bookRepository.openBook(nextBook)
            result.onSuccess { opened ->
                _currentChapterIndex.value = opened.currentChapterIndex
                _savedScrollOffset.value = opened.currentScrollOffset
                _savedTextOffset.value = opened.currentBlockTextOffset
                _savedPixelOffset.value = opened.currentScrollPixelOffset
            }
        }
    }

    private val _currentChapterIndex = MutableStateFlow(0)
    val currentChapterIndex: StateFlow<Int> = _currentChapterIndex.asStateFlow()

    private val _savedScrollOffset = MutableStateFlow(0)
    val savedScrollOffset: StateFlow<Int> = _savedScrollOffset.asStateFlow()

    private val _savedTextOffset = MutableStateFlow(0)
    val savedTextOffset = _savedTextOffset.asStateFlow()
    private val _savedPixelOffset = MutableStateFlow(0)
    val savedPixelOffset = _savedPixelOffset.asStateFlow()

    init {
        currentBook.value?.let {
            _currentChapterIndex.value = it.currentChapterIndex
            _savedScrollOffset.value = it.currentScrollOffset
            _savedTextOffset.value = it.currentBlockTextOffset
            _savedPixelOffset.value = it.currentScrollPixelOffset
        }
        viewModelScope.launch {
            com.aura.reader.service.BookTtsService.ttsState.collect { state ->
                if (state.isServiceRunning && state.totalParagraphs > 0) {
                    val book = currentBook.value
                    val chapter = book?.chapters?.getOrNull(_currentChapterIndex.value)
                    if (chapter != null) {
                        val validBlocks = chapter.blocks.mapIndexedNotNull { originalIndex, block ->
                            if (block.type != com.aura.reader.data.model.BlockType.IMAGE &&
                                block.type != com.aura.reader.data.model.BlockType.DIVIDER &&
                                block.text.isNotBlank()
                            ) {
                                originalIndex
                            } else null
                        }
                        val originalBlockIndex = validBlocks.getOrNull(state.currentParagraphIndex) ?: state.currentParagraphIndex
                        if (originalBlockIndex != _savedScrollOffset.value) {
                            _savedScrollOffset.value = originalBlockIndex
                            saveCurrentProgress()
                        }
                    } else {
                        if (state.currentParagraphIndex != _savedScrollOffset.value) {
                            _savedScrollOffset.value = state.currentParagraphIndex
                            saveCurrentProgress()
                        }
                    }
                }
            }
        }
    }

    fun openBook(book: Book) {
        isAtBookEnd = false
        _currentChapterIndex.value = book.currentChapterIndex
        _savedScrollOffset.value = book.currentScrollOffset
        _savedTextOffset.value = book.currentBlockTextOffset
        _savedPixelOffset.value = book.currentScrollPixelOffset
        bookRepository.prepareBook(book)
        viewModelScope.launch {
            bookRepository.openBook(book)
        }
    }

    fun setChapter(index: Int, offset: Int = 0) {
        val book = currentBook.value ?: return
        if (index in book.chapters.indices) {
            isAtBookEnd = false
            _currentChapterIndex.value = index
            _savedScrollOffset.value = offset
            _savedTextOffset.value = 0
            _savedPixelOffset.value = 0
            saveCurrentProgress()
        }
    }

    fun navigateToLink(target: String): Boolean {
        val book = currentBook.value ?: return false
        if (target.startsWith("nav:")) {
            val parts = target.removePrefix("nav:").split(":")
            val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
            setChapter(ch, blk)
            return true
        }
        val noteVal = book.footnotes[target] ?: book.footnotes[target.removePrefix("#")]
        if (noteVal != null && noteVal.startsWith("nav:")) {
            val parts = noteVal.removePrefix("nav:").split(":")
            val ch = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val blk = parts.getOrNull(1)?.toIntOrNull() ?: 0
            setChapter(ch, blk)
            return true
        }
        val clean = target.substringAfterLast("/").substringBefore("#").lowercase().trim()
        val chIdx = book.chapters.indexOfFirst {
            it.title.equals(target, ignoreCase = true) ||
            (clean.isNotBlank() && it.id.equals(clean, ignoreCase = true))
        }
        if (chIdx >= 0) {
            setChapter(chIdx, 0)
            return true
        }
        return false
    }

    fun nextChapter() {
        val book = currentBook.value ?: return
        val next = _currentChapterIndex.value + 1
        if (next < book.chapters.size) {
            setChapter(next, 0)
        }
    }

    fun prevChapter(startAtEnd: Boolean = false) {
        val book = currentBook.value ?: return
        val prev = _currentChapterIndex.value - 1
        if (prev >= 0) {
            val totalBlocks = book.chapters.getOrNull(prev)?.blocks?.size ?: 0
            val offset = if (startAtEnd && totalBlocks > 0) totalBlocks - 1 else 0
            setChapter(prev, offset)
        }
    }

    private var isAtBookEnd = false
    private var pendingProgressSave: kotlinx.coroutines.Job? = null

    fun updateScrollProgress(scrollItemIndex: Int, totalItems: Int, textOffset: Int = 0, pixelOffset: Int = 0, atEnd: Boolean = false) {
        isAtBookEnd = atEnd
        _savedScrollOffset.value = scrollItemIndex
        _savedTextOffset.value = textOffset
        _savedPixelOffset.value = pixelOffset
        pendingProgressSave?.cancel()
        pendingProgressSave = viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            saveCurrentProgress()
        }
    }

    fun saveCurrentProgress() {
        val book = currentBook.value ?: return
        val chapterIdx = _currentChapterIndex.value
        val block = _savedScrollOffset.value
        val text = _savedTextOffset.value
        val pixel = _savedPixelOffset.value
        val percent = if (isAtBookEnd || (book.progressPercent == 100 && book.currentChapterIndex == chapterIdx &&
            book.currentScrollOffset == block && book.currentBlockTextOffset == text && book.currentScrollPixelOffset == pixel)) 100
            else com.aura.reader.data.model.ReadingPosition.percent(book, chapterIdx, block, text)
        viewModelScope.launch {
            // Do not persist a position captured for a different book after navigation.
            if (bookRepository.currentBook.value?.id == book.id) {
                bookRepository.updateReadingProgress(chapterIdx, block, percent, text, pixel, expectedBookId = book.id)
            }
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

    fun setMaterialYouEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setMaterialYouEnabled(enabled)
        }
    }

    fun setFontFamily(family: ReaderFontFamily) {
        viewModelScope.launch {
            preferencesManager.updateFontFamily(family)
        }
    }

    fun setFontName(name: String) {
        viewModelScope.launch {
            preferencesManager.updateFontName(name)
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

    fun setAutoHyphenation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateAutoHyphenation(enabled)
        }
    }

    fun setTwoColumnMode(mode: com.aura.reader.data.model.TwoColumnMode) {
        viewModelScope.launch {
            preferencesManager.updateTwoColumnMode(mode)
        }
    }

    fun setPageAnimation(animation: com.aura.reader.data.model.PageTurnAnimation) {
        viewModelScope.launch {
            preferencesManager.updatePageAnimation(animation)
        }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateHapticFeedbackEnabled(enabled)
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
        saveCurrentProgress()
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

    fun addQuote(text: String, color: Long = 0xFFFFF59DL) {
        val book = currentBook.value ?: return
        viewModelScope.launch {
            bookRepository.addQuote(
                com.aura.reader.data.model.Quote(
                    bookId = book.id,
                    bookTitle = book.title,
                    chapterIndex = _currentChapterIndex.value,
                    text = text.trim(),
                    color = color
                )
            )
        }
    }

    fun removeQuote(id: String) {
        viewModelScope.launch {
            bookRepository.removeQuote(id)
        }
    }

    // --- Dynamic Adaptive Reading Speed ---
    val userAverageWpm: StateFlow<Float> = preferencesManager.userAverageWpm
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 200f)

    private var pageEnterTimestamp: Long = System.currentTimeMillis()
    private var lastRecordedWordsCount: Int = 0

    fun onPageOrSectionTurn(wordsCount: Int) {
        val now = System.currentTimeMillis()
        val elapsedSecs = (now - pageEnterTimestamp) / 1000f
        if (elapsedSecs in 4f..240f && lastRecordedWordsCount > 15) {
            val sampleWpm = (lastRecordedWordsCount / elapsedSecs) * 60f
            if (sampleWpm in 70f..650f) {
                val currentAvg = userAverageWpm.value
                val newAvg = (currentAvg * 0.82f + sampleWpm * 0.18f).coerceIn(80f, 600f)
                viewModelScope.launch {
                    preferencesManager.updateAverageWpm(newAvg)
                }
            }
        }
        pageEnterTimestamp = now
        lastRecordedWordsCount = wordsCount
    }

    // --- TTS Narration ---
    fun startTts(context: Context, startIndex: Int? = null, selectedText: String? = null) {
        val book = currentBook.value ?: return
        val chapter = book.chapters.getOrNull(_currentChapterIndex.value) ?: return

        val validBlocksWithIndex = chapter.blocks.mapIndexedNotNull { originalIndex, block ->
            if (block.type != com.aura.reader.data.model.BlockType.IMAGE &&
                block.type != com.aura.reader.data.model.BlockType.DIVIDER &&
                block.text.isNotBlank()
            ) {
                originalIndex to block.text.trim()
            } else null
        }

        if (validBlocksWithIndex.isEmpty()) return

        val paragraphs = ArrayList(validBlocksWithIndex.map { it.second })

        val targetTtsIndex = when {
            !selectedText.isNullOrBlank() -> {
                val clean = selectedText.trim()
                val found = validBlocksWithIndex.indexOfFirst { (_, text) ->
                    text.contains(clean, ignoreCase = true) || clean.contains(text, ignoreCase = true)
                }
                if (found != -1) found else 0
            }
            startIndex != null -> {
                val found = validBlocksWithIndex.indexOfFirst { (origIdx, _) -> origIdx >= startIndex }
                if (found != -1) found else startIndex.coerceIn(0, paragraphs.size - 1)
            }
            else -> {
                val savedBlock = _savedScrollOffset.value
                val found = validBlocksWithIndex.indexOfFirst { (origIdx, _) -> origIdx >= savedBlock }
                if (found != -1) found else 0
            }
        }.coerceIn(0, paragraphs.size - 1)

        viewModelScope.launch {
            val speed = preferencesManager.ttsSpeed.first()
            val pitch = preferencesManager.ttsPitch.first()
            val savedVoice = preferencesManager.ttsSelectedVoiceName.first()

            com.aura.reader.service.BookTtsService.start(
                context = context,
                bookTitle = book.title,
                chapterTitle = chapter.title.ifBlank { "Глава ${_currentChapterIndex.value + 1}" },
                paragraphs = paragraphs,
                startIndex = targetTtsIndex,
                speed = speed,
                pitch = pitch,
                initialVoice = savedVoice
            )
        }
    }

    fun resetReadingSpeed() {
        viewModelScope.launch {
            preferencesManager.resetAverageWpm()
        }
    }
}
