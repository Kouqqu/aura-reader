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
}
