package com.aura.reader.data.parser

import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

object TxtParser {

    private val CHAPTER_REGEX = Regex(
        "^\\s*(Глава|Часть|Раздел|Книга|Chapter|Part|Section|Book)\\s+([0-9IVXLCDM]+|[A-Za-zА-Яа-яЁё]+)(.*)?$",
        RegexOption.IGNORE_CASE
    )

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String
    ): Book {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("Файл $fileName пуст")
        }

        val text = decodeBytes(bytes)
        val cleanFileName = fileName.substringBeforeLast(".")
        val rawLines = text.lines()

        val chapters = mutableListOf<Chapter>()
        var currentChapterTitle = cleanFileName
        var currentBlocks = mutableListOf<FormattedBlock>()
        var chapterOrder = 0

        fun flushChapter() {
            if (currentBlocks.isNotEmpty()) {
                val fullContent = currentBlocks.joinToString("\n\n") { it.text }
                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        title = currentChapterTitle,
                        content = fullContent,
                        blocks = currentBlocks.toList(),
                        order = chapterOrder++
                    )
                )
                currentBlocks = mutableListOf()
            }
        }

        var paragraphBuffer = StringBuilder()

        fun flushParagraph() {
            val p = paragraphBuffer.toString().trim()
            if (p.isNotEmpty()) {
                currentBlocks.add(FormattedBlock(type = BlockType.PARAGRAPH, text = p))
            }
            paragraphBuffer = StringBuilder()
        }

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                flushParagraph()
                continue
            }

            if (trimmed.length < 80 && CHAPTER_REGEX.matches(trimmed)) {
                flushParagraph()
                flushChapter()
                currentChapterTitle = trimmed
                currentBlocks.add(FormattedBlock(type = BlockType.TITLE, text = trimmed))
                continue
            }

            if (paragraphBuffer.isNotEmpty()) {
                paragraphBuffer.append(" ")
            }
            paragraphBuffer.append(trimmed)

            if (currentBlocks.size >= 80 && paragraphBuffer.isEmpty()) {
                flushChapter()
                currentChapterTitle = "$cleanFileName — Часть ${chapterOrder + 1}"
            }
        }

        flushParagraph()
        flushChapter()

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = cleanFileName,
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text)),
                    order = 0
                )
            )
        }

        return Book(
            id = UUID.randomUUID().toString(),
            title = cleanFileName,
            author = "",
            format = BookFormat.TXT,
            uriString = uriString,
            chapters = chapters
        )
    }

    private fun decodeBytes(bytes: ByteArray): String {
        if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
            return String(bytes, 3, bytes.size - 3, StandardCharsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16BE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, StandardCharsets.UTF_16LE)
        }

        try {
            val decoder = StandardCharsets.UTF_8.newDecoder()
            val buffer = java.nio.ByteBuffer.wrap(bytes)
            return decoder.decode(buffer).toString()
        } catch (e: Exception) {}

        return try {
            val cp1251 = Charset.forName("windows-1251")
            String(bytes, cp1251)
        } catch (e: Exception) {
            String(bytes, StandardCharsets.UTF_8)
        }
    }
}
