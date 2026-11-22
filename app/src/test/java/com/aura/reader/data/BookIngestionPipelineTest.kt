package com.aura.reader.data

import android.net.Uri
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.parser.BookIngestionPipeline
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BookIngestionPipelineTest {

    private val context get() = RuntimeEnvironment.getApplication()

    @Test
    fun ingestEmptyStreamReturnsFailure() {
        val pipeline = BookIngestionPipeline(context)
        val result = pipeline.ingest(
            inputStream = ByteArrayInputStream(ByteArray(0)),
            uri = Uri.parse("file:///empty.txt"),
            fileName = "empty.txt"
        )
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("пуст") == true)
    }

    @Test
    fun ingestTxtStreamParsesSuccessfullyAndPersists() {
        val pipeline = BookIngestionPipeline(context)
        val text = "Глава 1\nТестовый текст для проверки пайплайна."
        val result = pipeline.ingest(
            inputStream = ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)),
            uri = Uri.parse("file:///test_book.txt"),
            fileName = "test_book.txt"
        )
        assertTrue(result.isSuccess)
        val book = result.getOrThrow()
        assertEquals(BookFormat.TXT, book.format)
        assertTrue(book.chapters.isNotEmpty())
        assertTrue(book.chapters.first().blocks.any { it.text.contains("Тестовый текст") })
    }
}
