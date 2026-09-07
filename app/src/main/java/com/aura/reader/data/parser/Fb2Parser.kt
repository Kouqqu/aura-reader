package com.aura.reader.data.parser

import android.util.Base64
import android.util.Xml
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
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
        parser.setInput(stream, null) // null allows XML encoding declaration auto-detection

        var eventType = parser.eventType
        var currentSectionTitle = ""
        var currentSectionText = StringBuilder()
        var inTitleInfo = false
        var inCoverpage = false
        var inBody = false
        var inSection = false
        var inTitle = false
        var inParagraph = false
        var currentBinaryId: String? = null
        var binaryContent = StringBuilder()

        var chapterOrder = 0

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
                                if (currentSectionText.isNotBlank()) {
                                    chapters.add(
                                        Chapter(
                                            id = UUID.randomUUID().toString(),
                                            title = currentSectionTitle.ifBlank { "Глава ${chapterOrder + 1}" },
                                            content = currentSectionText.toString().trim(),
                                            order = chapterOrder++
                                        )
                                    )
                                    currentSectionText = StringBuilder()
                                    currentSectionTitle = ""
                                }
                                inSection = true
                            }
                        }
                        "title" -> {
                            if (inSection || inBody) inTitle = true
                        }
                        "p" -> {
                            inParagraph = true
                        }
                        "empty-line" -> {
                            currentSectionText.append("\n\n")
                        }
                        "binary" -> {
                            currentBinaryId = parser.getAttributeValue(null, "id")
                            binaryContent = StringBuilder()
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    val text = parser.text
                    if (currentBinaryId != null) {
                        binaryContent.append(text)
                    } else if (inTitle) {
                        if (text != null && text.isNotBlank()) {
                            if (currentSectionTitle.isNotEmpty()) currentSectionTitle += " "
                            currentSectionTitle += text.trim()
                        }
                    } else if (inParagraph && text != null) {
                        currentSectionText.append(text.trim())
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (tagName) {
                        "title-info" -> inTitleInfo = false
                        "coverpage" -> inCoverpage = false
                        "title" -> inTitle = false
                        "p" -> {
                            inParagraph = false
                            currentSectionText.append("\n\n")
                        }
                        "binary" -> {
                            val binId = currentBinaryId
                            if (binId != null) {
                                binaries[binId] = binaryContent.toString().replace("\n", "").replace("\r", "")
                                currentBinaryId = null
                            }
                        }
                        "section" -> {
                            if (currentSectionText.isNotBlank()) {
                                chapters.add(
                                    Chapter(
                                        id = UUID.randomUUID().toString(),
                                        title = currentSectionTitle.ifBlank { "Глава ${chapterOrder + 1}" },
                                        content = currentSectionText.toString().trim(),
                                        order = chapterOrder++
                                    )
                                )
                                currentSectionText = StringBuilder()
                                currentSectionTitle = ""
                            }
                            inSection = false
                        }
                        "body" -> inBody = false
                    }
                }
            }
            eventType = parser.next()
        }

        if (currentSectionText.isNotBlank()) {
            chapters.add(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = currentSectionTitle.ifBlank { "Глава ${chapterOrder + 1}" },
                    content = currentSectionText.toString().trim(),
                    order = chapterOrder
                )
            )
        }

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
}
