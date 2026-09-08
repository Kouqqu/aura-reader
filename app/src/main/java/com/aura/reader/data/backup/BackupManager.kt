package com.aura.reader.data.backup

import android.content.Context
import com.aura.reader.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    suspend fun exportBackup(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val zipOut = ZipOutputStream(outputStream)

            // 1. Collect all metadata
            val recentBooksJson = preferencesManager.recentBooksJson.first() ?: "[]"
            val bookmarksJson = preferencesManager.bookmarks.first()
            val quotesJson = preferencesManager.quotes.first()
            val userCollections = preferencesManager.userCollections.first()
            val readerSettings = preferencesManager.readerSettings.first()
            val statsEnabled = preferencesManager.readingStatsEnabled.first()
            val materialYou = preferencesManager.materialYouEnabled.first()
            val appLang = preferencesManager.appLanguage.first()
            val updateNotifs = preferencesManager.updateNotificationsEnabled.first()
            val flibustaUrl = preferencesManager.flibustaBaseUrl.first()

            val manifest = JSONObject().apply {
                put("version", 1)
                put("createdAt", System.currentTimeMillis())
                put("createdAtFormatted", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

                put("recentBooksJson", recentBooksJson)

                val userCollectionsArr = JSONArray()
                for (col in userCollections) userCollectionsArr.put(col)
                put("userCollections", userCollectionsArr)

                val bookmarksArr = JSONArray()
                for (bm in bookmarksJson) {
                    bookmarksArr.put(JSONObject().apply {
                        put("id", bm.id)
                        put("bookId", bm.bookId)
                        put("chapterIndex", bm.chapterIndex)
                        put("scrollOffset", bm.scrollOffset)
                        put("chapterTitle", bm.chapterTitle)
                        put("previewText", bm.previewText)
                        put("timestamp", bm.timestamp)
                    })
                }
                put("bookmarks", bookmarksArr)

                val quotesArr = JSONArray()
                for (q in quotesJson) {
                    quotesArr.put(JSONObject().apply {
                        put("id", q.id)
                        put("bookId", q.bookId)
                        put("bookTitle", q.bookTitle)
                        put("chapterIndex", q.chapterIndex)
                        put("text", q.text)
                        put("timestamp", q.timestamp)
                    })
                }
                put("quotes", quotesArr)

                put("settings", JSONObject().apply {
                    put("fontSize", readerSettings.fontSize)
                    put("lineHeight", readerSettings.lineHeightMultiplier)
                    put("themeMode", readerSettings.themeMode.name)
                    put("fontFamily", readerSettings.fontFamily.name)
                    put("keepScreenOn", readerSettings.keepScreenOn)
                    put("pagingMode", readerSettings.pagingMode)
                    put("appLanguage", appLang.code)
                    put("statsEnabled", statsEnabled)
                    put("materialYou", materialYou)
                    put("updateNotifications", updateNotifs)
                    put("flibustaUrl", flibustaUrl)
                })
            }

            // Write manifest.json
            val manifestEntry = ZipEntry("manifest.json")
            zipOut.putNextEntry(manifestEntry)
            zipOut.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
            zipOut.closeEntry()

            // 2. Archive saved books
            val savedBooksDir = File(context.filesDir, "saved_books")
            var bookFilesCount = 0
            if (savedBooksDir.exists()) {
                val files = savedBooksDir.listFiles() ?: emptyArray()
                for (file in files) {
                    if (file.isFile && file.length() > 0) {
                        try {
                            val bookEntry = ZipEntry("books/${file.name}")
                            zipOut.putNextEntry(bookEntry)
                            FileInputStream(file).use { fis ->
                                fis.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                            bookFilesCount++
                        } catch (e: Exception) {}
                    }
                }
            }

            zipOut.finish()
            zipOut.flush()

            val booksArray = try { JSONArray(recentBooksJson) } catch (e: Exception) { JSONArray() }
            val count = maxOf(booksArray.length(), bookFilesCount)
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportBackupToTempFile(): Result<Pair<File, Int>> = withContext(Dispatchers.IO) {
        try {
            val backupsDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
            val tempFile = File(backupsDir, "AuraReader_Backup_$dateStr.aurabackup")
            val outputStream = FileOutputStream(tempFile)
            val exportResult = exportBackup(outputStream)
            outputStream.close()

            if (exportResult.isSuccess) {
                Result.success(Pair(tempFile, exportResult.getOrDefault(0)))
            } else {
                Result.failure(exportResult.exceptionOrNull() ?: Exception("Ошибка экспорта"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun importBackup(inputStream: InputStream): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val zis = ZipInputStream(inputStream)
            var manifestJsonStr: String? = null
            val savedBooksDir = File(context.filesDir, "saved_books").apply { mkdirs() }
            var restoredFilesCount = 0

            var entry = zis.nextEntry
            while (entry != null) {
                val name = entry.name
                if (name == "manifest.json") {
                    manifestJsonStr = zis.readBytes().toString(Charsets.UTF_8)
                } else if (name.startsWith("books/") && !entry.isDirectory) {
                    val fileName = name.removePrefix("books/")
                    if (fileName.isNotBlank()) {
                        val targetFile = File(savedBooksDir, fileName)
                        FileOutputStream(targetFile).use { out ->
                            zis.copyTo(out)
                        }
                        restoredFilesCount++
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }

            if (manifestJsonStr.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Файл не является корректной резервной копией Aura Reader (отсутствует manifest.json)"))
            }

            val manifest = JSONObject(manifestJsonStr)

            // Restore recent books
            if (manifest.has("recentBooksJson")) {
                val recentJson = manifest.getString("recentBooksJson")
                if (recentJson.isNotBlank() && recentJson != "[]") {
                    preferencesManager.saveRecentBooksJson(recentJson)
                }
            }

            // Restore user collections
            if (manifest.has("userCollections")) {
                val collectionsArr = manifest.getJSONArray("userCollections")
                val cols = mutableListOf<String>()
                for (i in 0 until collectionsArr.length()) cols.add(collectionsArr.getString(i))
                preferencesManager.saveUserCollections(cols)
            }

            // Restore bookmarks
            if (manifest.has("bookmarks")) {
                val bmArr = manifest.getJSONArray("bookmarks")
                for (i in 0 until bmArr.length()) {
                    val o = bmArr.getJSONObject(i)
                    preferencesManager.addBookmark(
                        com.aura.reader.data.model.Bookmark(
                            id = o.getString("id"),
                            bookId = o.getString("bookId"),
                            chapterIndex = o.getInt("chapterIndex"),
                            scrollOffset = o.getInt("scrollOffset"),
                            chapterTitle = o.getString("chapterTitle"),
                            previewText = o.getString("previewText"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Restore quotes
            if (manifest.has("quotes")) {
                val qArr = manifest.getJSONArray("quotes")
                for (i in 0 until qArr.length()) {
                    val o = qArr.getJSONObject(i)
                    preferencesManager.addQuote(
                        com.aura.reader.data.model.Quote(
                            id = o.getString("id"),
                            bookId = o.getString("bookId"),
                            bookTitle = o.getString("bookTitle"),
                            chapterIndex = o.getInt("chapterIndex"),
                            text = o.getString("text"),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
            }

            // Restore settings
            if (manifest.has("settings")) {
                val s = manifest.getJSONObject("settings")
                if (s.has("fontSize")) preferencesManager.updateFontSize(s.getDouble("fontSize").toFloat())
                if (s.has("lineHeight")) preferencesManager.updateLineHeight(s.getDouble("lineHeight").toFloat())
                if (s.has("themeMode")) {
                    try {
                        preferencesManager.updateThemeMode(com.aura.reader.data.model.ReaderThemeMode.valueOf(s.getString("themeMode")))
                    } catch (e: Exception) {}
                }
                if (s.has("fontFamily")) {
                    try {
                        preferencesManager.updateFontFamily(com.aura.reader.data.model.ReaderFontFamily.valueOf(s.getString("fontFamily")))
                    } catch (e: Exception) {}
                }
                if (s.has("keepScreenOn")) preferencesManager.updateKeepScreenOn(s.getBoolean("keepScreenOn"))
                if (s.has("pagingMode")) preferencesManager.updatePagingMode(s.getBoolean("pagingMode"))
                if (s.has("statsEnabled")) preferencesManager.setReadingStatsEnabled(s.getBoolean("statsEnabled"))
                if (s.has("materialYou")) preferencesManager.setMaterialYouEnabled(s.getBoolean("materialYou"))
                if (s.has("updateNotifications")) preferencesManager.setUpdateNotificationsEnabled(s.getBoolean("updateNotifications"))
                if (s.has("flibustaUrl")) preferencesManager.setFlibustaBaseUrl(s.getString("flibustaUrl"))
                if (s.has("appLanguage")) {
                    preferencesManager.updateAppLanguage(com.aura.reader.ui.theme.AppLanguage.fromCode(s.getString("appLanguage")))
                }
            }

            val recentBooksJson = manifest.optString("recentBooksJson", "[]")
            val booksArray = try { JSONArray(recentBooksJson) } catch (e: Exception) { JSONArray() }
            val totalBooks = maxOf(booksArray.length(), restoredFilesCount)

            Result.success(totalBooks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
