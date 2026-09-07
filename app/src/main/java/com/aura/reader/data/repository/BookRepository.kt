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

    suspend fun openBookFromUri(uri: Uri): Result<Book> = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(uri) ?: "Книга"
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл"))

            val book = when {
                fileName.endsWith(".epub", ignoreCase = true) -> {
                    EpubParser.parse(inputStream, uri.toString(), fileName)
                }
                fileName.endsWith(".fb2", ignoreCase = true) || fileName.endsWith(".fb2.zip", ignoreCase = true) -> {
                    Fb2Parser.parse(inputStream, uri.toString(), fileName)
                }
                else -> {
                    // Fallback to plain text or check content
                    val content = inputStream.bufferedReader().use { it.readText() }
                    if (content.trimStart().startsWith("<?xml") && content.contains("<FictionBook", ignoreCase = true)) {
                        Fb2Parser.parse(content.byteInputStream(), uri.toString(), fileName)
                    } else {
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
                    }
                }
            }

            // Restore saved progress if book was opened before
            val existing = _recentBooks.value.find { it.uriString == uri.toString() || it.title == book.title }
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
            Result.success(resolvedBook)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
        val index = currentList.indexOfFirst { it.id == book.id || it.uriString == book.uriString }
        if (index >= 0) {
            currentList.removeAt(index)
        }
        currentList.add(0, book)
        saveRecentBooks(currentList.take(30))
    }

    private fun getFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return cursor.getString(nameIndex)
                    }
                }
            }
        }
        return uri.path?.let { path ->
            val cut = path.lastIndexOf('/')
            if (cut != -1) path.substring(cut + 1) else path
        }
    }
}
