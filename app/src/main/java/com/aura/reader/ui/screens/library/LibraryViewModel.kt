package com.aura.reader.ui.screens.library

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.reader.data.model.Book
import com.aura.reader.data.repository.BookRepository
import com.aura.reader.data.updater.AppUpdateManager
import com.aura.reader.data.updater.UpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

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

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    private val _downloadProgress = MutableStateFlow<Float?>(null)
    val downloadProgress: StateFlow<Float?> = _downloadProgress.asStateFlow()

    private val _foundDeviceFiles = MutableStateFlow<List<File>>(emptyList())
    val foundDeviceFiles: StateFlow<List<File>> = _foundDeviceFiles.asStateFlow()

    init {
        viewModelScope.launch {
            bookRepository.loadRecentBooks()
        }
        // Scan for books and check updates
        scanDeviceForBooks()
        checkForUpdates(manual = false)
    }

    fun checkForUpdates(manual: Boolean = true) {
        viewModelScope.launch {
            val currentVersion = "v${com.aura.reader.BuildConfig.VERSION_NAME}"
            val result = AppUpdateManager.checkForUpdates(currentVersion)
            result.onSuccess { info ->
                if (info != null && info.isAvailable) {
                    _updateInfo.value = info
                } else if (manual) {
                    _uiState.value = LibraryUiState.Error("У вас установлена последняя версия!")
                }
            }.onFailure { e ->
                if (manual) {
                    _uiState.value = LibraryUiState.Error("Не удалось проверить обновления: ${e.localizedMessage}")
                }
            }
        }
    }

    fun dismissUpdateDialog() {
        _updateInfo.value = null
    }

    fun startUpdateDownload(context: Context, downloadUrl: String) {
        viewModelScope.launch {
            _downloadProgress.value = 0f
            val result = AppUpdateManager.downloadAndInstallApk(context, downloadUrl) { progress ->
                _downloadProgress.value = progress
            }
            _downloadProgress.value = null
            result.onFailure { e ->
                _uiState.value = LibraryUiState.Error("Ошибка загрузки обновления: ${e.localizedMessage}")
            }
        }
    }

    fun scanDeviceForBooks() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<File>()
            val candidates = listOf(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                File(Environment.getExternalStorageDirectory(), "Books"),
                File(Environment.getExternalStorageDirectory(), "Download"),
                File(Environment.getExternalStorageDirectory(), "Documents")
            )

            for (dir in candidates) {
                if (dir.exists() && dir.isDirectory) {
                    try {
                        dir.walkTopDown().maxDepth(3).forEach { file ->
                            val name = file.name.lowercase()
                            if (file.isFile && (name.endsWith(".fb2") || name.endsWith(".fb2.zip") || name.endsWith(".epub") || name.endsWith(".txt"))) {
                                if (!list.any { it.absolutePath == file.absolutePath }) {
                                    list.add(file)
                                }
                            }
                        }
                    } catch (e: Exception) {}
                }
            }
            _foundDeviceFiles.value = list.sortedByDescending { it.lastModified() }
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

    fun openBooksFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        if (uris.size == 1) {
            openBookFromUri(uris.first())
            return
        }

        viewModelScope.launch {
            _uiState.value = LibraryUiState.Loading
            var addedCount = 0
            val errors = mutableListOf<String>()

            for (uri in uris) {
                val result = bookRepository.openBookFromUri(uri)
                result.onSuccess {
                    addedCount++
                }.onFailure { e ->
                    errors.add(e.localizedMessage ?: "Неизвестная ошибка")
                }
            }

            if (addedCount > 0) {
                if (errors.isEmpty()) {
                    _uiState.value = LibraryUiState.Error("Добавлено книг в библиотеку: $addedCount")
                } else {
                    val firstErr = errors.first()
                    _uiState.value = LibraryUiState.Error("Добавлено книг: $addedCount. $firstErr")
                }
            } else {
                val firstErr = errors.firstOrNull() ?: "Не удалось добавить выбранные файлы"
                _uiState.value = LibraryUiState.Error(firstErr)
            }
        }
    }

    fun openBookFromFile(file: File) {
        openBookFromUri(Uri.fromFile(file))
    }

    fun openSampleBook() {
        val sample = bookRepository.loadSampleBook()
        _uiState.value = LibraryUiState.Success(sample)
    }

    fun resetUiState() {
        _uiState.value = LibraryUiState.Idle
    }
}
