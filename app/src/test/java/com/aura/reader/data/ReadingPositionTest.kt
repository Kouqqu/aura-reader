package com.aura.reader.data

import com.aura.reader.data.model.*
import org.junit.Assert.*
import org.junit.Test

class ReadingPositionTest {
    private fun paragraph(text: String, offset: Int = 0) = FormattedBlock(BlockType.PARAGRAPH, text, sourceTextOffset = offset)
    @Test fun longParagraphReopensAtCorrectChunkInsteadOfItsLastPage() {
        val pages = listOf(listOf(0 to paragraph("First", 0)), listOf(0 to paragraph("Second", 200)), listOf(0 to paragraph("Third", 400)), listOf(1 to paragraph("Next", 0)))
        assertEquals(0, ReadingPosition.pageForAnchor(pages, 0, 0))
        assertEquals(1, ReadingPosition.pageForAnchor(pages, 0, 250))
        assertEquals(2, ReadingPosition.pageForAnchor(pages, 0, 400))
        assertEquals(3, ReadingPosition.pageForAnchor(pages, 1, 0))
    }
    @Test fun reflowUsesTextAnchorRatherThanOldPageNumber() {
        val pages = listOf(listOf(0 to paragraph("First", 0)), listOf(0 to paragraph("Second", 100)), listOf(0 to paragraph("Third", 200)))
        assertEquals(2, ReadingPosition.pageForAnchor(pages, 0, 250))
    }
    @Test fun shortChapterDoesNotCountAsHalfOfLongBook() {
        val book = Book("test", "Title", format = BookFormat.TXT, uriString = "test", chapters = listOf(
            Chapter("a", "Short", "", listOf(paragraph("a".repeat(100))), 0),
            Chapter("b", "Long", "", listOf(paragraph("b".repeat(900))), 1)))
        assertEquals(10, ReadingPosition.percent(book, 1, 0))
        assertEquals(55, ReadingPosition.percent(book, 1, 0, 450))
        assertEquals(99, ReadingPosition.percent(book, 1, 0, 900))
    }
    @Test fun emptyAndInvalidPositionsAreClamped() {
        val book = Book("empty", "Title", format = BookFormat.TXT, uriString = "test")
        assertEquals(0, ReadingPosition.percent(book, -1, -1, -1))
        assertEquals(0, ReadingPosition.pageForAnchor(emptyList(), 0, 0))
    }
}
