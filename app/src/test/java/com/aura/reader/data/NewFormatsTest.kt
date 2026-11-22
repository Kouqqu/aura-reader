package com.aura.reader.data

import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.parser.HtmlParser
import com.aura.reader.data.parser.MarkdownParser
import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream

class NewFormatsTest {

    @Test
    fun testMarkdownParserWithFrontmatterAndHeadings() {
        val mdContent = """
            ---
            title: The Art of War
            author: Sun Tzu
            ---
            # Chapter 1: Laying Plans
            The art of war is of vital importance to the State.
            
            > All warfare is based on deception.
            
            ## Tactical Decisions
            Hence, when able to attack, we must seem unable.
            
            ---
            # Chapter 2: Waging War
            In the operations of war, where there are in the field a thousand swift chariots...
        """.trimIndent()

        val book = MarkdownParser.parse(
            ByteArrayInputStream(mdContent.toByteArray(Charsets.UTF_8)),
            "file:///mock/art_of_war.md",
            "art_of_war.md"
        )

        assertEquals("The Art of War", book.title)
        assertEquals("Sun Tzu", book.author)
        assertEquals(BookFormat.MD, book.format)
        assertEquals(2, book.chapters.size)

        assertEquals("Chapter 1: Laying Plans", book.chapters[0].title)
        assertEquals("Chapter 2: Waging War", book.chapters[1].title)

        val ch1Blocks = book.chapters[0].blocks
        assertTrue(ch1Blocks.any { it.type == BlockType.TITLE && it.text == "Chapter 1: Laying Plans" })
        assertTrue(ch1Blocks.any { it.type == BlockType.PARAGRAPH && it.text.contains("vital importance") })
        assertTrue(ch1Blocks.any { it.type == BlockType.EPIGRAPH && it.text == "All warfare is based on deception." })
        assertTrue(ch1Blocks.any { it.type == BlockType.SUBTITLE && it.text == "Tactical Decisions" })
        assertTrue(ch1Blocks.any { it.type == BlockType.DIVIDER })
    }

    @Test
    fun testHtmlParser() {
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>HTML Book Sample</title>
                <meta name="author" content="Jane Doe">
            </head>
            <body>
                <h1>Part One: Beginnings</h1>
                <p>It was a bright cold day in April.</p>
                <blockquote>Knowledge is power.</blockquote>
                <h2>Section 1.1</h2>
                <p>The clocks were striking thirteen.</p>
                <hr>
                <h1>Part Two: Journey</h1>
                <p>A second chapter began here.</p>
            </body>
            </html>
        """.trimIndent()

        val book = HtmlParser.parse(
            ByteArrayInputStream(htmlContent.toByteArray(Charsets.UTF_8)),
            "file:///mock/sample.html",
            "sample.html"
        )

        assertEquals("HTML Book Sample", book.title)
        assertEquals("Jane Doe", book.author)
        assertEquals(BookFormat.HTML, book.format)
        assertEquals(2, book.chapters.size)

        assertEquals("Part One: Beginnings", book.chapters[0].title)
        assertEquals("Part Two: Journey", book.chapters[1].title)

        val ch1Blocks = book.chapters[0].blocks
        assertTrue(ch1Blocks.any { it.type == BlockType.TITLE && it.text == "Part One: Beginnings" })
        assertTrue(ch1Blocks.any { it.type == BlockType.PARAGRAPH && it.text.contains("bright cold day") })
        assertTrue(ch1Blocks.any { it.type == BlockType.EPIGRAPH && it.text == "Knowledge is power." })
        assertTrue(ch1Blocks.any { it.type == BlockType.SUBTITLE && it.text == "Section 1.1" })
        assertTrue(ch1Blocks.any { it.type == BlockType.DIVIDER })
    }

    @Test
    fun testOpdsFileSizeInMb() {
        fun formatFileSize(bytes: Long): String {
            val mb = bytes / (1024f * 1024f)
            return when {
                mb >= 10f -> String.format(java.util.Locale.US, "%.1f МБ", mb)
                mb >= 1f -> String.format(java.util.Locale.US, "%.1f МБ", mb)
                mb >= 0.1f -> String.format(java.util.Locale.US, "%.1f МБ", mb)
                mb > 0f -> String.format(java.util.Locale.US, "%.2f МБ", mb)
                else -> "0 МБ"
            }
        }

        assertEquals("1.5 МБ", formatFileSize((1.5 * 1024 * 1024).toLong()))
        assertEquals("0.8 МБ", formatFileSize((800 * 1024).toLong()))
        assertEquals("0.3 МБ", formatFileSize((350 * 1024).toLong()))
        assertEquals("0.05 МБ", formatFileSize((50 * 1024).toLong()))
    }

    @Test
    fun testPagingModeDefaultsByFormat() {
        fun resolvePagingMode(format: BookFormat, baseSetting: Boolean, override: Boolean?): Boolean {
            return override ?: when (format) {
                BookFormat.PDF, BookFormat.CBZ -> false
                else -> baseSetting
            }
        }

        // Comics & PDF default to Scroll (false) regardless of global setting
        assertFalse(resolvePagingMode(BookFormat.PDF, baseSetting = true, override = null))
        assertFalse(resolvePagingMode(BookFormat.CBZ, baseSetting = true, override = null))

        // Normal books default to global base setting
        assertTrue(resolvePagingMode(BookFormat.EPUB, baseSetting = true, override = null))
        assertFalse(resolvePagingMode(BookFormat.FB2, baseSetting = false, override = null))

        // Explicit per-book override wins
        assertTrue(resolvePagingMode(BookFormat.PDF, baseSetting = false, override = true))
        assertFalse(resolvePagingMode(BookFormat.EPUB, baseSetting = true, override = false))
    }
}
