package com.aura.reader.data.parser

import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

object MobiParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String
    ): Book {
        val bytes = inputStream.readBytes()
        if (bytes.size < 78) {
            throw IllegalArgumentException("Файл $fileName слишком мал для формата MOBI")
        }

        val numRecords = ((bytes[76].toInt() and 0xFF) shl 8) or (bytes[77].toInt() and 0xFF)
        if (numRecords <= 0) {
            throw IllegalArgumentException("В файле $fileName не найдены записи")
        }

        val recordOffsets = IntArray(numRecords)
        var pos = 78
        for (i in 0 until numRecords) {
            if (pos + 4 > bytes.size) break
            val offset = ((bytes[pos].toInt() and 0xFF) shl 24) or
                    ((bytes[pos + 1].toInt() and 0xFF) shl 16) or
                    ((bytes[pos + 2].toInt() and 0xFF) shl 8) or
                    (bytes[pos + 3].toInt() and 0xFF)
            recordOffsets[i] = offset
            pos += 8
        }

        if (recordOffsets.isEmpty() || recordOffsets[0] >= bytes.size) {
            throw IllegalArgumentException("Повреждён заголовок $fileName")
        }

        val rec0Start = recordOffsets[0]
        val rec0End = if (numRecords > 1 && recordOffsets[1] in (rec0Start + 1)..bytes.size) recordOffsets[1] else bytes.size
        val rec0Size = rec0End - rec0Start
        if (rec0Size < 16) {
            throw IllegalArgumentException("Неверная нулевая запись MOBI в $fileName")
        }

        val compression = ((bytes[rec0Start].toInt() and 0xFF) shl 8) or (bytes[rec0Start + 1].toInt() and 0xFF)
        val textRecordCount = ((bytes[rec0Start + 8].toInt() and 0xFF) shl 8) or (bytes[rec0Start + 9].toInt() and 0xFF)

        var extractedTitle: String? = null
        if (rec0Size >= 92 &&
            bytes[rec0Start + 16] == 'M'.code.toByte() &&
            bytes[rec0Start + 17] == 'O'.code.toByte() &&
            bytes[rec0Start + 18] == 'B'.code.toByte() &&
            bytes[rec0Start + 19] == 'I'.code.toByte()
        ) {
            val titleOffset = ((bytes[rec0Start + 84].toInt() and 0xFF) shl 24) or
                    ((bytes[rec0Start + 85].toInt() and 0xFF) shl 16) or
                    ((bytes[rec0Start + 86].toInt() and 0xFF) shl 8) or
                    (bytes[rec0Start + 87].toInt() and 0xFF)
            val titleLength = ((bytes[rec0Start + 88].toInt() and 0xFF) shl 24) or
                    ((bytes[rec0Start + 89].toInt() and 0xFF) shl 16) or
                    ((bytes[rec0Start + 90].toInt() and 0xFF) shl 8) or
                    (bytes[rec0Start + 91].toInt() and 0xFF)

            val absTitleStart = rec0Start + titleOffset
            val absTitleEnd = absTitleStart + titleLength
            if (titleOffset > 0 && titleLength in 1..256 && absTitleEnd <= bytes.size) {
                extractedTitle = String(bytes, absTitleStart, titleLength, StandardCharsets.UTF_8).trim()
            }
        }

        val bookTitle = extractedTitle?.ifBlank { null } ?: fileName.substringBeforeLast(".")

        val decompressedText = ByteArrayOutputStream()
        val limitRecords = minOf(textRecordCount, numRecords - 1)
        for (i in 1..limitRecords) {
            val rStart = recordOffsets[i]
            val rEnd = if (i + 1 < numRecords) recordOffsets[i + 1] else bytes.size
            if (rStart >= bytes.size || rStart >= rEnd) continue
            val slice = bytes.copyOfRange(rStart, minOf(rEnd, bytes.size))

            when (compression) {
                1 -> decompressedText.write(slice)
                2 -> {
                    val decoded = decompressPalmDoc(slice)
                    decompressedText.write(decoded)
                }
                else -> {
                    decompressedText.write(slice)
                }
            }
        }

        val rawTextBytes = decompressedText.toByteArray()
        val textContent = decodeBytes(rawTextBytes)

        val cleanHtml = cleanMobiHtml(textContent)
        val chapters = buildChapters(cleanHtml, bookTitle)

        return Book(
            id = UUID.nameUUIDFromBytes("${uriString}_${fileName}".toByteArray()).toString(),
            title = bookTitle,
            author = "",
            format = BookFormat.MOBI,
            uriString = uriString,
            chapters = chapters
        )
    }

    private fun decompressPalmDoc(data: ByteArray): ByteArray {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < data.size) {
            val b = data[i++].toInt() and 0xFF
            when {
                b == 0 -> out.write(0)
                b in 1..8 -> {
                    val count = minOf(b, data.size - i)
                    if (count > 0) {
                        out.write(data, i, count)
                        i += count
                    }
                }
                b in 9..0x7F -> out.write(b)
                b in 0x80..0xBF -> {
                    if (i < data.size) {
                        val b2 = data[i++].toInt() and 0xFF
                        val distance = ((b and 0x3F) shl 8) or b2
                        val length = (distance and 0x07) + 3
                        val offset = distance shr 3
                        val buf = out.toByteArray()
                        val start = buf.size - offset
                        if (start >= 0 && offset > 0) {
                            for (k in 0 until length) {
                                out.write(buf[start + (k % offset)].toInt())
                            }
                        }
                    }
                }
                else -> {
                    out.write(' '.code)
                    out.write(b xor 0x80)
                }
            }
        }
        return out.toByteArray()
    }

    private fun decodeBytes(bytes: ByteArray): String {
        return try {
            val utf8 = String(bytes, StandardCharsets.UTF_8)
            if (!utf8.contains('�')) utf8 else throw Exception()
        } catch (e: Exception) {
            try {
                String(bytes, Charset.forName("windows-1251"))
            } catch (e2: Exception) {
                String(bytes, StandardCharsets.ISO_8859_1)
            }
        }
    }

    private fun cleanMobiHtml(html: String): String {
        return html
            .replace(Regex("<mbp:pagebreak/?>", RegexOption.IGNORE_CASE), "

<!--pagebreak-->

")
            .replace(Regex("<br\s*/?>", RegexOption.IGNORE_CASE), "
")
            .replace(Regex("</p>|</div>|</h1>|</h2>|</h3>", RegexOption.IGNORE_CASE), "

")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&quot;", """)
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&apos;", "'")
            .replace("&#160;", " ")
    }

    private fun buildChapters(text: String, defaultTitle: String): List<Chapter> {
        val parts = text.split("<!--pagebreak-->").map { it.trim() }.filter { it.isNotEmpty() }
        val rawSections = if (parts.isNotEmpty()) parts else listOf(text)

        val chapters = mutableListOf<Chapter>()
        var chapterOrder = 0

        for ((idx, sec) in rawSections.withIndex()) {
            val lines = sec.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isEmpty()) continue

            val title = if (lines.first().length <= 60 && (lines.first().startsWith("Глава", ignoreCase = true) || lines.first().startsWith("Chapter", ignoreCase = true) || lines.size > 1)) {
                lines.first()
            } else {
                if (rawSections.size > 1) "Часть ${idx + 1}" else defaultTitle
            }

            val blocks = lines.map { FormattedBlock(BlockType.PARAGRAPH, it) }
            val fullContent = blocks.joinToString("

") { it.text }

            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = fullContent,
                    blocks = blocks,
                    order = chapterOrder++
                )
            )
        }

        if (chapters.isEmpty()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = defaultTitle,
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text)),
                    order = 0
                )
            )
        }

        return chapters
    }
}
