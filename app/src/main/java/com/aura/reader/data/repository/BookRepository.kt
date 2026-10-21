package com.aura.reader.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.parser.EpubParser
import com.aura.reader.data.parser.Fb2Parser
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
import com.aura.reader.data.io.copyBoundedTo
import com.aura.reader.data.io.readBoundedBytes
import java.util.UUID

class BookRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
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
                            seriesNumber = if (obj.has("seriesNumber") && !obj.isNull("seriesNumber")) obj.getInt("seriesNumber") else null
                        )
                    )
                }
                _recentBooks.value = list
            } catch (e: Exception) {
                _recentBooks.value = emptyList()
            }
        }
    }

    private suspend fun saveRecentBooks(books: List<Book>) = withContext(Dispatchers.IO) {
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
            val updated = existingInMemory.copy(
                currentChapterIndex = bookToOpen.currentChapterIndex,
                currentScrollOffset = bookToOpen.currentScrollOffset,
                currentBlockTextOffset = bookToOpen.currentBlockTextOffset,
                currentScrollPixelOffset = bookToOpen.currentScrollPixelOffset,
                progressPercent = bookToOpen.progressPercent
            )
            _currentBook.value = updated
            addOrUpdateRecentBook(updated)
            return@withLibraryLock Result.success(updated)
        }
        val uri = Uri.parse(bookToOpen.uriString)
        val fileName = getFileName(uri) ?: "${bookToOpen.title}.${bookToOpen.format.name.lowercase()}"
        val inputStream = getInputStreamForUri(uri, bookToOpen.id)
            ?: return@withLibraryLock Result.failure(Exception("Файл книги «${bookToOpen.title}» не найден на устройстве"))

        val result = parseAndResolveBook(inputStream, uri, fileName, bookToOpen)
        result.onSuccess { book ->
            val resolved = book.copy(
                id = bookToOpen.id,
                title = if (bookToOpen.title.isNotBlank()) bookToOpen.title else book.title,
                author = if (bookToOpen.author.isNotBlank()) bookToOpen.author else book.author,
                series = bookToOpen.series ?: book.series,
                seriesNumber = bookToOpen.seriesNumber ?: book.seriesNumber,
                currentChapterIndex = bookToOpen.currentChapterIndex,
                currentScrollOffset = bookToOpen.currentScrollOffset,
                currentBlockTextOffset = bookToOpen.currentBlockTextOffset,
                currentScrollPixelOffset = bookToOpen.currentScrollPixelOffset,
                progressPercent = bookToOpen.progressPercent
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

        val result = parseAndResolveBook(inputStream, uri, fileName, null)
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
                    progressPercent = existing.progressPercent
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

    private fun parseAndResolveBook(
        inputStream: InputStream,
        uri: Uri,
        fileName: String,
        savedBook: Book?
    ): Result<Book> {
        try {
            val buffered = inputStream.buffered()
            buffered.mark(8)
            val header = ByteArray(4)
            val headerSize = buffered.read(header)
            buffered.reset()
            if (fileName.endsWith(".pdf", ignoreCase = true) ||
                (headerSize == 4 && header.contentEquals(byteArrayOf(37, 80, 68, 70)))) {
                return parseStagedBook(buffered, uri, fileName, savedBook, BookFormat.PDF)
            }
            if (fileName.endsWith(".epub", ignoreCase = true)) {
                return parseStagedBook(buffered, uri, fileName, savedBook, BookFormat.EPUB)
            }
            val bytes = buffered.use { it.readBoundedBytes(128L * 1024 * 1024) }
            if (bytes.isEmpty()) {
                return Result.failure(Exception("Файл книги пуст"))
            }

            val isZip = bytes.size >= 4 &&
                    bytes[0] == 0x50.toByte() &&
                    bytes[1] == 0x4B.toByte() &&
                    bytes[2] == 0x03.toByte() &&
                    bytes[3] == 0x04.toByte()

            val bookCacheKey = UUID.nameUUIDFromBytes("${uri}_${fileName}".toByteArray()).toString()
            val imagesDir = java.io.File(context.cacheDir, "book_images/$bookCacheKey").apply { mkdirs() }

            val parsedBook = when {
                fileName.endsWith(".epub", ignoreCase = true) -> {
                    EpubParser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".fb2", ignoreCase = true) || fileName.endsWith(".fb2.zip", ignoreCase = true) -> {
                    Fb2Parser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".txt", ignoreCase = true) -> {
                    com.aura.reader.data.parser.TxtParser.parse(
                        java.io.ByteArrayInputStream(bytes),
                        uri.toString(),
                        fileName
                    )
                }
                fileName.endsWith(".cbz", ignoreCase = true) -> {
                    com.aura.reader.data.parser.CbzParser.parse(
                        java.io.ByteArrayInputStream(bytes),
                        uri.toString(),
                        fileName,
                        imagesDir
                    )
                }
                fileName.endsWith(".mobi", ignoreCase = true) || (bytes.size >= 68 && bytes[60] == 'B'.code.toByte() && bytes[61] == 'O'.code.toByte() && bytes[62] == 'O'.code.toByte() && bytes[63] == 'K'.code.toByte()) -> {
                    com.aura.reader.data.parser.MobiParser.parse(
                        java.io.ByteArrayInputStream(bytes),
                        uri.toString(),
                        fileName
                    )
                }
                isZip -> {
                    var hasMetaInf = false
                    var hasFb2 = false
                    var hasTxt = false
                    var hasImages = false
                    var entryName = ""
                    try {
                        val zis = java.util.zip.ZipInputStream(java.io.ByteArrayInputStream(bytes))
                        var e = zis.nextEntry
                        while (e != null) {
                            val lower = e.name.lowercase()
                            if (lower.contains("meta-inf/container.xml")) {
                                hasMetaInf = true
                                break
                            }
                            if (lower.endsWith(".fb2")) {
                                hasFb2 = true
                                entryName = e.name
                            }
                            if (lower.endsWith(".txt")) {
                                hasTxt = true
                                entryName = e.name
                            }
                            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp") || lower.endsWith(".gif")) {
                                hasImages = true
                            }
                            e = zis.nextEntry
                        }
                    } catch (e: Exception) {}

                    when {
                        hasMetaInf -> EpubParser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        hasFb2 -> Fb2Parser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        hasTxt -> {
                            val zis = java.util.zip.ZipInputStream(java.io.ByteArrayInputStream(bytes))
                            var e = zis.nextEntry
                            var txtBytes = ByteArray(0)
                            while (e != null) {
                                if (e.name == entryName) {
                                    txtBytes = zis.readBytes()
                                    break
                                }
                                e = zis.nextEntry
                            }
                            com.aura.reader.data.parser.TxtParser.parse(
                                java.io.ByteArrayInputStream(txtBytes),
                                uri.toString(),
                                entryName
                            )
                        }
                        hasImages || fileName.endsWith(".cbz", ignoreCase = true) -> {
                            com.aura.reader.data.parser.CbzParser.parse(
                                java.io.ByteArrayInputStream(bytes),
                                uri.toString(),
                                fileName,
                                imagesDir
                            )
                        }
                        else -> throw IllegalArgumentException("Архив «$fileName» не содержит поддерживаемых книг (FB2, EPUB, TXT, CBZ).")
                    }
                }
                else -> {
                    val sampleHeader = String(bytes.take(2048).toByteArray(), Charsets.UTF_8)
                    if (sampleHeader.contains("<FictionBook", ignoreCase = true)) {
                        Fb2Parser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                    } else {
                        try {
                            com.aura.reader.data.parser.TxtParser.parse(
                                java.io.ByteArrayInputStream(bytes),
                                uri.toString(),
                                fileName
                            )
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Формат «$fileName» не поддерживается. Aura Reader поддерживает FB2, EPUB, PDF, MOBI и TXT.")
                        }
                    }
                }
            }

            val uniqueId = savedBook?.id ?: UUID.nameUUIDFromBytes("${uri}_${parsedBook.format.name}".toByteArray()).toString()
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val localBookDir = java.io.File(context.filesDir, "saved_books").apply { mkdirs() }
            val localBookFile = java.io.File(localBookDir, "${uniqueId}_$safeName")
            if (savedBook == null || !localBookFile.exists() || localBookFile.length() != bytes.size.toLong()) {
                try {
                    val pending = java.io.File(localBookDir, localBookFile.name + ".tmp")
                    try {
                        pending.writeBytes(bytes)
                        java.nio.file.Files.move(pending.toPath(), localBookFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                            java.nio.file.StandardCopyOption.ATOMIC_MOVE)
                    } finally { pending.delete() }
                } catch (e: Exception) {
                    return Result.failure(e)
                }
            }

            val persistentUriString = if (localBookFile.exists()) {
                Uri.fromFile(localBookFile).toString()
            } else {
                uri.toString()
            }

            val resolved = parsedBook.copy(
                id = uniqueId,
                uriString = persistentUriString,
                currentChapterIndex = savedBook?.currentChapterIndex ?: 0,
                currentScrollOffset = savedBook?.currentScrollOffset ?: 0,
                currentBlockTextOffset = savedBook?.currentBlockTextOffset ?: 0,
                currentScrollPixelOffset = savedBook?.currentScrollPixelOffset ?: 0,
                progressPercent = savedBook?.progressPercent ?: 0,
                isFavorite = savedBook?.isFavorite ?: false,
                collections = savedBook?.collections ?: emptyList(),
                coverBase64 = savedBook?.coverBase64 ?: parsedBook.coverBase64
            )
            return Result.success(resolved)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun parseStagedBook(
        input: InputStream, uri: Uri, fileName: String, savedBook: Book?, format: BookFormat
    ): Result<Book> {
        val staged = java.io.File.createTempFile("import_", ".book", context.filesDir)
        try {
            input.use { source -> staged.outputStream().use { source.copyBoundedTo(it, 512L * 1024 * 1024) } }
            val id = savedBook?.id ?: UUID.nameUUIDFromBytes((uri.toString() + "_" + format.name).toByteArray()).toString()
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destination = java.io.File(java.io.File(context.filesDir, "saved_books").apply { mkdirs() }, id + "_" + safeName)
            val parsed = if (format == BookFormat.PDF) {
                com.aura.reader.data.parser.PdfParser.parse(staged, destination, uri.toString(), fileName)
            } else {
                val imagesDir = java.io.File(context.cacheDir, "book_images/" + id).apply { mkdirs() }
                EpubParser.parseFile(staged, uri.toString(), fileName, imagesDir)
            }
            java.nio.file.Files.move(staged.toPath(), destination.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE)
            return Result.success(parsed.copy(
                id = id, uriString = Uri.fromFile(destination).toString(),
                currentChapterIndex = savedBook?.currentChapterIndex ?: 0,
                currentScrollOffset = savedBook?.currentScrollOffset ?: 0,
                currentBlockTextOffset = savedBook?.currentBlockTextOffset ?: 0,
                currentScrollPixelOffset = savedBook?.currentScrollPixelOffset ?: 0,
                progressPercent = savedBook?.progressPercent ?: 0,
                isFavorite = savedBook?.isFavorite ?: false,
                collections = savedBook?.collections ?: emptyList(),
                coverBase64 = savedBook?.coverBase64 ?: parsed.coverBase64
            ))
        } catch (error: Exception) {
            return Result.failure(error)
        } finally { staged.delete() }
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
        if (current.currentChapterIndex == chapterIndex &&
            current.currentScrollOffset == scrollOffset &&
            current.progressPercent == progressPercent &&
            current.currentBlockTextOffset == textOffset && current.currentScrollPixelOffset == pixelOffset) {
            return@withLibraryLock
        }
        val updated = current.copy(
            currentChapterIndex = chapterIndex,
            currentScrollOffset = scrollOffset,
            currentBlockTextOffset = textOffset.coerceAtLeast(0),
            currentScrollPixelOffset = pixelOffset.coerceAtLeast(0),
            progressPercent = progressPercent,
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

    suspend fun removeBook(bookId: String) = withLibraryLock {
        val currentList = _recentBooks.value.toMutableList()
        val bookToRemove = currentList.find { it.id == bookId }
        currentList.removeAll { it.id == bookId }
        saveRecentBooks(currentList)

        if (bookToRemove != null) {
            try {
                val localBookDir = java.io.File(context.filesDir, "saved_books")
                val files = localBookDir.listFiles() ?: emptyArray()
                for (f in files) {
                    if (f.name.startsWith(bookId)) {
                        f.delete()
                    }
                }
            } catch (e: Exception) {}
        }
    }

    val bookmarks = preferencesManager.bookmarks
    suspend fun addBookmark(bookmark: com.aura.reader.data.model.Bookmark) = preferencesManager.addBookmark(bookmark)
    suspend fun removeBookmark(id: String) = preferencesManager.removeBookmark(id)

    val quotes = preferencesManager.quotes
    suspend fun addQuote(quote: com.aura.reader.data.model.Quote) = preferencesManager.addQuote(quote)
    suspend fun removeQuote(id: String) = preferencesManager.removeQuote(id)

    val todayReadingMinutes = preferencesManager.todayReadingMinutes
    val readingStatsData = preferencesManager.readingStatsData
    suspend fun addReadingSeconds(seconds: Long) = preferencesManager.addReadingSeconds(seconds)

    val backupManager = com.aura.reader.data.backup.BackupManager(context, preferencesManager)

    val userCollections = preferencesManager.userCollections
    suspend fun addUserCollection(name: String) = preferencesManager.addUserCollection(name)
    suspend fun removeUserCollection(name: String) = preferencesManager.removeUserCollection(name)

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
            val updated = old.copy(
                progressPercent = progress.coerceIn(0, 100),
                lastReadTimestamp = System.currentTimeMillis()
            )
            currentList[index] = updated
            saveRecentBooks(currentList)
            if (_currentBook.value?.id == bookId) {
                _currentBook.value = _currentBook.value?.copy(progressPercent = updated.progressPercent, lastReadTimestamp = updated.lastReadTimestamp)
            }
        }
    }
}
