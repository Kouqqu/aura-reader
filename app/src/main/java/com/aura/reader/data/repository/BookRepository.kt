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
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.UUID

class BookRepository(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()

    private val _recentBooks = MutableStateFlow<List<Book>>(emptyList())
    val recentBooks: StateFlow<List<Book>> = _recentBooks.asStateFlow()

    suspend fun loadRecentBooks() = withContext(Dispatchers.IO) {
        val json = preferencesManager.recentBooksJson.first()
        if (!json.isNullOrBlank()) {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<Book>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Book(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            author = obj.optString("author", ""),
                            coverBase64 = if (obj.has("coverBase64") && !obj.isNull("coverBase64")) obj.getString("coverBase64") else null,
                            format = BookFormat.valueOf(obj.getString("format")),
                            uriString = obj.getString("uriString"),
                            currentChapterIndex = obj.optInt("currentChapterIndex", 0),
                            currentScrollOffset = obj.optInt("currentScrollOffset", 0),
                            progressPercent = obj.optInt("progressPercent", 0),
                            lastReadTimestamp = obj.optLong("lastReadTimestamp", System.currentTimeMillis())
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
                put("progressPercent", b.progressPercent)
                put("lastReadTimestamp", b.lastReadTimestamp)
            }
            array.put(obj)
        }
        preferencesManager.saveRecentBooksJson(array.toString())
        _recentBooks.value = books
    }

    suspend fun openBook(bookToOpen: Book): Result<Book> = withContext(Dispatchers.IO) {
        val uri = Uri.parse(bookToOpen.uriString)
        val fileName = getFileName(uri) ?: "${bookToOpen.title}.${bookToOpen.format.name.lowercase()}"
        val inputStream = getInputStreamForUri(uri, fileName, bookToOpen.title)
            ?: return@withContext Result.failure(Exception("Файл книги «${bookToOpen.title}» не найден на устройстве"))

        val result = parseAndResolveBook(inputStream, uri, fileName, bookToOpen)
        result.onSuccess { book ->
            val resolved = book.copy(
                id = bookToOpen.id,
                title = if (bookToOpen.title.isNotBlank()) bookToOpen.title else book.title,
                author = if (bookToOpen.author.isNotBlank()) bookToOpen.author else book.author,
                currentChapterIndex = bookToOpen.currentChapterIndex,
                currentScrollOffset = bookToOpen.currentScrollOffset,
                progressPercent = bookToOpen.progressPercent
            )
            _currentBook.value = resolved
            addOrUpdateRecentBook(resolved)
            return@withContext Result.success(resolved)
        }
        result
    }

    suspend fun openBookFromUri(uri: Uri): Result<Book> = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri) ?: "Книга"
        val inputStream = getInputStreamForUri(uri, fileName, null)
            ?: return@withContext Result.failure(Exception("Не удалось прочитать файл «$fileName»"))

        val result = parseAndResolveBook(inputStream, uri, fileName, null)
        result.onSuccess { book ->
            val cleanTitle = book.title.trim()
            val existing = _recentBooks.value.find {
                it.id == book.id || it.uriString == book.uriString || it.uriString == uri.toString() ||
                (it.format == book.format && (
                    (cleanTitle.isNotBlank() && it.title.trim().equals(cleanTitle, ignoreCase = true) && fileName.isNotBlank() && it.uriString.contains(fileName)) ||
                    (fileName.isNotBlank() && it.uriString.endsWith(fileName, ignoreCase = true))
                ))
            }
            val resolvedBook = if (existing != null) {
                book.copy(
                    id = existing.id,
                    currentChapterIndex = existing.currentChapterIndex,
                    currentScrollOffset = existing.currentScrollOffset,
                    progressPercent = existing.progressPercent
                )
            } else {
                book
            }
            _currentBook.value = resolvedBook
            addOrUpdateRecentBook(resolvedBook)
            return@withContext Result.success(resolvedBook)
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
            val bytes = inputStream.use { it.readBytes() }
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
                isZip -> {
                    var hasMetaInf = false
                    var hasFb2 = false
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
                            }
                            e = zis.nextEntry
                        }
                    } catch (e: Exception) {}

                    when {
                        hasMetaInf -> EpubParser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        hasFb2 -> Fb2Parser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        else -> throw IllegalArgumentException("Файл «$fileName» не содержит книги в формате FB2 или EPUB.")
                    }
                }
                else -> {
                    val sampleHeader = String(bytes.take(2048).toByteArray(), Charsets.UTF_8)
                    if (sampleHeader.contains("<FictionBook", ignoreCase = true)) {
                        Fb2Parser.parse(java.io.ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                    } else if (fileName.endsWith(".txt", ignoreCase = true)) {
                        val content = String(bytes, Charsets.UTF_8)
                        Book(
                            id = UUID.randomUUID().toString(),
                            title = fileName.substringBeforeLast("."),
                            author = "",
                            format = BookFormat.TXT,
                            uriString = uri.toString(),
                            chapters = listOf(
                                Chapter(
                                    id = UUID.randomUUID().toString(),
                                    title = fileName,
                                    content = content,
                                    order = 0
                                )
                            )
                        )
                    } else {
                        throw IllegalArgumentException("Формат «$fileName» не поддерживается. Aura Reader предназначен для книг FB2, FB2.ZIP и EPUB.")
                    }
                }
            }

            val uniqueId = savedBook?.id ?: UUID.nameUUIDFromBytes("${uri}_${parsedBook.format.name}".toByteArray()).toString()
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val localBookDir = java.io.File(context.filesDir, "saved_books").apply { mkdirs() }
            val localBookFile = java.io.File(localBookDir, "${uniqueId}_$safeName")
            if (!localBookFile.exists() || localBookFile.length() != bytes.size.toLong()) {
                try {
                    localBookFile.writeBytes(bytes)
                } catch (e: Exception) {}
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
                progressPercent = savedBook?.progressPercent ?: 0
            )
            return Result.success(resolved)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun getInputStreamForUri(uri: Uri, fileName: String?, bookTitle: String?): InputStream? {
        // 1. Direct file:// check
        if (uri.scheme == "file" || uri.scheme == null) {
            val path = uri.path ?: uri.toString().removePrefix("file://")
            val file = java.io.File(path)
            if (file.exists() && file.canRead()) {
                try {
                    return java.io.FileInputStream(file)
                } catch (e: Exception) {}
            }
        }

        // 2. Persistent internal cache (saved_books)
        val localBookDir = java.io.File(context.filesDir, "saved_books")
        if (localBookDir.exists()) {
            val cachedFiles = localBookDir.listFiles() ?: emptyArray()
            if (!fileName.isNullOrBlank()) {
                val cleanName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val found = cachedFiles.find { it.name.endsWith(cleanName, ignoreCase = true) || it.name.contains(cleanName, ignoreCase = true) }
                if (found != null && found.canRead()) {
                    try {
                        return java.io.FileInputStream(found)
                    } catch (e: Exception) {}
                }
            }
            val uriEnd = uri.lastPathSegment
            if (!uriEnd.isNullOrBlank()) {
                val found = cachedFiles.find { it.name.endsWith(uriEnd, ignoreCase = true) }
                if (found != null && found.canRead()) {
                    try {
                        return java.io.FileInputStream(found)
                    } catch (e: Exception) {}
                }
            }
        }

        // 3. ContentResolver
        if (uri.scheme == "content") {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {}
            try {
                val stream = context.contentResolver.openInputStream(uri)
                if (stream != null) return stream
            } catch (e: Exception) {}
        }

        // 4. Device storage search (Downloads, Documents, Books)
        val candidateDirs = listOf(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOCUMENTS),
            java.io.File(android.os.Environment.getExternalStorageDirectory(), "Download"),
            java.io.File(android.os.Environment.getExternalStorageDirectory(), "Documents"),
            java.io.File(android.os.Environment.getExternalStorageDirectory(), "Books")
        )

        val targetNames = mutableListOf<String>()
        if (!fileName.isNullOrBlank()) targetNames.add(fileName)
        if (!bookTitle.isNullOrBlank()) {
            targetNames.add("$bookTitle.fb2")
            targetNames.add("$bookTitle.epub")
            targetNames.add("$bookTitle.fb2.zip")
        }

        for (dir in candidateDirs) {
            if (dir.exists() && dir.isDirectory) {
                for (target in targetNames) {
                    val direct = java.io.File(dir, target)
                    if (direct.exists() && direct.canRead()) {
                        try {
                            return java.io.FileInputStream(direct)
                        } catch (e: Exception) {}
                    }
                    try {
                        val match = dir.walkTopDown().maxDepth(2).find { it.isFile && it.name.equals(target, ignoreCase = true) }
                        if (match != null && match.canRead()) {
                            return java.io.FileInputStream(match)
                        }
                    } catch (e: Exception) {}
                }
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

    suspend fun updateReadingProgress(chapterIndex: Int, scrollOffset: Int, progressPercent: Int) {
        val current = _currentBook.value ?: return
        val updated = current.copy(
            currentChapterIndex = chapterIndex,
            currentScrollOffset = scrollOffset,
            progressPercent = progressPercent,
            lastReadTimestamp = System.currentTimeMillis()
        )
        _currentBook.value = updated
        addOrUpdateRecentBook(updated)
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
        saveRecentBooks(currentList.take(30))
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

    suspend fun removeBook(bookId: String) = withContext(Dispatchers.IO) {
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
    suspend fun addReadingSeconds(seconds: Long) = preferencesManager.addReadingSeconds(seconds)
}
