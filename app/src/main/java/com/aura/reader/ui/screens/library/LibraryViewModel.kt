package com.aura.reader.ui.screens.library

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Idle : LibraryUiState
    data object Loading : LibraryUiState
    data class Success(val book: Book) : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel(
    private val bookRepository: BookRepository
) : ViewModel() {

    val recentBooks: StateFlow<List<Book>> = bookRepository.recentBooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Idle)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.loadRecentBooks()
        }
    }

    fun openBookFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            val result = bookRepository.openBookFromUri(uri)
            result.onSuccess { book ->
                _uiState.value = LibraryUiState.Success(book)
            }.onFailure { error ->
                _uiState.value = LibraryUiState.Error(error.localizedMessage ?: "Не удалось открыть книгу")
            }
        }
    }

    fun openSampleBook() {
        val sample = bookRepository.loadSampleBook()
        _uiState.value = LibraryUiState.Success(sample)
    }

    fun resetUiState() {
        _uiState.value = LibraryUiState.Idle
    }
}
