package com.aura.reader.data.parser

import android.util.Xml
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipInputStream

object Fb2Parser {

    fun parse(inputStream: InputStream, uriString: String, fileName: String): Book {
        val stream = if (fileName.endsWith(".zip", ignoreCase = true)) {
            val zis = ZipInputStream(inputStream)
            var entry = zis.nextEntry
            while (entry != null && !entry.name.endsWith(".fb2", ignoreCase = true)) {
                entry = zis.nextEntry
            }
            zis
        } else {
            inputStream
        }

        var title = fileName.removeSuffix(".fb2").removeSuffix(".zip")
        var author = ""
        var coverId: String? = null
        val binaries = mutableMapOf<String, String>()
        val chapters = mutableListOf<Chapter>()

        val parser = Xml.newPullParser()
        parser.setInput(stream, null)

        var eventType = parser.eventType
        var inTitleInfo = false
        var inCoverpage = false
        var inBody = false
        var currentBinaryId: String? = null
        val binaryContent = StringBuilder()

        // Context state
        var inTitle = false
        var inSubtitle = false
        var inEpigraph = false
        var inTextAuthor = false
        var inPoem = false
        var inParagraph = false

        var sectionDepth = 0
        val currentBlocks = mutableListOf<FormattedBlock>()
        var currentSectionTitleLines = mutableListOf<String>()
        val currentText = StringBuilder()
        val currentEpigraphLines = mutableListOf<String>()
        var currentEpigraphAuthor = ""
        val currentPoemLines = mutableListOf<String>()

        fun flushChapter() {
            if (currentBlocks.isNotEmpty()) {
                val fullTitle = currentSectionTitleLines
                    .filter { it.isNotBlank() }
                    .joinToString(": ")

                val resolvedTitle = if (fullTitle.isNotBlank()) {
                    fullTitle
                } else {
                    // Try to find a TITLE block
                    val titleBlock = currentBlocks.firstOrNull { it.type == BlockType.TITLE }
                    if (titleBlock != null) {
                        titleBlock.text
                    } else {
                        val firstP = currentBlocks.firstOrNull { it.type == BlockType.PARAGRAPH }?.text ?: ""
                        if (isLikelyHeading(firstP)) {
                            firstP
                        } else {
                            "Глава ${chapters.size + 1}"
                        }
                    }
                }

                // Build plain text fallback for backward compatibility
                val plainText = currentBlocks.joinToString("\n\n") { block ->
                    when (block.type) {
                        BlockType.EPIGRAPH -> if (block.subText != null) "${block.text}\n— ${block.subText}" else block.text
                        else -> block.text
                    }
                }

                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        title = resolvedTitle,
                        content = plainText,
                        blocks = ArrayList(currentBlocks),
                        order = chapters.size
                    )
                )

                currentBlocks.clear()
                currentSectionTitleLines.clear()
            }
        }

        while (eventType != XmlPullParser.END_DOCUMENT) {
            val tagName = parser.name?.lowercase() ?: ""

            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (tagName) {
                        "title-info" -> inTitleInfo = true
                        "book-title" -> {
                            if (inTitleInfo) {
                                val t = parser.nextText()
                                if (t.isNotBlank()) title = t.trim()
                            }
                        }
                        "author" -> {
                            if (inTitleInfo) {
                                val authorBuilder = StringBuilder()
                                while (parser.next() != XmlPullParser.END_TAG || parser.name?.lowercase() != "author") {
                                    if (parser.eventType == XmlPullParser.START_TAG) {
                                        val aTag = parser.name?.lowercase()
                                        if (aTag in listOf("first-name", "middle-name", "last-name", "nickname")) {
                                            val part = parser.nextText().trim()
                                            if (part.isNotEmpty()) authorBuilder.append(part).append(" ")
                                        }
                                    }
                                }
                                val parsedAuthor = authorBuilder.toString().trim()
                                if (parsedAuthor.isNotEmpty()) author = parsedAuthor
                            }
                        }
                        "coverpage" -> inCoverpage = true
                        "image" -> {
                            if (inCoverpage) {
                                val href = parser.getAttributeValue(null, "href")
                                    ?: parser.getAttributeValue("http://www.w3.org/1999/xlink", "href")
                                if (href != null) {
                                    coverId = href.removePrefix("#")
                                }
                            }
                        }
                        "body" -> inBody = true
                        "section" -> {
                            if (inBody) {
                                flushChapter()
                                sectionDepth++
                            }
                        }
                        "title" -> {
                            if (inBody) {
                                inTitle = true
                            }
                        }
                        "subtitle" -> inSubtitle = true
                        "epigraph", "cite" -> {
                            inEpigraph = true
                            currentEpigraphLines.clear()
                            currentEpigraphAuthor = ""
                        }
                        "text-author" -> {
                            if (inEpigraph) inTextAuthor = true
                            currentText.setLength(0)
                        }
                        "poem" -> {
                            inPoem = true
                            currentPoemLines.clear()
                        }
                        "p", "v" -> {
                            inParagraph = true
                            currentText.setLength(0)
                        }
                        "empty-line" -> {
                            if (!inTitle && !inEpigraph) {
                                currentBlocks.add(FormattedBlock(BlockType.DIVIDER, ""))
                            }
                        }
                        "binary" -> {
                            currentBinaryId = parser.getAttributeValue(null, "id")
                            binaryContent.setLength(0)
                        }
                    }
                }

                XmlPullParser.TEXT -> {
                    val text = parser.text
                    val binId = currentBinaryId
                    if (binId != null) {
                        binaryContent.append(text)
                    } else if (text != null && text.isNotBlank()) {
                        currentText.append(text)
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "title-info" -> inTitleInfo = false
                        "coverpage" -> inCoverpage = false
                        "title" -> {
                            inTitle = false
                        }
                        "subtitle" -> inSubtitle = false
                        "text-author" -> {
                            if (inEpigraph) {
                                currentEpigraphAuthor = currentText.toString().trim()
                                inTextAuthor = false
                            }
                            currentText.setLength(0)
                        }
                        "epigraph", "cite" -> {
                            inEpigraph = false
                            val quoteText = currentEpigraphLines.joinToString("\n")
                            if (quoteText.isNotBlank()) {
                                currentBlocks.add(
                                    FormattedBlock(
                                        type = BlockType.EPIGRAPH,
                                        text = quoteText,
                                        subText = currentEpigraphAuthor.ifBlank { null }
                                    )
                                )
                            }
                            currentEpigraphLines.clear()
                            currentEpigraphAuthor = ""
                        }
                        "poem" -> {
                            inPoem = false
                            if (currentPoemLines.isNotEmpty()) {
                                currentBlocks.add(
                                    FormattedBlock(
                                        type = BlockType.VERSE,
                                        text = currentPoemLines.joinToString("\n")
                                    )
                                )
                                currentPoemLines.clear()
                            }
                        }
                        "p", "v" -> {
                            inParagraph = false
                            val paragraphText = currentText.toString().trim()
                            if (paragraphText.isNotEmpty()) {
                                when {
                                    inTitle -> {
                                        currentSectionTitleLines.add(paragraphText)
                                        currentBlocks.add(FormattedBlock(BlockType.TITLE, paragraphText))
                                    }
                                    inSubtitle -> {
                                        currentBlocks.add(FormattedBlock(BlockType.SUBTITLE, paragraphText))
                                    }
                                    inEpigraph -> {
                                        if (!inTextAuthor) {
                                            currentEpigraphLines.add(paragraphText)
                                        }
                                    }
                                    inPoem -> {
                                        currentPoemLines.add(paragraphText)
                                    }
                                    else -> {
                                        currentBlocks.add(FormattedBlock(BlockType.PARAGRAPH, paragraphText))
                                    }
                                }
                            }
                            currentText.setLength(0)
                        }
                        "binary" -> {
                            val binId = currentBinaryId
                            if (binId != null) {
                                binaries[binId] = binaryContent.toString().replace("\n", "").replace("\r", "")
                                currentBinaryId = null
                            }
                        }
                        "section" -> {
                            if (inBody) {
                                flushChapter()
                                if (sectionDepth > 0) sectionDepth--
                            }
                        }
                        "body" -> inBody = false
                    }
                }
            }
            eventType = parser.next()
        }

        flushChapter()

        val coverData = coverId?.let { binaries[it] } ?: binaries.values.firstOrNull()

        return Book(
            id = UUID.randomUUID().toString(),
            title = title,
            author = author,
            coverBase64 = coverData,
            format = BookFormat.FB2,
            uriString = uriString,
            chapters = if (chapters.isNotEmpty()) chapters else listOf(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    content = "Книга не содержит текста",
                    order = 0
                )
            ),
            progressPercent = 0
        )
    }

    private fun isLikelyHeading(line: String): Boolean {
        if (line.length > 80 || line.isBlank()) return false
        val lower = line.lowercase()
        return lower.startsWith("глава") ||
                lower.startsWith("часть") ||
                lower.startsWith("пролог") ||
                lower.startsWith("эпилог") ||
                lower.startsWith("введение") ||
                lower.startsWith("предисловие") ||
                lower.startsWith("послесловие") ||
                lower.startsWith("chapter") ||
                lower.startsWith("act") ||
                line.matches(Regex("^[IVXLCDM]+\\.?.*")) ||
                line.matches(Regex("^\\d+\\.?.*"))
    }
}
