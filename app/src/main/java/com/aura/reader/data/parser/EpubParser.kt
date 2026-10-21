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
import java.util.zip.ZipFile
import com.aura.reader.data.io.copyBoundedTo
import com.aura.reader.data.io.readBoundedBytes
import javax.xml.parsers.DocumentBuilderFactory

object EpubParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String,
        imagesDir: File? = null
    ): Book {
        val archive = File.createTempFile("epub_", ".zip", imagesDir?.parentFile)
        try {
            inputStream.use { input -> archive.outputStream().use { input.copyBoundedTo(it, 512L * 1024 * 1024) } }
            return parseFile(archive, uriString, fileName, imagesDir)
        } finally { archive.delete() }
    }

    fun parseFile(archive: File, uriString: String, fileName: String, imagesDir: File? = null): Book {
        ZipFile(archive).use { zip ->
            val files = linkedMapOf<String, ZipEntry>()
            var totalSize = 0L
            val entries = zip.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                require(files.size < 10000) { "Too many EPUB entries" }
                if (!entry.isDirectory) {
                    require(entry.size in 0..(32L * 1024 * 1024)) { "EPUB entry exceeds size limit" }
                    totalSize += entry.size
                    require(totalSize <= 512L * 1024 * 1024) { "EPUB exceeds expanded size limit" }
                    val name = entry.name.replace('\\', '/')
                    require(files.put(name, entry) == null) { "Duplicate EPUB entry" }
                }
            }
            fun readEntry(entry: ZipEntry?): ByteArray? = entry?.let {
                zip.getInputStream(it).use { input -> input.readBoundedBytes(32L * 1024 * 1024) }
            }

            // Extract image files to imagesDir if provided
            val extractedImages = mutableMapOf<String, String>()
            if (imagesDir != null) {
                for ((path, entry) in files) {
                    val lower = path.lowercase()
                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") ||
                        lower.endsWith(".webp") || lower.endsWith(".gif") || lower.endsWith(".svg")
                    ) {
                        try {
                            val safeName = path.replace("/", "_").replace("\\", "_")
                            val imgFile = File(imagesDir, safeName)
                            imgFile.writeBytes(readEntry(entry)!!)
                            extractedImages[path] = imgFile.absolutePath
                            val fileOnly = path.substringAfterLast("/")
                            extractedImages[fileOnly] = imgFile.absolutePath
                        } catch (e: Exception) {}
                    }
                }
            }

            // 1. Locate rootfile from META-INF/container.xml
            val containerBytes = readEntry(files["META-INF/container.xml"])
                ?: throw IllegalArgumentException("Неверный EPUB формат: отсутствует META-INF/container.xml")

            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val containerDoc = docBuilder.parse(ByteArrayInputStream(containerBytes))
            val rootfileElement = containerDoc.getElementsByTagName("rootfile").item(0) as? Element
                ?: throw IllegalArgumentException("Не найден rootfile в container.xml")

            val opfPath = rootfileElement.getAttribute("full-path")
            val opfDir = if (opfPath.contains("/")) opfPath.substringBeforeLast("/") else ""

            val opfBytes = readEntry(files[opfPath])
                ?: throw IllegalArgumentException("Не найден OPF файл по пути: $opfPath")

            // 2. Parse OPF package
            val opfDoc = docBuilder.parse(ByteArrayInputStream(opfBytes))

            // Metadata
            val title = opfDoc.getElementsByTagName("dc:title").item(0)?.textContent?.trim()
                ?: fileName.removeSuffix(".epub")
            val author = opfDoc.getElementsByTagName("dc:creator").item(0)?.textContent?.trim() ?: ""

            var series: String? = null
            var seriesNumber: Int? = null

            val metaNodes = opfDoc.getElementsByTagName("meta")
            for (i in 0 until metaNodes.length) {
                val meta = metaNodes.item(i) as? Element ?: continue
                val nameAttr = meta.getAttribute("name")
                val contentAttr = meta.getAttribute("content")
                val propertyAttr = meta.getAttribute("property")

                // Calibre / EPUB2 conventions
                if (nameAttr.equals("calibre:series", ignoreCase = true) && !contentAttr.isNullOrBlank()) {
                    series = contentAttr.trim()
                }
                if (nameAttr.equals("calibre:series_index", ignoreCase = true) && !contentAttr.isNullOrBlank()) {
                    seriesNumber = contentAttr.trim().toDoubleOrNull()?.toInt() ?: contentAttr.trim().toIntOrNull()
                }

                // EPUB3 conventions
                if (propertyAttr.equals("belongs-to-collection", ignoreCase = true)) {
                    val text = meta.textContent?.trim()
                    if (!text.isNullOrBlank() && series == null) {
                        series = text
                    }
                }
                if (propertyAttr.equals("group-position", ignoreCase = true)) {
                    val text = meta.textContent?.trim()
                    val num = text?.toDoubleOrNull()?.toInt() ?: text?.toIntOrNull()
                    if (num != null && seriesNumber == null) {
                        seriesNumber = num
                    }
                }
            }

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
                val ncxBytes = readEntry(files[resolvedNcxPath])
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
                val coverBytes = readEntry(files[resolvedCoverPath])
                if (coverBytes != null && coverBytes.isNotEmpty()) {
                    coverBase64 = Base64.encodeToString(coverBytes, Base64.NO_WRAP)
                }
            }

            // 3. Comprehensive scan for footnotes across all documents in EPUB
            val chapters = mutableListOf<Chapter>()
            val footnotes = mutableMapOf<String, String>()

            for ((path, entry) in files) {
                val lower = path.lowercase()
                if (lower.endsWith(".xhtml") || lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".xml")) {
                    try {
                        val doc = Jsoup.parse(String(readEntry(entry)!!, Charsets.UTF_8))
                        val fileName = path.substringAfterLast("/")

                        // Look for aside, epub:type footnotes, elements with note/footnote classes or IDs
                        doc.select("aside, [epub\\:type~=footnote], [epub\\:type~=note], [epub\\:type~=endnote], .footnote, .note, .endnote, [id^='fn'], [id^='note'], [id^='n_'], [id^='comment']").forEach { noteEl ->
                            val id = noteEl.id().ifBlank { noteEl.attr("name") }
                            val clone = noteEl.clone()
                            clone.select("a[href]").remove()
                            val text = clone.text().trim()
                            if (id.isNotBlank() && text.isNotBlank()) {
                                registerFootnote(footnotes, id, text, fileName)
                            }
                        }

                        // Look for list items or containers with id in note documents
                        if (lower.contains("note") || lower.contains("comment") || lower.contains("annot")) {
                            doc.select("li[id], p[id], div[id], section[id]").forEach { itemEl ->
                                val id = itemEl.id().ifBlank { itemEl.attr("name") }
                                val clone = itemEl.clone()
                                clone.select("a[href]").remove()
                                val text = clone.text().trim()
                                if (id.isNotBlank() && text.isNotBlank()) {
                                    registerFootnote(footnotes, id, text, fileName)
                                }
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            var order = 0

            for (chHref in chapterHrefs) {
                val resolvedChPath = resolvePath(opfDir, chHref)
                val chBytes = readEntry(files[resolvedChPath]) ?: continue

                val htmlString = String(chBytes, Charsets.UTF_8)
                val jsoupDoc = Jsoup.parse(htmlString)

                val body = jsoupDoc.body()

                // 1) Determine title & whether this spine item starts a real chapter:
                val cleanHref = chHref.substringAfterLast("/")
                val explicitTocTitle = tocTitles[chHref] ?: tocTitles[cleanHref]

                // If not in TOC, check <body> headings (avoiding <head><title>)
                val headingEl = if (explicitTocTitle == null) {
                    body.select("h1, h2, h3, [class*='chapter'], [class*='title']").firstOrNull { el ->
                        val txt = el.text().trim()
                        txt.isNotBlank() && !txt.equals(title, ignoreCase = true) && !txt.equals(author, ignoreCase = true)
                    }
                } else null
                val explicitHeadingTitle = headingEl?.text()?.trim()

                val rawText = body.text().trim()
                val firstP = body.select("p").firstOrNull()?.text()?.trim() ?: ""
                val isHeadingParagraph = isLikelyHeading(firstP)

                // Front-matter recognition ONLY for the beginning of the book before first real chapter
                val isInitialFrontMatter = chapters.isEmpty() && (
                        cleanHref.contains("cover", ignoreCase = true) ||
                        cleanHref.contains("title", ignoreCase = true) ||
                        cleanHref.contains("copy", ignoreCase = true) ||
                        cleanHref.contains("annot", ignoreCase = true) ||
                        cleanHref.contains("info", ignoreCase = true)
                )

                val startsNewChapter = explicitTocTitle != null ||
                        explicitHeadingTitle != null ||
                        isHeadingParagraph ||
                        chapters.isEmpty()

                val resolvedTitle = when {
                    explicitTocTitle != null -> explicitTocTitle
                    explicitHeadingTitle != null -> explicitHeadingTitle
                    isHeadingParagraph -> firstP
                    isInitialFrontMatter && cleanHref.contains("cover", ignoreCase = true) -> "Обложка"
                    isInitialFrontMatter && cleanHref.contains("title", ignoreCase = true) -> "Титул"
                    isInitialFrontMatter && cleanHref.contains("copy", ignoreCase = true) -> "Информация об издании"
                    isInitialFrontMatter && cleanHref.contains("annot", ignoreCase = true) -> "Аннотация"
                    else -> title.ifBlank { "Книга" }
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

                    // Skip standalone footnote container elements from being added as paragraphs
                    if (el.tagName() == "aside" ||
                        el.attr("epub:type").contains("footnote") ||
                        el.attr("epub:type").contains("endnote") ||
                        (el.hasClass("footnote") && !el.tagName().equals("p", ignoreCase = true)) ||
                        el.parents().any { it.tagName() == "aside" || it.attr("epub:type").contains("footnote") }
                    ) {
                        continue
                    }

                    // Process footnote links inside this element:
                    // Replace <a href="...">num</a> with [num] so that InteractiveText highlights and links it!
                    val anchorLinks = el.select("a[href]")
                    for (a in anchorLinks) {
                        val href = a.attr("href").trim()
                        val aText = a.text().trim()
                        val target = if (href.contains("#")) href.substringAfter("#").trim() else href
                        val cleanFile = if (href.contains("#")) href.substringBefore("#").substringAfterLast("/") else ""

                        val isFootnoteLink = a.attr("epub:type").contains("noteref") ||
                            a.hasClass("noteref") || a.hasClass("footnote") || a.hasClass("note") ||
                            target.contains("note", ignoreCase = true) ||
                            target.contains("fn", ignoreCase = true) ||
                            target.contains("n_", ignoreCase = true) ||
                            target.contains("comment", ignoreCase = true) ||
                            cleanFile.contains("note", ignoreCase = true) ||
                            cleanFile.contains("comment", ignoreCase = true) ||
                            aText.matches(Regex("""^\[?\d{1,4}\]?$""")) ||
                            aText.matches(Regex("""^\[?[¹²³⁴⁵⁶⁷⁸⁹⁰]+\]?$""")) ||
                            aText == "*" || aText == "**" || aText == "***"

                        if (isFootnoteLink || footnotes.containsKey(target) || footnotes.containsKey(href) || (cleanFile.isNotBlank() && footnotes.containsKey("$cleanFile#$target"))) {
                            val cleanNum = aText.removePrefix("[").removeSuffix("]").trim()
                            val display = if (cleanNum.isNotBlank()) cleanNum else target
                            val bracketed = if (display.startsWith("[") && display.endsWith("]")) display else "[$display]"

                            val resolvedNote = footnotes[target]
                                ?: footnotes[href]
                                ?: (if (cleanFile.isNotBlank()) footnotes["$cleanFile#$target"] else null)
                                ?: footnotes[cleanNum]
                                ?: footnotes["[$cleanNum]"]

                            if (resolvedNote != null) {
                                footnotes[bracketed] = resolvedNote
                                footnotes[display] = resolvedNote
                                footnotes[cleanNum] = resolvedNote
                            } else if (target.isNotBlank()) {
                                footnotes[bracketed] = target
                                footnotes[display] = target
                            }

                            a.text(" $bracketed ")
                        }
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
                    if (startsNewChapter || chapters.isEmpty()) {
                        chapters.add(
                            Chapter(
                                id = UUID.randomUUID().toString(),
                                title = resolvedTitle,
                                content = rawText,
                                blocks = blocks,
                                order = chapters.size
                            )
                        )
                    } else {
                        // It is an unheaded illustration, continuation, or sub-part:
                        // Merge smoothly into the previous chapter!
                        val prev = chapters.removeAt(chapters.size - 1)
                        val mergedBlocks = prev.blocks + blocks
                        val mergedContent = if (prev.content.isNotBlank()) prev.content + "\n\n" + rawText else rawText
                        chapters.add(prev.copy(blocks = mergedBlocks, content = mergedContent))
                    }
                }
            }

            // Resolve any indirect footnote pointers
            for ((key, value) in footnotes.toMap()) {
                val resolved = footnotes[value]
                if (resolved != null && resolved != value) {
                    footnotes[key] = resolved
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
                footnotes = footnotes,
                progressPercent = 0,
                series = series,
                seriesNumber = seriesNumber
            )
        }
    }

    private fun isLikelyHeading(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.length > 80) return false
        val lower = trimmed.lowercase()
        if (lower.startsWith("глава") ||
            lower.startsWith("часть") ||
            lower.startsWith("раздел") ||
            lower.startsWith("книга") ||
            lower.startsWith("пролог") ||
            lower.startsWith("эпилог") ||
            lower.startsWith("введение") ||
            lower.startsWith("предисловие") ||
            lower.startsWith("послесловие") ||
            lower.startsWith("chapter") ||
            lower.startsWith("part") ||
            lower.startsWith("prologue") ||
            lower.startsWith("epilogue") ||
            lower.startsWith("act")
        ) {
            return true
        }
        if (trimmed.matches(Regex("""^[IVXLCDM]+(?:\.|\:|\s+|$).{0,60}$""", RegexOption.IGNORE_CASE))) {
            return true
        }
        if (trimmed.matches(Regex("""^\d{1,4}(?:\.|\:|\s+|$)[^\.\!\?]{0,60}$"""))) {
            return true
        }
        return false
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

    private fun registerFootnote(
        footnotes: MutableMap<String, String>,
        id: String,
        rawText: String,
        fileName: String = ""
    ) {
        val text = rawText.trim()
        if (text.isBlank()) return

        footnotes[id] = text
        footnotes["[$id]"] = text
        footnotes["#$id"] = text
        if (fileName.isNotBlank()) {
            footnotes["$fileName#$id"] = text
        }

        val digits = id.filter { it.isDigit() }
        if (digits.isNotBlank()) {
            footnotes[digits] = text
            footnotes["[$digits]"] = text
            footnotes["{$digits}"] = text
            footnotes["#$digits"] = text
            if (fileName.isNotBlank()) {
                footnotes["$fileName#$digits"] = text
            }
        }
    }
}
