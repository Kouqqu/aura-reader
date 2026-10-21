package com.aura.reader

import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.parser.PdfPageCache
import com.aura.reader.data.preferences.PreferencesManager
import com.aura.reader.data.repository.BookRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PdfReadingTest {
    @Test fun pdfPagesAreRenderedOnDemandAndCacheIsBounded() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val source = File(context.cacheDir, "pdf_test_" + System.nanoTime() + ".pdf")
        val document = PdfDocument()
        try {
            repeat(20) { index ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(400, 600, index + 1).create())
                page.canvas.drawText("Page " + index, 20f, 60f, android.graphics.Paint())
                document.finishPage(page)
            }
            source.outputStream().use { document.writeTo(it) }
        } finally { document.close() }
        val cache = File(context.cacheDir, "pdf_pages")
        cache.deleteRecursively()
        val repo = BookRepository(context, PreferencesManager(context))
        val book = repo.openBookFromUri(Uri.fromFile(source)).getOrThrow()
        assertEquals(BookFormat.PDF, book.format)
        assertEquals(20, book.chapters.size)
        assertNotNull(book.coverBase64)
        assertFalse(cache.exists())
        val first = PdfPageCache.render(context, book.chapters[0].blocks[0].text)
        assertTrue(first.length() > 0)
        assertEquals(1, cache.listFiles()!!.size)
        for (index in 1..15) PdfPageCache.render(context, book.chapters[index].blocks[0].text)
        assertTrue(cache.listFiles()!!.size <= 12)
        cache.deleteRecursively()
        assertTrue(PdfPageCache.render(context, book.chapters[5].blocks[0].text).length() > 0)
        repo.removeBook(book.id)
        source.delete()
        Unit
    }
}
