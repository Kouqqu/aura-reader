package com.aura.reader.data.parser

import android.content.Context
import android.net.Uri
import com.aura.reader.data.io.copyBoundedTo
import com.aura.reader.data.io.readBoundedBytes
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import java.util.zip.ZipInputStream

/**
 * Deep ingestion pipeline for book files of all supported formats.
 *
 * Encapsulates:
 * 1. Stream sniffing and MIME magic byte analysis (EPUB, FB2, TXT, CBZ, MOBI, PDF).
 * 2. Unzipping and format detection within compressed container files (.zip, .fb2.zip, .cbz).
 * 3. Parser routing to specialized streaming and DOM parsers.
 * 4. Image resource caching under isolated cache namespaces.
 * 5. Atomic persistence to local `saved_books/` storage with tmp-file staging.
 * 6. Reconciliation with existing book reading progress, bookmarks, and metadata.
 */
class BookIngestionPipeline(
    private val context: Context
) {

    fun ingest(
        inputStream: InputStream,
        uri: Uri,
        fileName: String,
        savedBook: Book? = null
    ): Result<Book> {
        return try {
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
            val imagesDir = File(context.cacheDir, "book_images/$bookCacheKey").apply { mkdirs() }

            val parsedBook = when {
                fileName.endsWith(".epub", ignoreCase = true) -> {
                    EpubParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".fb2", ignoreCase = true) || fileName.endsWith(".fb2.zip", ignoreCase = true) || fileName.endsWith(".fbz", ignoreCase = true) -> {
                    Fb2Parser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".md", ignoreCase = true) || fileName.endsWith(".markdown", ignoreCase = true) -> {
                    MarkdownParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName)
                }
                fileName.endsWith(".html", ignoreCase = true) || fileName.endsWith(".htm", ignoreCase = true) -> {
                    HtmlParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".cbt", ignoreCase = true) -> {
                    CbtParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".txt", ignoreCase = true) -> {
                    TxtParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName)
                }
                fileName.endsWith(".cbz", ignoreCase = true) || fileName.endsWith(".cbr", ignoreCase = true) -> {
                    CbzParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                }
                fileName.endsWith(".mobi", ignoreCase = true) || (bytes.size >= 68 && bytes[60] == 'B'.code.toByte() && bytes[61] == 'O'.code.toByte() && bytes[62] == 'O'.code.toByte() && bytes[63] == 'K'.code.toByte()) -> {
                    MobiParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName)
                }
                isZip -> {
                    var hasMetaInf = false
                    var hasFb2 = false
                    var hasTxt = false
                    var hasHtml = false
                    var hasMd = false
                    var hasImages = false
                    var entryName = ""
                    try {
                        val zis = ZipInputStream(ByteArrayInputStream(bytes))
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
                            if (lower.endsWith(".html") || lower.endsWith(".htm")) {
                                hasHtml = true
                                entryName = e.name
                            }
                            if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
                                hasMd = true
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
                    } catch (_: Exception) {}

                    when {
                        hasMetaInf -> EpubParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        hasFb2 -> Fb2Parser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        hasHtml -> {
                            val zis = ZipInputStream(ByteArrayInputStream(bytes))
                            var e = zis.nextEntry
                            var htmlBytes = ByteArray(0)
                            while (e != null) {
                                if (e.name == entryName) {
                                    htmlBytes = zis.readBytes()
                                    break
                                }
                                e = zis.nextEntry
                            }
                            HtmlParser.parse(ByteArrayInputStream(htmlBytes), uri.toString(), entryName, imagesDir)
                        }
                        hasMd -> {
                            val zis = ZipInputStream(ByteArrayInputStream(bytes))
                            var e = zis.nextEntry
                            var mdBytes = ByteArray(0)
                            while (e != null) {
                                if (e.name == entryName) {
                                    mdBytes = zis.readBytes()
                                    break
                                }
                                e = zis.nextEntry
                            }
                            MarkdownParser.parse(ByteArrayInputStream(mdBytes), uri.toString(), entryName)
                        }
                        hasTxt -> {
                            val zis = ZipInputStream(ByteArrayInputStream(bytes))
                            var e = zis.nextEntry
                            var txtBytes = ByteArray(0)
                            while (e != null) {
                                if (e.name == entryName) {
                                    txtBytes = zis.readBytes()
                                    break
                                }
                                e = zis.nextEntry
                            }
                            TxtParser.parse(ByteArrayInputStream(txtBytes), uri.toString(), entryName)
                        }
                        hasImages || fileName.endsWith(".cbz", ignoreCase = true) || fileName.endsWith(".cbr", ignoreCase = true) -> {
                            CbzParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                        }
                        else -> throw IllegalArgumentException("Архив «$fileName» не содержит поддерживаемых книг (FB2, EPUB, TXT, CBZ, HTML, MD).")
                    }
                }
                else -> {
                    val sampleHeader = String(bytes.take(2048).toByteArray(), Charsets.UTF_8)
                    if (sampleHeader.contains("<FictionBook", ignoreCase = true)) {
                        Fb2Parser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                    } else if (sampleHeader.contains("<html", ignoreCase = true) || sampleHeader.contains("<!doctype html", ignoreCase = true)) {
                        HtmlParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName, imagesDir)
                    } else {
                        try {
                            TxtParser.parse(ByteArrayInputStream(bytes), uri.toString(), fileName)
                        } catch (e: Exception) {
                            throw IllegalArgumentException("Формат «$fileName» не поддерживается. Aura Reader поддерживает FB2, EPUB, PDF, MOBI и TXT.")
                        }
                    }
                }
            }

            val uniqueId = savedBook?.id ?: UUID.nameUUIDFromBytes("${uri}_${parsedBook.format.name}".toByteArray()).toString()
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val localBookDir = File(context.filesDir, "saved_books").apply { mkdirs() }
            val localBookFile = File(localBookDir, "${uniqueId}_$safeName")
            if (savedBook == null || !localBookFile.exists() || localBookFile.length() != bytes.size.toLong()) {
                try {
                    val pending = File(localBookDir, localBookFile.name + ".tmp")
                    try {
                        pending.writeBytes(bytes)
                        Files.move(
                            pending.toPath(),
                            localBookFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING,
                            StandardCopyOption.ATOMIC_MOVE
                        )
                    } finally {
                        pending.delete()
                    }
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
                manualShelf = savedBook?.manualShelf,
                isFavorite = savedBook?.isFavorite ?: false,
                collections = savedBook?.collections ?: emptyList(),
                coverBase64 = savedBook?.coverBase64 ?: parsedBook.coverBase64,
                fileSizeBytes = if (bytes.isNotEmpty()) bytes.size.toLong() else (savedBook?.fileSizeBytes ?: 0L),
                addedTimestamp = savedBook?.addedTimestamp ?: System.currentTimeMillis(),
                firstOpenedTimestamp = savedBook?.firstOpenedTimestamp,
                description = savedBook?.description ?: parsedBook.description,
                genres = if (savedBook?.genres?.isNotEmpty() == true) savedBook.genres else parsedBook.genres,
                deletedAtTimestamp = savedBook?.deletedAtTimestamp
            )
            Result.success(resolved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStagedBook(
        input: InputStream,
        uri: Uri,
        fileName: String,
        savedBook: Book?,
        format: BookFormat
    ): Result<Book> {
        val staged = File.createTempFile("import_", ".book", context.filesDir)
        try {
            input.use { source -> staged.outputStream().use { source.copyBoundedTo(it, 512L * 1024 * 1024) } }
            val id = savedBook?.id ?: UUID.nameUUIDFromBytes((uri.toString() + "_" + format.name).toByteArray()).toString()
            val safeName = fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val destination = File(File(context.filesDir, "saved_books").apply { mkdirs() }, "${id}_$safeName")
            val parsed = if (format == BookFormat.PDF) {
                PdfParser.parse(staged, destination, uri.toString(), fileName)
            } else {
                val imagesDir = File(context.cacheDir, "book_images/$id").apply { mkdirs() }
                EpubParser.parseFile(staged, uri.toString(), fileName, imagesDir)
            }
            try {
                Files.move(
                    staged.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                )
            } catch (e: java.nio.file.AtomicMoveNotSupportedException) {
                Files.move(
                    staged.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            return Result.success(
                parsed.copy(
                    id = id,
                    uriString = Uri.fromFile(destination).toString(),
                    currentChapterIndex = savedBook?.currentChapterIndex ?: 0,
                    currentScrollOffset = savedBook?.currentScrollOffset ?: 0,
                    currentBlockTextOffset = savedBook?.currentBlockTextOffset ?: 0,
                    currentScrollPixelOffset = savedBook?.currentScrollPixelOffset ?: 0,
                    progressPercent = savedBook?.progressPercent ?: 0,
                    manualShelf = savedBook?.manualShelf,
                    isFavorite = savedBook?.isFavorite ?: false,
                    collections = savedBook?.collections ?: emptyList(),
                    coverBase64 = savedBook?.coverBase64 ?: parsed.coverBase64,
                    fileSizeBytes = destination.length(),
                    addedTimestamp = savedBook?.addedTimestamp ?: System.currentTimeMillis(),
                    firstOpenedTimestamp = savedBook?.firstOpenedTimestamp,
                    description = savedBook?.description ?: parsed.description,
                    genres = if (savedBook?.genres?.isNotEmpty() == true) savedBook.genres else parsed.genres,
                    deletedAtTimestamp = savedBook?.deletedAtTimestamp
                )
            )
        } catch (error: Exception) {
            return Result.failure(error)
        } finally {
            staged.delete()
        }
    }
}
