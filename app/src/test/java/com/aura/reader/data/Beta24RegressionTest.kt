package com.aura.reader.data

import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import com.aura.reader.data.model.OpdsBook
import com.aura.reader.data.model.estimatedPaperPages
import com.aura.reader.data.model.estimatedReadingMinutes
import com.aura.reader.data.model.withComputedVolume
import com.aura.reader.data.parser.EpubParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Beta24RegressionTest {

    @Test
    fun opdsRecommendsEpubForStudyAndTechTagsAndFb2ForFiction() {
        val textbook = OpdsBook(
            id = "1",
            title = "Алгоритмы и структуры данных",
            fb2Url = "https://example.org/1.fb2",
            epubUrl = "https://example.org/1.epub",
            categories = listOf("Программирование", "Учебная литература")
        )
        assertEquals(BookFormat.EPUB, textbook.recommendedFormat)

        val fiction = OpdsBook(
            id = "2",
            title = "Мастер и Маргарита",
            fb2Url = "https://example.org/2.fb2",
            epubUrl = "https://example.org/2.epub",
            categories = listOf("Классическая проза")
        )
        assertEquals(BookFormat.FB2, fiction.recommendedFormat)
    }

    @Test
    fun bookVolumeComputesPaperPagesAndReadingMinutes() {
        val text = "слово ".repeat(600).trim() // ~3600 chars, 600 words
        val book = Book(
            id = "vol",
            title = "Volume Test",
            format = BookFormat.TXT,
            uriString = "test://vol",
            chapters = listOf(
                Chapter(
                    id = "c1",
                    title = "Глава 1",
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text)),
                    order = 0
                )
            )
        ).withComputedVolume()

        assertEquals(600, book.wordCount)
        assertTrue(book.estimatedPaperPages() >= 1)
        assertEquals(3, book.estimatedReadingMinutes(200f))
    }

    @Test
    fun epubNestedNcxPreservesChapterDepthAndCleansTitle() {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            zos.putNextEntry(ZipEntry("META-INF/container.xml"))
            zos.write(
                """<?xml version="1.0"?>
                <container version="1.0" xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                  <rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles>
                </container>""".toByteArray()
            )
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("OEBPS/content.opf"))
            zos.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <package xmlns="http://www.idpf.org/2007/opf" version="2.0">
                  <metadata xmlns:dc="http://purl.org/dc/elements/1.1/"><dc:title>Nested Book</dc:title></metadata>
                  <manifest>
                    <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
                    <item id="c1" href="ch1.xhtml" media-type="application/xhtml+xml"/>
                    <item id="c1_1" href="ch1_1.xhtml" media-type="application/xhtml+xml"/>
                  </manifest>
                  <spine toc="ncx">
                    <itemref idref="c1"/>
                    <itemref idref="c1_1"/>
                  </spine>
                </package>""".toByteArray()
            )
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("OEBPS/toc.ncx"))
            zos.write(
                """<?xml version="1.0" encoding="utf-8"?>
                <ncx xmlns="http://www.daisy.org/z3986/2005/ncx/" version="2005-1">
                  <navMap>
                    <navPoint id="np1" playOrder="1">
                      <navLabel><text>1. Раздел</text></navLabel>
                      <content src="ch1.xhtml"/>
                      <navPoint id="np2" playOrder="2">
                        <navLabel><text>1.1 Подраздел</text></navLabel>
                        <content src="ch1_1.xhtml"/>
                      </navPoint>
                    </navPoint>
                  </navMap>
                </ncx>""".toByteArray()
            )
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("OEBPS/ch1.xhtml"))
            zos.write("<html><body><p>Текст раздела 1.</p></body></html>".toByteArray())
            zos.closeEntry()

            zos.putNextEntry(ZipEntry("OEBPS/ch1_1.xhtml"))
            zos.write("<html><body><p>Текст подраздела 1.1.</p></body></html>".toByteArray())
            zos.closeEntry()
        }

        val parsed = EpubParser.parse(ByteArrayInputStream(baos.toByteArray()), "test://nested.epub", "nested.epub")
        assertEquals(2, parsed.chapters.size)
        assertEquals("1. Раздел", parsed.chapters[0].title)
        assertEquals(0, parsed.chapters[0].depth)
        assertEquals("1.1 Подраздел", parsed.chapters[1].title)
        assertEquals(1, parsed.chapters[1].depth)
    }
}
