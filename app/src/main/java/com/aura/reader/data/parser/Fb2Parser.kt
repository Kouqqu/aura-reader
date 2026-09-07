package com.aura.reader.data.parser

import android.util.Base64
import android.util.Xml
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
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

        // Section parsing tracking
        var sectionDepth = 0
        var currentSectionTitleLines = mutableListOf<String>()
        var currentSectionText = StringBuilder()
        var currentTitleLine = StringBuilder()
        var inTitle = false
        var inParagraph = false

        fun flushChapter() {
            val text = currentSectionText.toString().trim()
            if (text.isNotBlank()) {
                val fullTitle = currentSectionTitleLines
                    .filter { it.isNotBlank() }
                    .joinToString(": ")

                // If no explicit title was in <title>, check first line
                val (resolvedTitle, resolvedText) = if (fullTitle.isBlank()) {
                    val firstLine = text.substringBefore("\n\n").trim()
                    if (isLikelyHeading(firstLine)) {
                        val remaining = text.substringAfter("\n\n", "").trim()
                        Pair(firstLine, remaining.ifBlank { text })
                    } else {
                        Pair("Глава ${chapters.size + 1}", text)
                    }
                } else {
                    Pair(fullTitle, text)
                }

                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        title = resolvedTitle,
                        content = resolvedText,
                        order = chapters.size
                    )
                )
                currentSectionText = StringBuilder()
                currentSectionTitleLines = mutableListOf()
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
                        "body" -> {
                            inBody = true
                        }
                        "section" -> {
                            if (inBody) {
                                // If a nested or sibling section starts and we already have chapter content, flush it
                                flushChapter()
                                sectionDepth++
                            }
                        }
                        "title" -> {
                            if (sectionDepth > 0 || inBody) {
                                inTitle = true
                            }
                        }
                        "p" -> {
                            inParagraph = true
                            if (inTitle) {
                                currentTitleLine = StringBuilder()
                            }
                        }
                        "empty-line" -> {
                            if (!inTitle) {
                                currentSectionText.append("\n\n")
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
                    } else if (inTitle) {
                        if (text != null && text.isNotBlank()) {
                            currentTitleLine.append(text)
                        }
                    } else if (inParagraph && text != null) {
                        currentSectionText.append(text)
                    }
                }

                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "title-info" -> inTitleInfo = false
                        "coverpage" -> inCoverpage = false
                        "title" -> {
                            inTitle = false
                        }
                        "p" -> {
                            inParagraph = false
                            if (inTitle) {
                                val line = currentTitleLine.toString().trim()
                                if (line.isNotEmpty()) {
                                    currentSectionTitleLines.add(line)
                                }
                                currentTitleLine = StringBuilder()
                            } else {
                                currentSectionText.append("\n\n")
                            }
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
                lower.startsWith("chapter") ||
                lower.startsWith("act") ||
                line.matches(Regex("^[IVXLCDM]+\\.?.*")) || // Roman numerals
                line.matches(Regex("^\\d+\\.?.*")) // Numbers: "1. Beginning"
    }
}
