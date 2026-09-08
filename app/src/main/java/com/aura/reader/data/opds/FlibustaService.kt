package com.aura.reader.data.opds

import android.content.Context
import android.util.Xml
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.FlibustaBook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URLEncoder
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

object FlibustaService {
    const val DEFAULT_BASE_URL = "http://flibusta.is/opds"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36 AuraReader/1.1.5"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val YEAR_REGEX = Regex("""Год(?:\s+издания)?:\s*(\d{4})""", RegexOption.IGNORE_CASE)
    private val FORMAT_REGEX = Regex("""Формат:\s*([a-zA-Z0-9]+)""", RegexOption.IGNORE_CASE)
    private val LANG_REGEX = Regex("""Язык:\s*([a-zA-Zа-яА-Я]+)""", RegexOption.IGNORE_CASE)
    private val SIZE_REGEX = Regex("""Размер:\s*([\d\s]+(?:Kb|Mb|Gb|Кб|Мб|Гб|bytes|байт))""", RegexOption.IGNORE_CASE)
    private val DOWNLOADS_REGEX = Regex("""Скачиваний:\s*(\d+)""", RegexOption.IGNORE_CASE)
    private val META_CLEAN_REGEX = Regex("""(?:Год(?:\s+издания)?:\s*\d{4}|Формат:\s*[a-zA-Z0-9]+|Язык:\s*[a-zA-Zа-яА-Я]+|Размер:\s*[\d\s]+(?:Kb|Mb|Gb|Кб|Мб|Гб|bytes|байт)|Скачиваний:\s*\d+)\s*""", RegexOption.IGNORE_CASE)

    fun resolveUrl(baseUrl: String, href: String): String {
        val trimmed = href.trim()
        if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
            return trimmed
        }
        return try {
            val baseHttpUrl = baseUrl.toHttpUrlOrNull()
            val resolved = baseHttpUrl?.resolve(trimmed)
            resolved?.toString() ?: run {
                val baseUri = URI(baseUrl)
                baseUri.resolve(trimmed).toString()
            }
        } catch (e: Exception) {
            val root = baseUrl.substringBefore("/opds").trimEnd('/')
            val rel = trimmed.trimStart('/')
            "$root/$rel"
        }
    }

    suspend fun searchBooks(query: String, baseUrl: String = DEFAULT_BASE_URL): Result<List<FlibustaBook>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
            val searchUrl = "$cleanBase/search?searchTerm=$encodedQuery"
            val httpUrl = searchUrl.toHttpUrlOrNull()
                ?: return@withContext Result.failure(Exception("Некорректный адрес поиска"))

            val request = Request.Builder()
                .url(httpUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Пустой ответ от сервера"))
            val list = parseFeed(body.byteStream(), searchUrl)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategory(path: String, baseUrl: String = DEFAULT_BASE_URL): Result<List<FlibustaBook>> = withContext(Dispatchers.IO) {
        try {
            val cleanBase = baseUrl.trimEnd('/')
            val targetUrl = if (path.startsWith("http://", ignoreCase = true) || path.startsWith("https://", ignoreCase = true)) {
                path
            } else {
                resolveUrl(cleanBase, path)
            }

            val httpUrl = targetUrl.toHttpUrlOrNull()
                ?: return@withContext Result.failure(Exception("Некорректный адрес ссылки"))

            val request = Request.Builder()
                .url(httpUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Пустой ответ от сервера"))
            val list = parseFeed(body.byteStream(), targetUrl)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseFeed(inputStream: InputStream, feedUrl: String): List<FlibustaBook> {
        val results = mutableListOf<FlibustaBook>()
        val parser = Xml.newPullParser()
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var inEntry = false
        var currentTitle = ""
        var currentAuthor = ""
        var currentAnnotation = ""
        var currentId = ""
        var currentCoverUrl: String? = null
        var currentFb2Url: String? = null
        var currentEpubUrl: String? = null
        var currentDownloadSize: String? = null
        var currentCategoryPath: String? = null
        var inAuthor = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.lowercase(Locale.ROOT)
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "entry" -> {
                            inEntry = true
                            currentTitle = ""
                            currentAuthor = ""
                            currentAnnotation = ""
                            currentId = ""
                            currentCoverUrl = null
                            currentFb2Url = null
                            currentEpubUrl = null
                            currentDownloadSize = null
                            currentCategoryPath = null
                        }
                        "title" -> {
                            if (inEntry) {
                                currentTitle = parser.nextText().trim()
                            }
                        }
                        "author" -> {
                            if (inEntry) inAuthor = true
                        }
                        "name" -> {
                            if (inEntry && inAuthor) {
                                val name = parser.nextText().trim()
                                currentAuthor = if (currentAuthor.isEmpty()) name else "$currentAuthor, $name"
                            }
                        }
                        "id" -> {
                            if (inEntry) {
                                currentId = parser.nextText().trim()
                            }
                        }
                        "content", "summary" -> {
                            if (inEntry) {
                                val raw = parser.nextText().trim()
                                val clean = try {
                                    Jsoup.parse(raw).text()
                                } catch (e: Exception) {
                                    raw.replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ")
                                }
                                if (clean.isNotBlank()) {
                                    currentAnnotation = clean
                                }
                            }
                        }
                        "link" -> {
                            if (inEntry) {
                                val href = parser.getAttributeValue(null, "href") ?: ""
                                val rel = parser.getAttributeValue(null, "rel") ?: ""
                                val type = parser.getAttributeValue(null, "type") ?: ""
                                val length = parser.getAttributeValue(null, "length")?.toLongOrNull()

                                if (type.startsWith("image/", ignoreCase = true) ||
                                    rel.contains("image", ignoreCase = true) ||
                                    rel.contains("thumbnail", ignoreCase = true) ||
                                    rel.contains("cover", ignoreCase = true)) {
                                    if (currentCoverUrl == null || !rel.contains("thumbnail")) {
                                        currentCoverUrl = resolveUrl(feedUrl, href)
                                    }
                                }

                                if (href.contains("/fb2", ignoreCase = true) ||
                                    type.contains("fb2", ignoreCase = true)) {
                                    currentFb2Url = resolveUrl(feedUrl, href)
                                    if (length != null && length > 0) {
                                        currentDownloadSize = formatFileSize(length)
                                    }
                                }

                                if (href.contains("/epub", ignoreCase = true) ||
                                    type.contains("epub", ignoreCase = true)) {
                                    currentEpubUrl = resolveUrl(feedUrl, href)
                                    if (currentDownloadSize == null && length != null && length > 0) {
                                        currentDownloadSize = formatFileSize(length)
                                    }
                                }

                                if (type.contains("atom+xml", ignoreCase = true) ||
                                    rel.contains("subsection", ignoreCase = true) ||
                                    rel.contains("related", ignoreCase = true)) {
                                    if (href.isNotBlank()) {
                                        currentCategoryPath = resolveUrl(feedUrl, href)
                                    }
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "author" -> inAuthor = false
                        "entry" -> {
                            inEntry = false
                            val id = if (currentId.isNotBlank()) currentId else currentTitle.hashCode().toString()
                            val isCat = currentFb2Url == null && currentEpubUrl == null && currentCategoryPath != null
                            if (currentTitle.isNotBlank()) {
                                val parsedYear = YEAR_REGEX.find(currentAnnotation)?.groupValues?.get(1)
                                val parsedFormat = FORMAT_REGEX.find(currentAnnotation)?.groupValues?.get(1)
                                val parsedLang = LANG_REGEX.find(currentAnnotation)?.groupValues?.get(1)?.uppercase(Locale.ROOT)
                                val parsedSize = SIZE_REGEX.find(currentAnnotation)?.groupValues?.get(1)
                                val parsedDownloads = DOWNLOADS_REGEX.find(currentAnnotation)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                                val cleanedAnnotation = currentAnnotation.replace(META_CLEAN_REGEX, "").trim()
                                val finalDownloadSize = currentDownloadSize ?: parsedSize

                                results.add(
                                    FlibustaBook(
                                        id = id,
                                        title = currentTitle,
                                        author = currentAuthor,
                                        annotation = cleanedAnnotation,
                                        coverUrl = currentCoverUrl,
                                        fb2Url = currentFb2Url,
                                        epubUrl = currentEpubUrl,
                                        downloadSize = finalDownloadSize,
                                        isCategory = isCat,
                                        categoryPath = currentCategoryPath,
                                        year = parsedYear,
                                        language = parsedLang,
                                        formatInfo = parsedFormat,
                                        downloadsCount = parsedDownloads
                                    )
                                )
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return results
    }

    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f МБ", bytes / (1024f * 1024f))
            bytes >= 1024 -> "${bytes / 1024} КБ"
            else -> "$bytes Б"
        }
    }

    suspend fun downloadAndExtractBook(
        context: Context,
        book: FlibustaBook,
        format: BookFormat,
        downloadUrl: String,
        onProgress: (Int) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val booksDir = File(context.filesDir, "books").apply { mkdirs() }
            val cleanTitle = book.title.replace(Regex("[^a-zA-Z0-9а-яА-ЯёЁ_ -]"), "").take(50).trim()
            val safeBaseName = if (cleanTitle.isNotBlank()) cleanTitle else "book"
            val safeBookId = book.id.replace(Regex("[^a-zA-Z0-9_-]"), "_").takeLast(20)
            val ext = if (format == BookFormat.FB2) "fb2" else "epub"
            val destFile = File(booksDir, "${safeBaseName}_${safeBookId}.$ext")

            if (destFile.exists() && destFile.length() > 0) {
                onProgress(100)
                return@withContext Result.success(destFile)
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val body = response.body ?: return@withContext Result.failure(Exception("Пустой ответ при скачивании"))
            val contentLength = body.contentLength()

            val tempDownload = File(context.cacheDir, "flibusta_dl_${System.currentTimeMillis()}.tmp")
            val buffer = ByteArray(16384)
            var bytesReadTotal = 0L

            body.byteStream().use { input ->
                tempDownload.outputStream().use { output ->
                    var read = input.read(buffer)
                    while (read != -1) {
                        output.write(buffer, 0, read)
                        bytesReadTotal += read
                        if (contentLength > 0) {
                            val progress = ((bytesReadTotal * 90) / contentLength).toInt().coerceIn(0, 90)
                            onProgress(progress)
                        }
                        read = input.read(buffer)
                    }
                }
            }

            // Check if downloaded file is ZIP (PK.. header: 0x50 0x4B 0x03 0x04)
            val isZip = tempDownload.length() >= 4 && run {
                val header = ByteArray(4)
                tempDownload.inputStream().use { it.read(header) }
                header[0] == 0x50.toByte() && header[1] == 0x4B.toByte() && header[2] == 0x03.toByte() && header[3] == 0x04.toByte()
            }

            if (isZip && format == BookFormat.FB2) {
                // Auto-unpack ZIP archive on the fly to raw .fb2
                var extracted = false
                ZipInputStream(tempDownload.inputStream()).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory && (entry.name.endsWith(".fb2", ignoreCase = true) || !entry.name.contains("."))) {
                            destFile.outputStream().use { out ->
                                zis.copyTo(out)
                            }
                            extracted = true
                            break
                        }
                        entry = zis.nextEntry
                    }
                }
                tempDownload.delete()
                if (!extracted) {
                    return@withContext Result.failure(Exception("В скачанном архиве не найден файл .fb2"))
                }
            } else {
                // Raw file or epub
                if (destFile.exists()) destFile.delete()
                tempDownload.copyTo(destFile, overwrite = true)
                tempDownload.delete()
            }

            onProgress(100)
            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isConnectionError(e: Throwable): Boolean {
        return e is java.net.SocketTimeoutException ||
                e is java.net.UnknownHostException ||
                e is java.net.ConnectException ||
                e is java.net.NoRouteToHostException ||
                e is java.io.InterruptedIOException ||
                e.message?.contains("timed out", ignoreCase = true) == true ||
                e.message?.contains("failed to connect", ignoreCase = true) == true ||
                e.message?.contains("Unable to resolve host", ignoreCase = true) == true
    }
}
