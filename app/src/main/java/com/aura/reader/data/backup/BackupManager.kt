package com.aura.reader.data.backup

import android.content.Context
import com.aura.reader.data.preferences.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.withLock
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
import java.util.zip.ZipOutputStream

class BackupManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {

    suspend fun exportBackup(outputStream: OutputStream): Result<Int> = withContext(Dispatchers.IO) {
        preferencesManager.libraryMutex.withLock {
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
                val catalogUrl = preferencesManager.catalogBaseUrl.first()

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
                            put("color", q.color)
                        })
                    }
                    put("quotes", quotesArr)

                    put("settings", JSONObject().apply {
                        put("fontSize", readerSettings.fontSizeSp)
                        put("lineHeight", readerSettings.lineHeightMultiplier)
                        put("themeMode", readerSettings.themeMode.name)
                        put("fontFamily", readerSettings.fontFamily.name)
                        put("keepScreenOn", readerSettings.keepScreenOn)
                        put("pagingMode", readerSettings.pagingMode)
                        put("appLanguage", appLang.code)
                        put("statsEnabled", statsEnabled)
                        put("materialYou", materialYou)
                        put("updateNotifications", updateNotifs)
                        put("catalogUrl", catalogUrl)
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
                            val bookEntry = ZipEntry("books/${file.name}")
                            zipOut.putNextEntry(bookEntry)
                            FileInputStream(file).use { fis ->
                                fis.copyTo(zipOut)
                            }
                            zipOut.closeEntry()
                            bookFilesCount++
                        }
                    }
                }

                zipOut.finish()
                zipOut.flush()
                try { outputStream.flush() } catch (e: Exception) {}

                val booksArray = try { JSONArray(recentBooksJson) } catch (e: Exception) { JSONArray() }
                val count = maxOf(booksArray.length(), bookFilesCount)
                Result.success(count)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun exportBackupToTempFile(): Result<Pair<File, Int>> = withContext(Dispatchers.IO) {
        try {
            val backupsDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
            val tempFile = File(backupsDir, "AuraReader_Backup_$dateStr.aurabackup")
            val exportResult = FileOutputStream(tempFile).use { outputStream ->
                exportBackup(outputStream)
            }

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
            preferencesManager.libraryMutex.withLock {
                val savedBooksDir = File(context.filesDir, "saved_books")
                BackupArchive.read(inputStream, context.filesDir, savedBooksDir).use { archive ->
                    // Do not allow cancellation between installing files and committing metadata.
                    withContext(kotlinx.coroutines.NonCancellable) {
                        archive.install(savedBooksDir, preferencesManager::restoreBackupMetadata)
                    }
                    Result.success(archive.bookCount)
                }
            }
        } catch (error: kotlinx.coroutines.CancellationException) {
            throw error
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
