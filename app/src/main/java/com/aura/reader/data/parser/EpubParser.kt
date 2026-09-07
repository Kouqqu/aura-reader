package com.aura.reader.data.parser

import android.util.Base64
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import org.jsoup.Jsoup
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URLDecoder
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object EpubParser {

    fun parse(inputStream: InputStream, uriString: String, fileName: String): Book {
        val files = mutableMapOf<String, ByteArray>()
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry

        while (entry != null) {
            if (!entry.isDirectory) {
                // Normalize path separators
                val normalizedName = entry.name.replace("\\", "/")
                files[normalizedName] = zis.readBytes()
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        // 1. Locate rootfile from META-INF/container.xml
        val containerBytes = files["META-INF/container.xml"]
            ?: throw IllegalArgumentException("Неверный EPUB формат: отсутствует META-INF/container.xml")

        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val containerDoc = docBuilder.parse(ByteArrayInputStream(containerBytes))
        val rootfileElement = containerDoc.getElementsByTagName("rootfile").item(0) as? Element
            ?: throw IllegalArgumentException("Не найден rootfile в container.xml")

        val opfPath = rootfileElement.getAttribute("full-path")
        val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") else ""

        val opfBytes = files[opfPath]
            ?: throw IllegalArgumentException("Не найден OPF файл по пути: $opfPath")

        // 2. Parse OPF package
        val opfDoc = docBuilder.parse(ByteArrayInputStream(opfBytes))

        // Metadata
        val title = opfDoc.getElementsByTagName("dc:title").item(0)?.textContent?.trim()
            ?: fileName.removeSuffix(".epub")
        val author = opfDoc.getElementsByTagName("dc:creator").item(0)?.textContent?.trim() ?: ""

        // Manifest: id -> href
        val manifestItems = opfDoc.getElementsByTagName("item")
        val idToHref = mutableMapOf<String, String>()
        val idToMediaType = mutableMapOf<String, String>()
        var coverHref: String? = null

        for (i in 0 until manifestItems.length) {
            val item = manifestItems.item(i) as Element
            val id = item.getAttribute("id")
            val href = item.getAttribute("href")
            val mediaType = item.getAttribute("media-type")
            val properties = item.getAttribute("properties")

            idToHref[id] = href
            idToMediaType[id] = mediaType

            if (properties?.contains("cover-image") == true ||
                id.equals("cover", ignoreCase = true) ||
                id.equals("cover-image", ignoreCase = true)
            ) {
                coverHref = href
            }
        }

        // Spine: ordered list of itemrefs
        val spineItems = opfDoc.getElementsByTagName("itemref")
        val chapterHrefs = mutableListOf<String>()
        for (i in 0 until spineItems.length) {
            val itemRef = spineItems.item(i) as Element
            val idref = itemRef.getAttribute("idref")
            idToHref[idref]?.let { chapterHrefs.add(it) }
        }

        // Resolve cover image to base64
        var coverBase64: String? = null
        val resolvedCoverPath = coverHref?.let { resolvePath(opfDir, it) }
        if (resolvedCoverPath != null && files.containsKey(resolvedCoverPath)) {
            val coverBytes = files[resolvedCoverPath]
            if (coverBytes != null && coverBytes.isNotEmpty()) {
                coverBase64 = Base64.encodeToString(coverBytes, Base64.NO_WRAP)
            }
        }

        // 3. Parse chapters
        val chapters = mutableListOf<Chapter>()
        var order = 0

        for (chHref in chapterHrefs) {
            val resolvedChPath = resolvePath(opfDir, chHref)
            val chBytes = files[resolvedChPath] ?: continue

            val htmlString = String(chBytes, Charsets.UTF_8)
            val jsoupDoc = Jsoup.parse(htmlString)

            // Extract title
            val chapterTitle = jsoupDoc.select("h1, h2, h3, title").firstOrNull()?.text()?.trim()
                ?: "Глава ${order + 1}"

            // Extract clean readable text with paragraph breaks
            val paragraphs = jsoupDoc.select("p, h1, h2, h3, h4, h5, h6, blockquote, li")
            val bodyText = if (paragraphs.isNotEmpty()) {
                paragraphs.map { it.text().trim() }.filter { it.isNotBlank() }.joinToString("\n\n")
            } else {
                jsoupDoc.body().text().trim()
            }

            if (bodyText.isNotBlank()) {
                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        title = chapterTitle,
                        content = bodyText,
                        order = order++
                    )
                )
            }
        }

        return Book(
            id = UUID.randomUUID().toString(),
            title = title,
            author = author,
            coverBase64 = coverBase64,
            format = BookFormat.EPUB,
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

    private fun resolvePath(baseDir: String, relativePath: String): String {
        val decoded = try {
            URLDecoder.decode(relativePath, "UTF-8")
        } catch (e: Exception) {
            relativePath
        }

        val combined = if (baseDir.isBlank()) decoded else "$baseDir/$decoded"
        val parts = combined.replace("\\", "/").split("/")
        val resolvedStack = mutableListOf<String>()

        for (part in parts) {
            when {
                part.isEmpty() || part == "." -> continue
                part == ".." -> if (resolvedStack.isNotEmpty()) resolvedStack.removeAt(resolvedStack.size - 1)
                else -> resolvedStack.add(part)
            }
        }
        return resolvedStack.joinToString("/")
    }
}
