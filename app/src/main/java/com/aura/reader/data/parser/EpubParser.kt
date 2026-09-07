package com.aura.reader.data.parser

import android.util.Base64
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import org.jsoup.Jsoup
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URLDecoder
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object EpubParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String,
        imagesDir: File? = null
    ): Book {
        val files = mutableMapOf<String, ByteArray>()
        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry

        while (entry != null) {
            if (!entry.isDirectory) {
                val normalizedName = entry.name.replace("\\", "/")
                files[normalizedName] = zis.readBytes()
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        // Extract image files to imagesDir if provided
        val extractedImages = mutableMapOf<String, String>()
        if (imagesDir != null) {
            for ((path, bytes) in files) {
                val lower = path.lowercase()
                if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                    lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".svg")
                ) {
                    try {
                        val safeName = path.replace("/", "_").replace("\\", "_")
                        val imgFile = File(imagesDir, safeName)
                        imgFile.writeBytes(bytes)
                        extractedImages[path] = imgFile.absolutePath
                        val fileOnly = path.substringAfterLast("/")
                        extractedImages[fileOnly] = imgFile.absolutePath
                    } catch (e: Exception) {}
                }
            }
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

        // Manifest: id -> href & mediaType
        val manifestItems = opfDoc.getElementsByTagName("item")
        val idToHref = mutableMapOf<String, String>()
        val idToMediaType = mutableMapOf<String, String>()
        var coverHref: String? = null
        var ncxHref: String? = null

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

            if (mediaType.equals("application/x-dtbncx+xml", ignoreCase = true) || id.equals("ncx", ignoreCase = true)) {
                ncxHref = href
            }
        }

        // Parse TOC (Table of Contents) from NCX
        // IMPORTANT: do not overwrite parent titles with sub-chapter anchors!
        val tocTitles = mutableMapOf<String, String>()
        if (ncxHref != null) {
            val resolvedNcxPath = resolvePath(opfDir, ncxHref)
            val ncxBytes = files[resolvedNcxPath]
            if (ncxBytes != null) {
                try {
                    val ncxDoc = docBuilder.parse(ByteArrayInputStream(ncxBytes))
                    val navPoints = ncxDoc.getElementsByTagName("navPoint")
                    for (i in 0 until navPoints.length) {
                        val np = navPoints.item(i) as Element
                        val text = np.getElementsByTagName("text").item(0)?.textContent?.trim() ?: continue
                        val contentEl = np.getElementsByTagName("content").item(0) as? Element ?: continue
                        val fullSrc = contentEl.getAttribute("src")
                        val cleanSrc = fullSrc.substringBefore("#")
                        if (cleanSrc.isNotBlank() && text.isNotBlank()) {
                            // Only put if absent so the parent/primary title is preserved!
                            if (!tocTitles.containsKey(cleanSrc)) {
                                tocTitles[cleanSrc] = text
                            }
                            val fileOnly = cleanSrc.substringAfterLast("/")
                            if (!tocTitles.containsKey(fileOnly)) {
                                tocTitles[fileOnly] = text
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore NCX parsing failure
                }
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

        // 3. Parse chapters & blocks
        val chapters = mutableListOf<Chapter>()
        var order = 0

        for (chHref in chapterHrefs) {
            val resolvedChPath = resolvePath(opfDir, chHref)
            val chBytes = files[resolvedChPath] ?: continue

            val htmlString = String(chBytes, Charsets.UTF_8)
            val jsoupDoc = Jsoup.parse(htmlString)
            val body = jsoupDoc.body()

            // 1) Determine title:
            val cleanHref = chHref.substringAfterLast("/")
            var chapterTitle = tocTitles[chHref] ?: tocTitles[cleanHref]

            // If not in TOC, check <body> headings (avoiding <head><title>)
            if (chapterTitle == null) {
                val headingEl = body.select("h1, h2, h3, [class*='chapter'], [class*='title']").firstOrNull { el ->
                    val txt = el.text().trim()
                    txt.isNotBlank() && !txt.equals(title, ignoreCase = true) && !txt.equals(author, ignoreCase = true)
                }
                chapterTitle = headingEl?.text()?.trim()
            }

            // Detect special preliminary/intro pages instead of naming them "Глава X"
            val rawText = body.text().trim()
            val hasOnlyImage = body.select("img, svg, image").isNotEmpty() && rawText.length < 50
            val isIntroOrCopyright = rawText.length < 250 && (
                    cleanHref.contains("cover", ignoreCase = true) ||
                    cleanHref.contains("title", ignoreCase = true) ||
                    cleanHref.contains("copy", ignoreCase = true) ||
                    cleanHref.contains("annot", ignoreCase = true) ||
                    cleanHref.contains("info", ignoreCase = true)
            )

            if (chapterTitle == null) {
                chapterTitle = when {
                    hasOnlyImage || cleanHref.contains("cover", ignoreCase = true) -> "Обложка"
                    cleanHref.contains("title", ignoreCase = true) -> "Титул"
                    cleanHref.contains("copy", ignoreCase = true) -> "Информация об издании"
                    cleanHref.contains("annot", ignoreCase = true) -> "Аннотация"
                    else -> {
                        // Check first short paragraph
                        val firstP = body.select("p").firstOrNull()?.text()?.trim() ?: ""
                        if (isLikelyHeading(firstP)) firstP else "Глава ${order + 1}"
                    }
                }
            }

            // 2) Parse blocks from HTML (Headings, Subtitles, Epigraphs, Paragraphs)
            val blocks = mutableListOf<FormattedBlock>()

            // Traverse direct children of body or top-level containers
            val elements = body.select("h1, h2, h3, h4, h5, h6, blockquote, div.epigraph, div.cite, p, pre, img, figure")
            val chDir = if (resolvedChPath.contains("/")) resolvedChPath.substringBeforeLast("/") else ""

            for (el in elements) {
                val tagName = el.tagName().lowercase()

                // 1) Handle figures
                if (tagName == "figure") {
                    val imgEl = el.select("img, image").firstOrNull()
                    if (imgEl != null) {
                        val src = (imgEl.attr("src").ifBlank { imgEl.attr("xlink:href") }).trim()
                        val caption = el.select("figcaption").firstOrNull()?.text()?.trim()
                            ?: imgEl.attr("alt").ifBlank { imgEl.attr("title") }
                        val resolvedImgPath = resolvePath(chDir, src)
                        val local = extractedImages[resolvedImgPath] ?: extractedImages[src.substringAfterLast("/")]
                        if (local != null) {
                            blocks.add(FormattedBlock(BlockType.IMAGE, text = local, subText = caption.ifBlank { null }))
                        }
                    }
                    continue
                }

                // 2) Handle standalone img / image
                if (tagName == "img" || tagName == "image") {
                    if (el.parents().any { it.tagName() == "figure" }) continue
                    val src = (el.attr("src").ifBlank { el.attr("xlink:href") }).trim()
                    val alt = el.attr("alt").ifBlank { el.attr("title") }.trim()
                    val resolvedImgPath = resolvePath(chDir, src)
                    val local = extractedImages[resolvedImgPath] ?: extractedImages[src.substringAfterLast("/")]
                    if (local != null) {
                        blocks.add(FormattedBlock(BlockType.IMAGE, text = local, subText = alt.ifBlank { null }))
                    }
                    continue
                }

                val text = el.text().trim()
                if (text.isBlank()) continue

                // Check if element is inside an already processed blockquote/epigraph
                if (el.parents().any { it.tagName() == "blockquote" || it.hasClass("epigraph") || it.hasClass("cite") }) {
                    continue
                }

                when {
                    tagName in listOf("h1", "h2") -> {
                        blocks.add(FormattedBlock(BlockType.TITLE, text))
                    }
                    tagName in listOf("h3", "h4", "h5", "h6") || el.hasClass("subtitle") -> {
                        blocks.add(FormattedBlock(BlockType.SUBTITLE, text))
                    }
                    tagName == "blockquote" || el.hasClass("epigraph") || el.hasClass("cite") -> {
                        val authorEl = el.select("cite, p.author, .author, span.author").firstOrNull()
                        val quoteAuthor = authorEl?.text()?.trim()
                        val quoteBody = if (authorEl != null) {
                            val copy = el.clone()
                            copy.select("cite, p.author, .author, span.author").remove()
                            copy.text().trim()
                        } else {
                            text
                        }
                        blocks.add(FormattedBlock(BlockType.EPIGRAPH, quoteBody, quoteAuthor))
                    }
                    tagName == "pre" || el.hasClass("poem") -> {
                        blocks.add(FormattedBlock(BlockType.VERSE, text))
                    }
                    else -> {
                        // Check if paragraph is an epigraph by class
                        if (el.className().contains("epigraph", ignoreCase = true) || el.className().contains("quote", ignoreCase = true)) {
                            blocks.add(FormattedBlock(BlockType.EPIGRAPH, text))
                        } else {
                            blocks.add(FormattedBlock(BlockType.PARAGRAPH, text))
                        }
                    }
                }
            }

            // Fallback if no structured blocks found
            if (blocks.isEmpty() && rawText.isNotBlank()) {
                rawText.split("\n\n").filter { it.isNotBlank() }.forEach {
                    blocks.add(FormattedBlock(BlockType.PARAGRAPH, it.trim()))
                }
            }

            if (blocks.isNotEmpty()) {
                chapters.add(
                    Chapter(
                        id = UUID.randomUUID().toString(),
                        title = chapterTitle,
                        content = rawText,
                        blocks = blocks,
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

    private fun isLikelyHeading(line: String): Boolean {
        if (line.length > 80 || line.isBlank()) return false
        val lower = line.lowercase()
        return lower.startsWith("глава") ||
                lower.startsWith("часть") ||
                lower.startsWith("пролог") ||
                lower.startsWith("эпилог") ||
                lower.startsWith("введение") ||
                lower.startsWith("предисловие") ||
                lower.startsWith("chapter") ||
                lower.startsWith("act") ||
                line.matches(Regex("^[IVXLCDM]+\\.?.*")) ||
                line.matches(Regex("^\\d+\\.?.*"))
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
