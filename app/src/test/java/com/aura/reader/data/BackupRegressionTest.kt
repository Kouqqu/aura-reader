package com.aura.reader.data

import com.aura.reader.data.backup.BackupArchive
import com.aura.reader.data.backup.BackupManager
import com.aura.reader.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BackupRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private fun zip(vararg entries: Pair<String, String>): ByteArrayInputStream {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { out -> entries.forEach { (name, text) ->
            out.putNextEntry(ZipEntry(name)); out.write(text.toByteArray()); out.closeEntry()
        } }
        return ByteArrayInputStream(bytes.toByteArray())
    }
    private fun book(id: String) = JSONObject().put("id", id).put("title", id)
        .put("format", "TXT").put("uriString", "file:///old/device/" + id + "_book.txt")
    private fun manifest(id: String = "new") = JSONObject().put("version", 1)
        .put("recentBooksJson", JSONArray().put(book(id)).toString())
        .put("bookmarks", JSONArray().put(JSONObject().put("id", "bookmark").put("bookId", id)
            .put("chapterIndex", 0).put("scrollOffset", 0).put("chapterTitle", "Chapter").put("previewText", "Text")))
        .put("quotes", JSONArray()).put("userCollections", JSONArray().put("Imported"))

    @Test fun missingManifestCannotOverwriteExistingBook() = runBlocking {
        val dir = File(context.filesDir, "saved_books").apply { mkdirs() }
        val old = File(dir, "old_book.txt").apply { writeText("original") }
        val result = BackupManager(context, PreferencesManager(context)).importBackup(zip("books/old_book.txt" to "replacement"))
        assertTrue(result.isFailure)
        assertEquals("original", old.readText())
    }

    @Test fun invalidSettingsCannotPartiallyRestoreFilesOrMetadata() = runBlocking {
        val prefs = PreferencesManager(context)
        val original = JSONArray().put(book("existing")).toString()
        prefs.saveRecentBooksJson(original)
        val invalid = manifest().put("settings", JSONObject().put("fontSize", "invalid"))
        val result = BackupManager(context, prefs).importBackup(zip("books/new_book.txt" to "new", "manifest.json" to invalid.toString()))
        assertTrue(result.isFailure)
        assertEquals(original, prefs.recentBooksJson.first())
        assertFalse(File(context.filesDir, "saved_books/new_book.txt").exists())
    }

    @Test fun restoreMergesLibraryRebasesPathsAndDoesNotDuplicateBookmarks() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson(JSONArray().put(book("existing")).toString())
        val manager = BackupManager(context, prefs)
        repeat(2) {
            manager.importBackup(zip("manifest.json" to manifest().toString(), "books/new_book.txt" to "restored")).getOrThrow()
        }
        val books = JSONArray(prefs.recentBooksJson.first())
        assertEquals(2, books.length())
        val restored = (0 until books.length()).map { books.getJSONObject(it) }.single { it.getString("id") == "new" }
        assertEquals("restored", File(java.net.URI(restored.getString("uriString"))).readText())
        assertEquals(1, prefs.bookmarks.first().count { it.id == "bookmark" })
    }

    @Test fun failedMetadataCommitRollsBackInstalledFiles() = runBlocking {
        val dir = File(context.filesDir, "rollback_books").apply { mkdirs() }
        val original = File(dir, "new_book.txt").apply { writeText("original") }
        BackupArchive.read(zip("manifest.json" to manifest().toString(), "books/new_book.txt" to "replacement"), context.filesDir, dir).use { archive ->
            try {
                archive.install(dir) { error("Simulated metadata write failure") }
                fail("Expected failure")
            } catch (_: IllegalStateException) { }
        }
        assertEquals("original", original.readText())
        assertEquals(1, dir.listFiles()!!.size)
    }

    @Test fun pathTraversalAndDuplicateNamesAreRejected() {
        val dir = File(context.filesDir, "saved_books")
        for (bad in listOf("books/../escape.txt", "books/sub/book.txt", "books/C:escape.txt")) {
            try {
                BackupArchive.read(zip("manifest.json" to manifest().toString(), bad to "bad"), context.filesDir, dir).close()
                fail("Expected invalid path to be rejected")
            } catch (_: IllegalArgumentException) { }
        }
        try {
            BackupArchive.read(zip("manifest.json" to manifest().toString(), "books/Book.txt" to "1", "books/book.txt" to "2"), context.filesDir, dir).close()
            fail("Expected case-insensitive duplicate to be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun exportAndImportRoundTripPreservesBookContent() = runBlocking {
        val prefs = PreferencesManager(context)
        val dir = File(context.filesDir, "saved_books").apply { mkdirs() }
        File(dir, "roundtrip_book.txt").writeText("roundtrip content")
        prefs.saveRecentBooksJson(JSONArray().put(book("roundtrip")).toString())
        val manager = BackupManager(context, prefs)
        val bytes = ByteArrayOutputStream()
        manager.exportBackup(bytes).getOrThrow()
        File(dir, "roundtrip_book.txt").writeText("changed")
        manager.importBackup(ByteArrayInputStream(bytes.toByteArray())).getOrThrow()
        val restored = JSONArray(prefs.recentBooksJson.first()).getJSONObject(0)
        assertEquals("roundtrip content", File(java.net.URI(restored.getString("uriString"))).readText())
    }
}
