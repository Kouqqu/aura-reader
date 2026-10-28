package com.aura.reader.data.backup

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderThemeMode

/** Validates a whole archive before touching the live library. */
internal class BackupArchive private constructor(
    val manifest: JSONObject,
    private val stagingDir: File,
    private val books: List<File>,
    private val installedNames: Map<String, String>
) : AutoCloseable {
    val bookCount: Int get() = maxOf(JSONArray(manifest.getString("recentBooksJson")).length(), books.size)

    suspend fun install(savedBooksDir: File, saveMetadata: suspend (JSONObject) -> Unit) {
        check(savedBooksDir.isDirectory || savedBooksDir.mkdirs()) { "Cannot create library directory" }
        val created = mutableListOf<File>()
        try {
            for (book in books) {
                val target = File(savedBooksDir, installedNames.getValue(book.name))
                if (target.exists()) {
                    require(digest(target) == digest(book)) { "Restored file conflicts with existing content" }
                } else {
                    if (!book.renameTo(target)) throw IOException("Cannot install restored book")
                    created.add(target)
                }
            }
            // Original files are never overwritten: interruption before this atomic edit
            // can leave only unreferenced copies, not a damaged existing library.
            saveMetadata(manifest)
        } catch (error: Exception) {
            created.forEach { if (!it.delete()) error.addSuppressed(IOException("Cannot remove unreferenced restored file")) }
            throw error
        }
    }

    override fun close() { stagingDir.deleteRecursively() }

    companion object {
        private const val MAX_MANIFEST_BYTES = 64L * 1024 * 1024
        private const val MAX_BOOK_BYTES = 512L * 1024 * 1024
        private const val MAX_TOTAL_BYTES = 2L * 1024 * 1024 * 1024
        private const val MAX_ENTRIES = 10000

        fun read(input: InputStream, stagingParent: File, savedBooksDir: File): BackupArchive {
            val stage = File(stagingParent, "restore_" + java.util.UUID.randomUUID()).apply {
                check(mkdirs()) { "Cannot create restore staging directory" }
            }
            try {
                var manifestText: String? = null
                val books = mutableListOf<File>()
                val seen = mutableSetOf<String>()
                var total = 0L
                var count = 0
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        require(++count <= MAX_ENTRIES) { "Too many backup entries" }
                        if (!entry.isDirectory) {
                            val name = entry.name
                            require(seen.add(name.lowercase(java.util.Locale.ROOT))) { "Duplicate backup entry" }
                            val limit = if (name == "manifest.json") MAX_MANIFEST_BYTES else MAX_BOOK_BYTES
                            val bytesLimit = minOf(limit, MAX_TOTAL_BYTES - total)
                            var written = 0L
                            val buffer = ByteArray(8192)
                            val file = if (name == "manifest.json") File(stage, "manifest.json") else {
                                require(name.startsWith("books/")) { "Unexpected backup entry" }
                                val leaf = name.removePrefix("books/")
                                require(leaf.isNotBlank() && leaf != "." && leaf != ".." &&
                                    leaf.none { it == '/' || it == '\\' || it == ':' || it == '\u0000' } &&
                                    !leaf.endsWith('.') && !leaf.endsWith(' ')) { "Invalid book filename" }
                                File(File(stage, "books").apply { mkdirs() }, leaf).also { books.add(it) }
                            }
                            file.outputStream().use { output ->
                                while (true) {
                                    val n = zip.read(buffer)
                                    if (n == -1) break
                                    written += n
                                    require(written <= bytesLimit) { "Backup exceeds size limit" }
                                    output.write(buffer, 0, n)
                                }
                            }
                            total += written
                            if (name == "manifest.json") manifestText = file.readText(Charsets.UTF_8)
                            else require(written > 0) { "Empty book in backup" }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
                require(!manifestText.isNullOrBlank()) { "Backup has no manifest.json" }
                val manifest = JSONObject(manifestText)
                validate(manifest)
                val installedNames = books.associate { book ->
                    val stem = book.name.substringBeforeLast('.', book.name).replace(Regex("\\.aura-[0-9a-f]{32}$"), "").take(180)
                    val extension = book.name.substringAfterLast('.', "").take(20)
                    book.name to (stem + ".aura-" + digest(book).take(32) + if (extension.isBlank()) "" else "." + extension)
                }
                val library = JSONArray(manifest.getString("recentBooksJson"))
                for (i in 0 until library.length()) {
                    val book = library.getJSONObject(i)
                    val id = book.getString("id")
                    val matches = books.filter { it.name.startsWith(id + "_") }
                    val originalName = try { File(java.net.URI(book.getString("uriString")).path).name } catch (_: Exception) { "" }
                    val match = matches.firstOrNull { it.name == originalName } ?: matches.singleOrNull()
                    if (match != null) {
                        book.put("uriString", File(savedBooksDir, installedNames.getValue(match.name)).toURI().toString())
                    }
                }
                manifest.put("recentBooksJson", library.toString())
                return BackupArchive(manifest, stage, books, installedNames)
            } catch (error: Exception) {
                stage.deleteRecursively()
                throw error
            }
        }

        private fun digest(file: File): String {
            val hash = java.security.MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val n = input.read(buffer)
                    if (n == -1) break
                    hash.update(buffer, 0, n)
                }
            }
            return hash.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        }

        private fun validate(manifest: JSONObject) {
            require(manifest.getInt("version") == 1) { "Unsupported backup version" }
            val library = JSONArray(manifest.getString("recentBooksJson"))
            val ids = mutableSetOf<String>()
            for (i in 0 until library.length()) {
                val book = library.getJSONObject(i)
                val id = book.getString("id")
                require(id.isNotBlank() && ids.add(id)) { "Invalid or duplicate book ID" }
                book.getString("title")
                book.getString("uriString")
                BookFormat.valueOf(book.getString("format"))
                for (key in listOf("currentChapterIndex", "currentScrollOffset")) {
                    if (book.has(key)) require(book.getInt(key) >= 0) { "Invalid reading position" }
                }
                if (book.has("collections")) strings(book.getJSONArray("collections"))
            }
            if (manifest.has("userCollections")) strings(manifest.getJSONArray("userCollections"))
            for (key in listOf("bookmarks", "quotes")) {
                val items = manifest.optJSONArray(key) ?: if (manifest.has(key)) error("Invalid " + key) else JSONArray()
                val itemIds = mutableSetOf<String>()
                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    require(item.getString("id").isNotBlank() && itemIds.add(item.getString("id")))
                    item.getString("bookId")
                    require(item.getInt("chapterIndex") >= 0)
                    if (key == "bookmarks") {
                        require(item.getInt("scrollOffset") >= 0)
                        item.getString("chapterTitle")
                        item.getString("previewText")
                    } else {
                        item.getString("bookTitle")
                        item.getString("text")
                    }
                }
            }
            if (manifest.has("settings")) {
                val settings = manifest.getJSONObject("settings")
                for ((key, range) in listOf("fontSize" to 8.0..96.0, "lineHeight" to 0.5..4.0)) {
                    if (settings.has(key)) require(settings.getDouble(key) in range) { "Invalid " + key }
                }
                if (settings.has("themeMode")) ReaderThemeMode.valueOf(settings.getString("themeMode"))
                if (settings.has("fontFamily")) ReaderFontFamily.valueOf(settings.getString("fontFamily"))
                for (key in listOf("keepScreenOn", "pagingMode", "statsEnabled", "materialYou", "updateNotifications")) {
                    if (settings.has(key)) settings.getBoolean(key)
                }
                for (key in listOf("appLanguage", "catalogUrl")) if (settings.has(key)) settings.getString(key)
            }
        }

        private fun strings(array: JSONArray) {
            for (i in 0 until array.length()) array.getString(i)
        }
    }
}
