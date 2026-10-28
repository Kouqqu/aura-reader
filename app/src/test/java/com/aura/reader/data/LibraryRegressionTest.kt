package com.aura.reader.data

import android.net.Uri
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class LibraryRegressionTest {
    private val context get() = RuntimeEnvironment.getApplication()

    private fun source(id: String, name: String, text: String): Uri {
        val uri = Uri.parse("content://test.books/" + id + "/" + name)
        shadowOf(context.contentResolver).registerInputStream(uri, ByteArrayInputStream(text.toByteArray()))
        return uri
    }

    @Test fun libraryKeepsMoreThanThirtyBooksAndSurvivesReload() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        repeat(35) { i ->
            repo.openBookFromUri(source("book" + i, "book" + i + ".txt", "Unique chapter " + i)).getOrThrow()
        }
        assertEquals(35, repo.recentBooks.value.size)
        val reloaded = BookRepository(context, prefs)
        reloaded.loadRecentBooks()
        assertEquals(35, reloaded.recentBooks.value.size)
    }

    @Test fun sameFilenameDoesNotSubstituteCachedBook() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        val first = repo.openBookFromUri(source("first", "book.txt", "First unique story")).getOrThrow()
        val second = repo.openBookFromUri(source("second", "book.txt", "Second completely different story")).getOrThrow()
        assertNotEquals(first.id, second.id)
        assertTrue(second.chapters.any { it.content.contains("Second completely different story") })
        assertEquals(2, repo.recentBooks.value.size)
    }

    @Test fun cyrillicFilenameCollisionsDoNotSubstituteBooks() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        val first = repo.openBookFromUri(source("ru1", "Кот.txt", "First Russian book")).getOrThrow()
        val second = repo.openBookFromUri(source("ru2", "Лес.txt", "Second Russian book")).getOrThrow()
        assertNotEquals(first.id, second.id)
        assertTrue(second.chapters.any { it.content.contains("Second Russian book") })
    }

    @Test fun reimportPreservesFavoriteCollectionAndProgress() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        val book = repo.openBookFromUri(source("same", "same.txt", "Book to reopen")).getOrThrow()
        repo.toggleFavorite(book.id)
        repo.updateBookCollections(book.id, listOf("My collection"))
        assertTrue(repo.currentBook.value!!.chapters.isNotEmpty())
        repo.updateReadingProgress(0, 0, 42, 120, 350)
        val reopened = repo.openBookFromUri(source("same", "same.txt", "Book to reopen")).getOrThrow()
        assertEquals(book.id, reopened.id)
        assertTrue(reopened.isFavorite)
        assertEquals(listOf("My collection"), reopened.collections)
        assertEquals(42, reopened.progressPercent)
        assertEquals(120, reopened.currentBlockTextOffset)
        assertEquals(350, reopened.currentScrollPixelOffset)
        val reloaded = BookRepository(context, prefs)
        reloaded.loadRecentBooks()
        assertEquals(120, reloaded.recentBooks.value.single().currentBlockTextOffset)
        assertTrue(repo.recentBooks.value.all { it.chapters.isEmpty() })
        assertEquals(1, repo.recentBooks.value.size)
    }

    @Test fun concurrentImportsDoNotLoseLibraryEntries() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        (0 until 12).map { i -> async {
            repo.openBookFromUri(source("parallel" + i, "parallel" + i + ".txt", "Parallel story " + i)).getOrThrow()
        } }.awaitAll()
        assertEquals(12, repo.recentBooks.value.size)
    }
    @Test fun staleProgressCannotBeWrittenIntoAnotherBook() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        val first = repo.openBookFromUri(source("old", "old.txt", "Previous book")).getOrThrow()
        val second = repo.openBookFromUri(source("active", "active.txt", "Active book")).getOrThrow()
        repo.updateReadingProgress(0, 7, 85, expectedBookId = first.id)
        assertEquals(second.id, repo.currentBook.value!!.id)
        assertEquals(0, repo.currentBook.value!!.progressPercent)
        assertEquals(0, repo.currentBook.value!!.currentScrollOffset)
    }

    @Test fun manualShelfPlacementIsPreservedAndNotOverridden() = runBlocking {
        val prefs = PreferencesManager(context)
        prefs.saveRecentBooksJson("[]")
        val repo = BookRepository(context, prefs)
        val book = repo.openBookFromUri(source("manual_shelf", "manual_shelf.txt", "Manual shelf content")).getOrThrow()
        repo.updateBookPlacement(book.id, listOf("Sci-Fi"), "UNREAD", true)

        val reloaded = BookRepository(context, prefs)
        reloaded.loadRecentBooks()
        val loadedBook = reloaded.recentBooks.value.single()
        assertEquals("UNREAD", loadedBook.manualShelf)
        assertTrue(loadedBook.isFavorite)
        assertEquals(listOf("Sci-Fi"), loadedBook.collections)

        // Re-opening the book preserves manual placement
        val reopened = reloaded.openBook(loadedBook).getOrThrow()
        assertEquals("UNREAD", reopened.manualShelf)
        assertTrue(reopened.isFavorite)
        assertEquals(listOf("Sci-Fi"), reopened.collections)
    }

}
