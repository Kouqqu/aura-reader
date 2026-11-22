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

object MarkdownParser {

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
        val lines = text.lines()

        var title = cleanFileName
        var author = ""
        var lineIndex = 0

        // Parse YAML Frontmatter (--- ... ---)
        if (lines.isNotEmpty() && lines[0].trim() == "---") {
            lineIndex = 1
            while (lineIndex < lines.size) {
                val line = lines[lineIndex].trim()
                if (line == "---") {
                    lineIndex++
                    break
                }
                val colonIdx = line.indexOf(':')
                if (colonIdx != -1) {
                    val key = line.substring(0, colonIdx).trim().lowercase()
                    val value = line.substring(colonIdx + 1).trim().trim('"', '\'')
                    when (key) {
                        "title" -> if (value.isNotBlank()) title = value
                        "author" -> if (value.isNotBlank()) author = value
                    }
                }
                lineIndex++
            }
        }

        val chapters = mutableListOf<Chapter>()
        var currentChapterTitle = title
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

        while (lineIndex < lines.size) {
            val line = lines[lineIndex]
            val trimmed = line.trim()
            lineIndex++

            if (trimmed.isEmpty()) {
                flushParagraph()
                continue
            }

            // Headings
            if (trimmed.startsWith("# ") || trimmed.startsWith("## ")) {
                flushParagraph()
                val isH1 = trimmed.startsWith("# ")
                val headingText = if (isH1) trimmed.removePrefix("# ").trim() else trimmed.removePrefix("## ").trim()
                
                if (isH1) {
                    if (currentBlocks.isNotEmpty()) {
                        flushChapter()
                    }
                    currentChapterTitle = headingText
                }

                val blockType = if (isH1) BlockType.TITLE else BlockType.SUBTITLE
                currentBlocks.add(FormattedBlock(type = blockType, text = headingText))
                continue
            }

            if (trimmed.startsWith("### ") || trimmed.startsWith("#### ")) {
                flushParagraph()
                val subText = trimmed.replace(Regex("^#+\\s*"), "").trim()
                currentBlocks.add(FormattedBlock(type = BlockType.SUBTITLE, text = subText))
                continue
            }

            // Blockquotes
            if (trimmed.startsWith(">")) {
                flushParagraph()
                val quoteText = trimmed.removePrefix(">").trim()
                if (quoteText.isNotEmpty()) {
                    currentBlocks.add(FormattedBlock(type = BlockType.EPIGRAPH, text = quoteText))
                }
                continue
            }

            // Horizontal dividers
            if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                flushParagraph()
                currentBlocks.add(FormattedBlock(type = BlockType.DIVIDER, text = ""))
                continue
            }

            // Regular paragraph line
            if (paragraphBuffer.isNotEmpty()) {
                paragraphBuffer.append(" ")
            }
            paragraphBuffer.append(trimmed)

            // Auto-break very long single-chapter files
            if (currentBlocks.size >= 90 && paragraphBuffer.isEmpty()) {
                flushChapter()
                currentChapterTitle = "$title — Часть ${chapterOrder + 1}"
            }
        }

        flushParagraph()
        flushChapter()

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text)),
                    order = 0
                )
            )
        }

        return Book(
            id = UUID.nameUUIDFromBytes("${uriString}_MD".toByteArray()).toString(),
            title = title,
            author = author,
            coverBase64 = null,
            format = BookFormat.MD,
            uriString = uriString,
            chapters = chapters,
            progressPercent = 0
        )
    }

    private fun decodeBytes(bytes: ByteArray): String {
        val sampleSize = minOf(bytes.size, 4096)
        val sample = bytes.copyOfRange(0, sampleSize)

        return try {
            val decoder = StandardCharsets.UTF_8.newDecoder()
            decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (_: Exception) {
            val detected = detectEncoding(sample)
            String(bytes, detected)
        }
    }

    private fun detectEncoding(sample: ByteArray): Charset {
        var windows1251Matches = 0
        var utf8Patterns = 0

        var i = 0
        while (i < sample.size) {
            val b = sample[i].toInt() and 0xFF
            if (b in 0xC0..0xFF) {
                windows1251Matches++
            }
            if (b in 0xD0..0xD1 && i + 1 < sample.size) {
                val next = sample[i + 1].toInt() and 0xFF
                if (next in 0x80..0xBF) {
                    utf8Patterns++
                    i++
                }
            }
            i++
        }

        return if (utf8Patterns > 5 && utf8Patterns * 2 >= windows1251Matches) {
            StandardCharsets.UTF_8
        } else if (windows1251Matches > 10) {
            try { Charset.forName("windows-1251") } catch (_: Exception) { StandardCharsets.UTF_8 }
        } else {
            StandardCharsets.UTF_8
        }
    }
}
