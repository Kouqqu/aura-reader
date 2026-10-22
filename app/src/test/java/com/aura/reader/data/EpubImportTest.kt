package com.aura.reader.data

import com.aura.reader.data.parser.EpubParser
import com.aura.reader.data.io.copyBoundedTo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class EpubImportTest {
    private fun epub(): ByteArray {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            for ((name, text) in listOf(
                "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
                "OPS/book.opf" to "<package xmlns:dc='http://purl.org/dc/elements/1.1/'><metadata><dc:title>Test EPUB</dc:title><dc:creator>Author</dc:creator></metadata><manifest><item id='chapter' href='chapter.xhtml' media-type='application/xhtml+xml'/></manifest><spine><itemref idref='chapter'/></spine></package>",
                "OPS/chapter.xhtml" to "<html><body><h1>Chapter 1</h1><p>Readable words in the test book.</p></body></html>"
            )) {
                zip.putNextEntry(ZipEntry(name)); zip.write(text.toByteArray()); zip.closeEntry()
            }
        }
        return bytes.toByteArray()
    }
    @Test fun streamedEpubPreservesTitleAuthorAndChapterText() {
        val dir = File(System.getProperty("java.io.tmpdir"), "epub_test_" + System.nanoTime()).apply { mkdirs() }
        try {
            val book = EpubParser.parse(ByteArrayInputStream(epub()), "test", "book.epub", File(dir, "images").apply { mkdirs() })
            assertEquals("Test EPUB", book.title)
            assertEquals("Author", book.author)
            assertTrue(book.chapters.any { chapter -> chapter.blocks.any { it.text.contains("Readable words") } })
            assertFalse(dir.listFiles()!!.any { it.extension == "zip" })
        } finally { dir.deleteRecursively() }
    }
    @Test fun boundedCopyRejectsOversizedInputBeforeWritingExcessBytes() {
        val output = ByteArrayOutputStream()
        try {
            ByteArrayInputStream(ByteArray(20000)).copyBoundedTo(output, 10000)
            fail("Expected size limit failure")
        } catch (_: java.io.IOException) { }
        assertTrue(output.size() <= 10000)
    }
    @Test fun epubExtractsAndResolvesInternalLinksAcrossChapters() {
        val bytes = ByteArrayOutputStream()
        ZipOutputStream(bytes).use { zip ->
            for ((name, text) in listOf(
                "META-INF/container.xml" to "<container><rootfiles><rootfile full-path='OPS/book.opf'/></rootfiles></container>",
                "OPS/book.opf" to "<package xmlns:dc='http://purl.org/dc/elements/1.1/'><metadata><dc:title>Linked Book</dc:title></metadata><manifest><item id='toc' href='toc.xhtml' media-type='application/xhtml+xml'/><item id='notes' href='notes.xhtml' media-type='application/xhtml+xml'/></manifest><spine><itemref idref='toc'/><itemref idref='notes'/></spine></package>",
                "OPS/toc.xhtml" to "<html><body><h1>Содержание</h1><p><a href='notes.xhtml'>Пометки</a></p></body></html>",
                "OPS/notes.xhtml" to "<html><body><h1>Пометки</h1><p>Текст пометок и сносок.</p></body></html>"
            )) {
                zip.putNextEntry(ZipEntry(name)); zip.write(text.toByteArray()); zip.closeEntry()
            }
        }
        val dir = File(System.getProperty("java.io.tmpdir"), "epub_link_test_" + System.nanoTime()).apply { mkdirs() }
        try {
            val book = EpubParser.parse(ByteArrayInputStream(bytes.toByteArray()), "test", "book.epub", File(dir, "images").apply { mkdirs() })
            assertEquals(2, book.chapters.size)
            val tocChapter = book.chapters[0]
            val linkBlock = tocChapter.blocks.firstOrNull { it.text.contains("Пометки") }
            assertNotNull("Block with link should exist", linkBlock)
            assertTrue("Block should have links", linkBlock!!.links.isNotEmpty())
            val link = linkBlock.links[0]
            assertEquals("nav:1:0", link.target)
            assertEquals("nav:1:0", book.footnotes["notes.xhtml"])
        } finally { dir.deleteRecursively() }
    }
}
