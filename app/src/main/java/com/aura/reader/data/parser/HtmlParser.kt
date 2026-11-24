package com.aura.reader.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

object HtmlParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String,
        imagesDir: File? = null
    ): Book {
        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("Файл $fileName пуст")
        }

        val htmlString = decodeBytes(bytes)
        val doc = Jsoup.parse(htmlString)

        val cleanFileName = fileName.substringBeforeLast(".")
        val title = doc.title().takeIf { it.isNotBlank() }
            ?: doc.selectFirst("h1")?.text()?.takeIf { it.isNotBlank() }
            ?: cleanFileName

        val author = doc.selectFirst("meta[name=author]")?.attr("content")
            ?: doc.selectFirst(".author, .byline")?.text()
            ?: ""

        val chapters = mutableListOf<Chapter>()
        var currentChapterTitle = title
        var currentBlocks = mutableListOf<FormattedBlock>()
        var chapterOrder = 0
        var coverBase64: String? = null
        var imageCounter = 0

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

        val body = doc.body() ?: doc

        fun processElement(element: Element) {
            val tag = element.tagName().lowercase()
            when (tag) {
                "h1" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        if (currentBlocks.isNotEmpty()) {
                            flushChapter()
                        }
                        currentChapterTitle = text
                        currentBlocks.add(FormattedBlock(BlockType.TITLE, text))
                    }
                }
                "h2", "h3" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        currentBlocks.add(FormattedBlock(BlockType.SUBTITLE, text))
                    }
                }
                "blockquote" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        currentBlocks.add(FormattedBlock(BlockType.EPIGRAPH, text))
                    }
                }
                "hr" -> {
                    currentBlocks.add(FormattedBlock(BlockType.DIVIDER, ""))
                }
                "figure" -> {
                    val img = element.selectFirst("img")
                    if (img != null) processElement(img)
                    val cap = element.selectFirst("figcaption")?.text()?.trim()
                    if (!cap.isNullOrBlank() && currentBlocks.lastOrNull()?.type == BlockType.IMAGE) {
                        val last = currentBlocks.removeAt(currentBlocks.lastIndex)
                        currentBlocks.add(last.copy(subText = cap))
                    }
                }
                "img" -> {
                    val src = element.attr("src")
                    if (src.startsWith("data:image/") && imagesDir != null) {
                        try {
                            val base64Data = src.substringAfter("base64,")
                            val imageBytes = Base64.decode(base64Data, Base64.DEFAULT)
                            imageCounter++
                            val imgFile = File(imagesDir, "img_${imageCounter.toString().padStart(4, '0')}.png")
                            FileOutputStream(imgFile).use { it.write(imageBytes) }
                            currentBlocks.add(FormattedBlock(BlockType.IMAGE, imgFile.absolutePath, element.attr("alt").ifBlank { null }))

                            if (coverBase64 == null) {
                                coverBase64 = createCoverThumbnail(imageBytes)
                            }
                        } catch (_: Exception) {}
                    }
                }
                "p", "li" -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty()) {
                        currentBlocks.add(FormattedBlock(BlockType.PARAGRAPH, text))
                    }
                }
                "div", "section", "article", "ul", "ol", "main" -> {
                    for (child in element.children()) {
                        processElement(child)
                    }
                }
                else -> {
                    val text = element.text().trim()
                    if (text.isNotEmpty() && element.children().isEmpty()) {
                        currentBlocks.add(FormattedBlock(BlockType.PARAGRAPH, text))
                    } else {
                        for (child in element.children()) {
                            processElement(child)
                        }
                    }
                }
            }
        }

        for (child in body.children()) {
            processElement(child)
        }

        flushChapter()

        if (chapters.isEmpty()) {
            val text = body.text()
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = text,
                    blocks = listOf(FormattedBlock(BlockType.PARAGRAPH, text.ifBlank { "Пустой документ" })),
                    order = 0
                )
            )
        }

        return Book(
            id = UUID.nameUUIDFromBytes("${uriString}_HTML".toByteArray()).toString(),
            title = title,
            author = author,
            coverBase64 = coverBase64,
            format = BookFormat.HTML,
            uriString = uriString,
            chapters = chapters,
            progressPercent = 0
        )
    }

    private fun createCoverThumbnail(bytes: ByteArray): String? {
        return try {
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            val targetW = 220
            val scaledH = (bmp.height * (targetW.toFloat() / bmp.width)).toInt().coerceAtLeast(1)
            val thumb = Bitmap.createScaledBitmap(bmp, targetW, scaledH, true)
            val baos = ByteArrayOutputStream()
            thumb.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            if (thumb != bmp) thumb.recycle()
            bmp.recycle()
            Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
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
