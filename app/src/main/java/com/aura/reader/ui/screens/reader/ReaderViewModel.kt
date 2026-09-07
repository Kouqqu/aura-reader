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
}
