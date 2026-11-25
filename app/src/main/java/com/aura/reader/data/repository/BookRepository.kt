package com.aura.reader.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.parser.BookIngestionPipeline
import com.aura.reader.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

class BookRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val ingestionPipeline = BookIngestionPipeline(context)

    private suspend fun <T> withLibraryLock(block: suspend () -> T): T =
        withContext(Dispatchers.IO) { preferencesManager.libraryMutex.withLock { block() } }

    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _recentBooks = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks.asStateFlow()

    suspend fun loadRecentBooks() = withLibraryLock {
        val json = preferencesManager.recentBooksJson.first()
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<Book>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val collectionsList = mutableListOf<String>()
                    if (obj.has("collections")) {
                        val cArr = obj.getJSONArray("collections")
                        for (j in 0 until cArr.length()) collectionsList.add(cArr.getString(j))
                    }
                    val genresList = mutableListOf<String>()
                    if (obj.has("genres")) {
                        val gArr = obj.getJSONArray("genres")
                        for (j in 0 until gArr.length()) genresList.add(gArr.getString(j))
                    }
                    val deletedAt = if (obj.has("deletedAtTimestamp") && !obj.isNull("deletedAtTimestamp")) obj.getLong("deletedAtTimestamp") else null
                    val fileSizeBytes = obj.optLong("fileSizeBytes", 0L)
                    val addedTimestamp = obj.optLong("addedTimestamp", obj.optLong("lastReadTimestamp", System.currentTimeMillis()))
                    val firstOpenedTimestamp = if (obj.has("firstOpenedTimestamp") && !obj.isNull("firstOpenedTimestamp")) obj.getLong("firstOpenedTimestamp") else null
                    val description = if (obj.has("description") && !obj.isNull("description")) obj.getString("description") else null
                    val charCount = obj.optLong("charCount", 0L)
                    val wordCount = obj.optInt("wordCount", 0)

                    list.add(
                        Book(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            author = obj.optString("author", ""),
                            coverBase64 = if (obj.has("coverBase64") && !obj.isNull("coverBase64")) obj.getString("coverBase64") else null,
                            format = try { BookFormat.valueOf(obj.getString("format")) } catch (e: Exception) { BookFormat.FB2 },
                            uriString = obj.getString("uriString"),
                            currentChapterIndex = obj.optInt("currentChapterIndex", 0),
                            currentScrollOffset = obj.optInt("currentScrollOffset", 0),
                            currentBlockTextOffset = obj.optInt("currentBlockTextOffset", 0),
                            currentScrollPixelOffset = obj.optInt("currentScrollPixelOffset", 0),
                            progressPercent = obj.optInt("progressPercent", 0),
                            lastReadTimestamp = obj.optLong("lastReadTimestamp", System.currentTimeMillis()),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            collections = collectionsList,
                            series = if (obj.has("series") && !obj.isNull("series")) obj.getString("series") else null,
                            seriesNumber = if (obj.has("seriesNumber") && !obj.isNull("seriesNumber")) obj.getInt("seriesNumber") else null,
                            manualShelf = if (obj.has("manualShelf") && !obj.isNull("manualShelf")) obj.getString("manualShelf") else null,
                            deletedAtTimestamp = deletedAt,
                            fileSizeBytes = fileSizeBytes,
                            addedTimestamp = addedTimestamp,
                            firstOpenedTimestamp = firstOpenedTimestamp,
                            description = description,
                            genres = genresList,
                            charCount = charCount,
                            wordCount = wordCount
                        )
                    )
                }
                val cutoff = System.currentTimeMillis() - (15L * 24L * 60L * 60L * 1000L)
                val expired = list.filter { it.deletedAtTimestamp != null && it.deletedAtTimestamp < cutoff }
                if (expired.isNotEmpty()) {
                    list.removeAll { it.deletedAtTimestamp != null && it.deletedAtTimestamp < cutoff }
                    saveRecentBooks(list)
                }
                _recentBooks.value = list
            } catch (e: Exception) {
                _recentBooks.value = emptyList()
            }
        }
    }

    internal suspend fun saveRecentBooks(books: List<Book>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        for (b in books) {
            val obj = JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("author", b.author)
                put("coverBase64", b.coverBase64)
                put("format", b.format.name)
                put("uriString", b.uriString)
                put("currentChapterIndex", b.currentChapterIndex)
                put("currentScrollOffset", b.currentScrollOffset)
                put("currentBlockTextOffset", b.currentBlockTextOffset)
                put("currentScrollPixelOffset", b.currentScrollPixelOffset)
                put("progressPercent", b.progressPercent)
                put("lastReadTimestamp", b.lastReadTimestamp)
                put("isFavorite", b.isFavorite)
                put("series", b.series)
                put("seriesNumber", b.seriesNumber)
                if (b.manualShelf != null) {
                    put("manualShelf", b.manualShelf)
                }
                if (b.deletedAtTimestamp != null) {
                    put("deletedAtTimestamp", b.deletedAtTimestamp)
                }
                put("fileSizeBytes", b.fileSizeBytes)
                put("addedTimestamp", b.addedTimestamp)
                if (b.firstOpenedTimestamp != null) {
                    put("firstOpenedTimestamp", b.firstOpenedTimestamp)
                }
                if (b.description != null) {
                    put("description", b.description)
                }
                if (b.charCount > 0L) {
                    put("charCount", b.charCount)
                }
                if (b.wordCount > 0) {
                    put("wordCount", b.wordCount)
                }
                val gArr = JSONArray()
                for (g in b.genres) gArr.put(g)
                put("genres", gArr)
                val cArr = JSONArray()
                for (c in b.collections) cArr.put(c)
                put("collections", cArr)
            }
            array.put(obj)
        }
        preferencesManager.saveRecentBooksJson(array.toString())
        // Keep library metadata only; retaining every imported chapter grows without bound.
        _recentBooks.value = books.map { it.copy(chapters = emptyList(), footnotes = emptyMap()) }
    }

    fun prepareBook(book: Book) {
        if (_currentBook.value?.id != book.id) {
            _currentBook.value = book
        }
    }

    suspend fun openBook(bookToOpen: Book): Result<Book> = withLibraryLock {
        if (bookToOpen.id == "sample_book" || bookToOpen.uriString == "sample://aura_guide") {
            val sample = loadSampleBook()
            return@withLibraryLock Result.success(sample)
        }
        val existingInMemory = _currentBook.value
        if (existingInMemory != null && existingInMemory.id == bookToOpen.id && existingInMemory.chapters.isNotEmpty()) {
            val totalChapters = existingInMemory.chapters.size
            val resolvedChapter = if (bookToOpen.currentChapterIndex == 0 && bookToOpen.currentScrollOffset > 0 && bookToOpen.currentScrollOffset < totalChapters) {
                bookToOpen.currentScrollOffset
            } else {
                bookToOpen.currentChapterIndex.coerceIn(0, (totalChapters - 1).coerceAtLeast(0))
            }
            val updated = existingInMemory.copy(
                currentChapterIndex = resolvedChapter,
                currentScrollOffset = bookToOpen.currentScrollOffset,
                currentBlockTextOffset = bookToOpen.currentBlockTextOffset,
                currentScrollPixelOffset = bookToOpen.currentScrollPixelOffset,
                progressPercent = bookToOpen.progressPercent,
                manualShelf = bookToOpen.manualShelf ?: existingInMemory.manualShelf,
                isFavorite = bookToOpen.isFavorite,
                collections = if (bookToOpen.collections.isNotEmpty()) bookToOpen.collections else existingInMemory.collections,
                firstOpenedTimestamp = existingInMemory.firstOpenedTimestamp ?: System.currentTimeMillis(),
                lastReadTimestamp = System.currentTimeMillis()
            )
            _currentBook.value = updated
            addOrUpdateRecentBook(updated)
            return@withLibraryLock Result.success(updated)
        }
        val uri = Uri.parse(bookToOpen.uriString)
        val fileName = getFileName(uri) ?: "${bookToOpen.title}.${bookToOpen.format.name.lowercase()}"
        val inputStream = getInputStreamForUri(uri, bookToOpen.id)
            ?: return@withLibraryLock Result.failure(Exception("Файл книги «${bookToOpen.title}» не найден на устройстве"))

        val result = ingestionPipeline.ingest(inputStream, uri, fileName, bookToOpen)
        result.onSuccess { book ->
            val totalChapters = book.chapters.size
            val resolvedChapter = if (bookToOpen.currentChapterIndex == 0 && bookToOpen.currentScrollOffset > 0 && bookToOpen.currentScrollOffset < totalChapters) {
                bookToOpen.currentScrollOffset
            } else {
                bookToOpen.currentChapterIndex.coerceIn(0, (totalChapters - 1).coerceAtLeast(0))
            }
            val resolved = book.copy(
                id = bookToOpen.id,
                title = if (bookToOpen.title.isNotBlank()) bookToOpen.title else book.title,
                author = if (bookToOpen.author.isNotBlank()) bookToOpen.author else book.author,
                series = bookToOpen.series ?: book.series,
                seriesNumber = bookToOpen.seriesNumber ?: book.seriesNumber,
                currentChapterIndex = resolvedChapter,
                currentScrollOffset = bookToOpen.currentScrollOffset,
                currentBlockTextOffset = bookToOpen.currentBlockTextOffset,
                currentScrollPixelOffset = bookToOpen.currentScrollPixelOffset,
                progressPercent = bookToOpen.progressPercent,
                manualShelf = bookToOpen.manualShelf,
                isFavorite = bookToOpen.isFavorite,
                collections = bookToOpen.collections,
                firstOpenedTimestamp = bookToOpen.firstOpenedTimestamp ?: System.currentTimeMillis(),
                lastReadTimestamp = System.currentTimeMillis(),
                description = bookToOpen.description ?: book.description,
                genres = if (bookToOpen.genres.isNotEmpty()) bookToOpen.genres else book.genres,
                fileSizeBytes = if (bookToOpen.fileSizeBytes > 0) bookToOpen.fileSizeBytes else book.fileSizeBytes,
                addedTimestamp = bookToOpen.addedTimestamp,
                deletedAtTimestamp = bookToOpen.deletedAtTimestamp,
                charCount = if (book.charCount > 0L) book.charCount else bookToOpen.charCount,
                wordCount = if (book.wordCount > 0) book.wordCount else bookToOpen.wordCount
            )
            _currentBook.value = resolved
            addOrUpdateRecentBook(resolved)
            return@withLibraryLock Result.success(resolved)
        }
        result
    }

    suspend fun openBookFromUri(uri: Uri): Result<Book> = withLibraryLock {
        val fileName = getFileName(uri) ?: "Книга"
        val inputStream = getInputStreamForUri(uri)
            ?: return@withLibraryLock Result.failure(Exception("Не удалось прочитать файл «$fileName»"))

        val result = ingestionPipeline.ingest(inputStream, uri, fileName, null)
        result.onSuccess { book ->
            // Names and titles are not identities: different books can share both.
            val existing = _recentBooks.value.find {
                it.id == book.id || it.uriString == book.uriString || it.uriString == uri.toString()
            }
            val resolvedBook = if (existing != null) {
                book.copy(
                    id = existing.id,
                    coverBase64 = existing.coverBase64 ?: book.coverBase64,
                    isFavorite = existing.isFavorite,
                    collections = existing.collections,
                    currentChapterIndex = existing.currentChapterIndex,
                    currentScrollOffset = existing.currentScrollOffset,
                    currentBlockTextOffset = existing.currentBlockTextOffset,
                    currentScrollPixelOffset = existing.currentScrollPixelOffset,
                    progressPercent = existing.progressPercent,
                    manualShelf = existing.manualShelf
                )
            } else {
                book
            }
            _currentBook.value = resolvedBook
            addOrUpdateRecentBook(resolvedBook)
            return@withLibraryLock Result.success(resolvedBook)
        }
        result
    }

    private fun getInputStreamForUri(uri: Uri, bookId: String? = null): InputStream? {
        // Always prefer the URI selected by the user over any internal copy.
        try {
            if (uri.scheme == "content") {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: SecurityException) { }
                context.contentResolver.openInputStream(uri)?.let { return it }
            } else if (uri.scheme == "file" || uri.scheme == null) {
                val file = java.io.File(uri.path ?: uri.toString())
                if (file.isFile && file.canRead()) return file.inputStream()
            }
        } catch (_: Exception) { }

        // Recovery after a device/backup migration must use a stable ID, never a filename.
        if (bookId != null) {
            val candidates = java.io.File(context.filesDir, "saved_books").listFiles()
                ?.filter { it.isFile && it.name.startsWith(bookId + "_") } ?: emptyList()
            if (candidates.size == 1) {
                try { return candidates.single().inputStream() } catch (_: Exception) { }
            }
        }
        return null
    }

    fun loadSampleBook(): Book {
        val sampleBook = Book(
            id = "sample_book",
            title = "Пример книги в Aura Reader",
            author = "Команда разработки",
            format = BookFormat.FB2,
            uriString = "sample://local",
            chapters = listOf(
                Chapter(
                    id = "ch_1",
                    title = "Глава 1. Знакомство с Material 3 Expressive",
                    content = "Добро пожаловать в читалку Aura Reader!\n\nЭто приложение создано с учётом новейших принципов дизайна от Google — Material 3 Expressive. Цвета приложения динамически адаптируются под тему вашей системы или обои на Android 12 и выше.\n\nВы можете настроить размер шрифта, межстрочный интервал, изменить гарнитуру или включить режим «Сепия» или «AMOLED» для комфортного чтения в любое время суток.",
                    order = 0
                ),
                Chapter(
                    id = "ch_2",
                    title = "Глава 2. Поддержка форматов FB2 и EPUB",
                    content = "Aura Reader полностью поддерживает книги в форматах FB2, FB2.ZIP и EPUB.\n\nЧтобы открыть свою книгу, просто нажмите на круглую кнопку со значком «+» на главном экране или нажмите на любой FB2/EPUB файл прямо в проводнике вашего смартфона — Aura Reader автоматически откроет его для чтения и сохранит ваше место в библиотеке.",
                    order = 1
                ),
                Chapter(
                    id = "ch_3",
                    title = "Глава 3. Управление чтением",
                    content = "Во время чтения коснитесь центра экрана, чтобы показать или скрыть панели управления.\n\nВ нижней панели вы найдёте кнопку настроек текста (иконка шестеренки/букв), оглавление со всеми главами и удобный ползунок для быстрого перемещения по страницам книги.\n\nПриятного чтения!",
                    order = 2
                )
            )
        )
        _currentBook.value = sampleBook
        return sampleBook
    }

    suspend fun updateReadingProgress(chapterIndex: Int, scrollOffset: Int, progressPercent: Int, textOffset: Int = 0, pixelOffset: Int = 0, expectedBookId: String? = null) = withLibraryLock {
        val current = _currentBook.value ?: return@withLibraryLock
        if (expectedBookId != null && current.id != expectedBookId) return@withLibraryLock
        val updatedManualShelf = when {
            progressPercent >= 100 && current.manualShelf == "READING" -> null
            progressPercent in 1..99 && (current.manualShelf == "FINISHED" || current.manualShelf == "UNREAD") -> null
            else -> current.manualShelf
        }
        if (current.currentChapterIndex == chapterIndex &&
            current.currentScrollOffset == scrollOffset &&
            current.progressPercent == progressPercent &&
            current.manualShelf == updatedManualShelf &&
            current.currentBlockTextOffset == textOffset && current.currentScrollPixelOffset == pixelOffset) {
            return@withLibraryLock
        }
        val updated = current.copy(
            currentChapterIndex = chapterIndex,
            currentScrollOffset = scrollOffset,
            currentBlockTextOffset = textOffset.coerceAtLeast(0),
            currentScrollPixelOffset = pixelOffset.coerceAtLeast(0),
            progressPercent = progressPercent,
            manualShelf = updatedManualShelf,
            lastReadTimestamp = System.currentTimeMillis()
        )
        _currentBook.value = updated
        addOrUpdateRecentBook(updated)
    }

    suspend fun updateBookCover(bookId: String, newCoverBase64: String?) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val updated = currentList[index].copy(coverBase64 = newCoverBase64)
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(coverBase64 = updated.coverBase64)
            }
            com.aura.reader.widget.BookAppWidgetProvider.updateAllWidgets(context, updated)
        }
    }

    private suspend fun addOrUpdateRecentBook(book: Book) {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst {
            it.id == book.id || (it.format == book.format && it.uriString == book.uriString)
        }
        if (index >= 0) {
            currentList.removeAt(index)
        }
        currentList.add(0, book)
        saveRecentBooks(currentList)
        com.aura.reader.widget.BookAppWidgetProvider.updateAllWidgets(context, book)
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            val name = cursor.getString(nameIndex)
                            if (!name.isNullOrBlank()) return name
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        val raw = uri.lastPathSegment?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        } ?: uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
        return if (raw != null && raw.length > 37 && raw[36] == '_') {
            raw.substring(37)
        } else {
            raw
        }
    }

    fun getDisplayName(uri: Uri): String? = getFileName(uri)

    fun isSupportedBookUri(uri: Uri): Boolean {
        val name = (getFileName(uri) ?: uri.lastPathSegment ?: "").lowercase()
        return name.endsWith(".fb2") ||
                name.endsWith(".fb2.zip") ||
                name.endsWith(".epub") ||
                name.endsWith(".txt") ||
                name.endsWith(".pdf") ||
                name.endsWith(".mobi") ||
                name.endsWith(".cbz")
    }

    private suspend fun removeBookInternal(bookId: String) {
        val currentList = _recentBooks.value.toMutableList()
        val bookToRemove = currentList.find { it.id == bookId }
        currentList.removeAll { it.id == bookId }
        saveRecentBooks(currentList)

        if (bookToRemove != null) {
            try {
                if (bookToRemove.uriString.isNotBlank()) {
                    val uri = Uri.parse(bookToRemove.uriString)
                    val filePath = if (uri.scheme == "file") uri.path else bookToRemove.uriString
                    if (filePath != null) {
                        val file = java.io.File(filePath)
                        if (file.exists() && file.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                            file.delete()
                        }
                    }
                }
                val localBookDir = java.io.File(context.filesDir, "saved_books")
                val files = localBookDir.listFiles() ?: emptyArray()
                for (f in files) {
                    if (f.name.startsWith(bookId) || (bookId.length >= 8 && f.name.contains(bookId.take(8)))) {
                        f.delete()
                    }
                }
                val booksDir = java.io.File(context.filesDir, "books")
                val bookFiles = booksDir.listFiles() ?: emptyArray()
                for (f in bookFiles) {
                    if (f.name.contains(bookId.takeLast(20)) || (bookId.length >= 8 && f.name.contains(bookId.take(8))) || f.name.contains(bookId)) {
                        f.delete()
                    }
                }
                val imagesDir = java.io.File(context.cacheDir, "book_images/$bookId")
                if (imagesDir.exists()) {
                    imagesDir.deleteRecursively()
                }
            } catch (e: Exception) {}
        }
    }

    suspend fun removeBook(bookId: String) = withLibraryLock {
        com.aura.reader.data.parser.PdfPageCache.closeRenderer()
        removeBookInternal(bookId)
    }

    val bookmarks = preferencesManager.bookmarks
    suspend fun addBookmark(bookmark: com.aura.reader.data.model.Bookmark) = preferencesManager.addBookmark(bookmark)
    suspend fun removeBookmark(id: String) = preferencesManager.removeBookmark(id)

    val quotes = preferencesManager.quotes
    suspend fun addQuote(quote: com.aura.reader.data.model.Quote) = preferencesManager.addQuote(quote)
    suspend fun removeQuote(id: String) = preferencesManager.removeQuote(id)

    val savedWords = preferencesManager.savedWords
    suspend fun addSavedWord(word: com.aura.reader.data.model.SavedWord) = preferencesManager.addSavedWord(word)
    suspend fun removeSavedWord(id: String) = preferencesManager.removeSavedWord(id)
    suspend fun toggleSavedWordMastered(id: String) = preferencesManager.toggleSavedWordMastered(id)

    val todayReadingMinutes = preferencesManager.todayReadingMinutes
    val readingStatsData = preferencesManager.readingStatsData
    suspend fun addReadingSeconds(seconds: Long) = preferencesManager.addReadingSeconds(seconds)

    val backupManager = com.aura.reader.data.backup.BackupManager(context, preferencesManager)

    val userCollections = preferencesManager.userCollections
    suspend fun addUserCollection(name: String) = preferencesManager.addUserCollection(name)
    suspend fun removeUserCollection(name: String) = withLibraryLock {
        preferencesManager.removeUserCollection(name)
        val currentList = _recentBooks.value.toMutableList()
        var changed = false
        for (i in currentList.indices) {
            val book = currentList[i]
            if (book.collections.contains(name)) {
                currentList[i] = book.copy(collections = book.collections.filter { it != name })
                changed = true
            }
        }
        if (changed) {
            saveRecentBooks(currentList)
            if (_currentBook.value?.collections?.contains(name) == true) {
                _currentBook.value = _currentBook.value?.let { it.copy(collections = it.collections.filter { c -> c != name }) }
            }
        }
    }

    suspend fun toggleFavorite(bookId: String) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            val updated = old.copy(isFavorite = !old.isFavorite)
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(isFavorite = updated.isFavorite)
            }
        }
    }

    suspend fun updateBookPlacement(
        bookId: String,
        collections: List<String>,
        manualShelf: String?,
        isFavorite: Boolean
    ) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            val updated = old.copy(
                collections = collections,
                manualShelf = manualShelf,
                isFavorite = isFavorite
            )
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(
                    collections = updated.collections,
                    manualShelf = updated.manualShelf,
                    isFavorite = updated.isFavorite
                )
            }
        }
    }

    suspend fun updateBookCollections(bookId: String, collections: List<String>) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            val updated = old.copy(collections = collections)
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(collections = updated.collections)
            }
        }
    }

    suspend fun setBookReadingProgress(bookId: String, progress: Int) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            val newManualShelf = when {
                progress >= 100 -> "FINISHED"
                progress == 0 -> "UNREAD"
                else -> old.manualShelf ?: "READING"
            }
            val updated = old.copy(
                progressPercent = progress.coerceIn(0, 100),
                manualShelf = newManualShelf,
                currentChapterIndex = if (progress == 0) 0 else old.currentChapterIndex,
                currentScrollOffset = if (progress == 0) 0 else old.currentScrollOffset,
                currentBlockTextOffset = if (progress == 0) 0 else old.currentBlockTextOffset,
                currentScrollPixelOffset = if (progress == 0) 0 else old.currentScrollPixelOffset,
                lastReadTimestamp = System.currentTimeMillis()
            )
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(
                    progressPercent = updated.progressPercent,
                    manualShelf = updated.manualShelf,
                    currentChapterIndex = updated.currentChapterIndex,
                    currentScrollOffset = updated.currentScrollOffset,
                    currentBlockTextOffset = updated.currentBlockTextOffset,
                    currentScrollPixelOffset = updated.currentScrollPixelOffset,
                    lastReadTimestamp = updated.lastReadTimestamp
                )
            }
        }
    }

    // --- Trash & Recycle Bin (15 days retention) ---

    suspend fun moveToTrash(bookId: String) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            currentList[index] = old.copy(deletedAtTimestamp = System.currentTimeMillis())
            saveRecentBooks(currentList)
        }
    }

    suspend fun restoreFromTrash(bookId: String) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            currentList[index] = old.copy(deletedAtTimestamp = null)
            saveRecentBooks(currentList)
        }
    }

    suspend fun deletePermanently(bookId: String) = withLibraryLock {
        removeBookInternal(bookId)
    }

    suspend fun clearTrash() = withLibraryLock {
        val trashed = _recentBooks.value.filter { it.deletedAtTimestamp != null }
        for (b in trashed) {
            removeBookInternal(b.id)
        }
    }

    suspend fun purgeExpiredTrash(maxAgeDays: Int = 15) = withLibraryLock {
        val cutoff = System.currentTimeMillis() - (maxAgeDays * 24L * 60L * 60L * 1000L)
        val expired = _recentBooks.value.filter { it.deletedAtTimestamp != null && it.deletedAtTimestamp < cutoff }
        for (b in expired) {
            removeBookInternal(b.id)
        }
    }

    // --- Batch Operations (Multi-Select) ---

    suspend fun batchMoveToTrash(bookIds: Set<String>) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val now = System.currentTimeMillis()
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                currentList[i] = currentList[i].copy(deletedAtTimestamp = now)
                changed = true
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    suspend fun batchRestoreFromTrash(bookIds: Set<String>) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                currentList[i] = currentList[i].copy(deletedAtTimestamp = null)
                changed = true
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    suspend fun batchDeletePermanently(bookIds: Set<String>) = withLibraryLock {
        for (id in bookIds) {
            removeBookInternal(id)
        }
    }

    suspend fun batchSetReadingProgress(bookIds: Set<String>, progress: Int) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val p = progress.coerceIn(0, 100)
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                val old = currentList[i]
                val newManualShelf = when {
                    p >= 100 -> "FINISHED"
                    p == 0 -> "UNREAD"
                    else -> old.manualShelf ?: "READING"
                }
                currentList[i] = old.copy(
                    progressPercent = p,
                    manualShelf = newManualShelf,
                    lastReadTimestamp = System.currentTimeMillis()
                )
                changed = true
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    suspend fun batchAddToCollection(bookIds: Set<String>, collectionName: String) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                val old = currentList[i]
                if (collectionName !in old.collections) {
                    val updated = old.copy(collections = old.collections + collectionName)
                    currentList[i] = updated
                    changed = true
                }
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    suspend fun batchSetManualShelf(bookIds: Set<String>, manualShelf: String?) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                val old = currentList[i]
                val updatedProgress = when (manualShelf) {
                    "FINISHED" -> 100
                    "UNREAD" -> 0
                    "READING" -> if (old.progressPercent == 0 || old.progressPercent >= 100) 1 else old.progressPercent
                    else -> old.progressPercent
                }
                currentList[i] = old.copy(
                    manualShelf = manualShelf,
                    progressPercent = updatedProgress,
                    lastReadTimestamp = System.currentTimeMillis()
                )
                changed = true
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    suspend fun batchSetFavorite(bookIds: Set<String>, isFavorite: Boolean) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        var changed = false
        for (i in currentList.indices) {
            if (currentList[i].id in bookIds) {
                currentList[i] = currentList[i].copy(isFavorite = isFavorite)
                changed = true
            }
        }
        if (changed) saveRecentBooks(currentList)
    }

    // --- Book Profile Metadata ---

    suspend fun updateBookMetadata(
        bookId: String,
        title: String,
        author: String,
        series: String?,
        seriesNumber: Int?,
        genres: List<String>,
        description: String?
    ) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookId }
        if (index >= 0) {
            val old = currentList[index]
            val updated = old.copy(
                title = title.trim(),
                author = author.trim(),
                series = series?.trim()?.ifBlank { null },
                seriesNumber = seriesNumber,
                genres = genres,
                description = description?.trim()?.ifBlank { null }
            )
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(
                    title = updated.title,
                    author = updated.author,
                    series = updated.series,
                    seriesNumber = updated.seriesNumber,
                    genres = updated.genres,
                    description = updated.description
                )
            }
            com.aura.reader.widget.BookAppWidgetProvider.updateAllWidgets(context, updated)
        }
    }
}
