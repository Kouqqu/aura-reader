package com.aura.reader.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory

object CbzParser {

    private val naturalOrderComparator = Comparator<String> { a, b ->
        val re = Regex("(\\d+)|(\\D+)")
        val aMatcher = re.findAll(a).map { it.value }.iterator()
        val bMatcher = re.findAll(b).map { it.value }.iterator()
        while (aMatcher.hasNext() && bMatcher.hasNext()) {
            val aToken = aMatcher.next()
            val bToken = bMatcher.next()
            val aNum = aToken.toLongOrNull()
            val bNum = bToken.toLongOrNull()
            val cmp = if (aNum != null && bNum != null) {
                aNum.compareTo(bNum)
            } else {
                aToken.compareTo(bToken, ignoreCase = true)
            }
            if (cmp != 0) return@Comparator cmp
        }
        a.length.compareTo(b.length)
    }

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String,
        imagesDir: File? = null
    ): Book {
        val targetDir = imagesDir ?: File.createTempFile("cbz_", "_dir").apply {
            delete()
            mkdirs()
        }
        targetDir.mkdirs()

        val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif")
        val imageFiles = mutableListOf<Pair<String, File>>()
        var comicInfoBytes: ByteArray? = null

        val zis = ZipInputStream(inputStream)
        var entry: ZipEntry? = zis.nextEntry
        var counter = 0

        while (entry != null) {
            val name = entry.name.replace("\\", "/")
            val cleanName = name.substringAfterLast("/")
            val lower = name.lowercase()
            val ext = lower.substringAfterLast(".", "")

            if (!entry.isDirectory && !name.contains("__MACOSX") && !cleanName.startsWith(".")) {
                if (ext in imageExtensions) {
                    counter++
                    val safeFileName = "page_${counter.toString().padStart(5, '0')}_${cleanName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
                    val outFile = File(targetDir, safeFileName)
                    FileOutputStream(outFile).use { out ->
                        zis.copyTo(out)
                    }
                    imageFiles.add(name to outFile)
                } else if (cleanName.equals("comicinfo.xml", ignoreCase = true)) {
                    comicInfoBytes = zis.readBytes()
                }
            }
            zis.closeEntry()
            entry = zis.nextEntry
        }

        if (imageFiles.isEmpty()) {
            throw IllegalArgumentException("Архив CBZ «$fileName» не содержит изображений.")
        }

        // Sort image files by original entry path using natural sort
        val sortedImages = imageFiles.sortedWith(compareBy(naturalOrderComparator) { it.first })

        // Extract metadata from ComicInfo.xml if present
        var title: String? = null
        var series: String? = null
        var seriesNumber: Int? = null
        var writer: String? = null

        if (comicInfoBytes != null) {
            try {
                val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                val doc = docBuilder.parse(ByteArrayInputStream(comicInfoBytes))

                title = doc.getElementsByTagName("Title").item(0)?.textContent?.trim()
                series = doc.getElementsByTagName("Series").item(0)?.textContent?.trim()
                val numStr = doc.getElementsByTagName("Number").item(0)?.textContent?.trim()
                seriesNumber = numStr?.toDoubleOrNull()?.toInt() ?: numStr?.toIntOrNull()
                writer = doc.getElementsByTagName("Writer").item(0)?.textContent?.trim()
            } catch (e: Exception) {
                // Ignore ComicInfo parsing errors
            }
        }

        val cleanTitle = if (!title.isNullOrBlank()) {
            title
        } else {
            fileName.removeSuffix(".cbz").removeSuffix(".cbr").removeSuffix(".zip")
        }

        val cleanAuthor = writer ?: ""

        // Generate coverBase64 thumbnail from the first image
        var coverBase64: String? = null
        val firstFile = sortedImages.firstOrNull()?.second
        if (firstFile != null && firstFile.exists()) {
            try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(firstFile.absolutePath, opts)
                val origW = opts.outWidth
                val origH = opts.outHeight
                if (origW > 0 && origH > 0) {
                    var sampleSize = 1
                    val targetW = 220
                    while (origW / (sampleSize * 2) >= targetW) {
                        sampleSize *= 2
                    }
                    val decodeOpts = BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                    }
                    val bmp = BitmapFactory.decodeFile(firstFile.absolutePath, decodeOpts)
                    if (bmp != null) {
                        val scaledHeight = (bmp.height * (targetW.toFloat() / bmp.width)).toInt().coerceAtLeast(1)
                        val thumb = Bitmap.createScaledBitmap(bmp, targetW, scaledHeight, true)
                        val baos = ByteArrayOutputStream()
                        thumb.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                        coverBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                        if (thumb != bmp) thumb.recycle()
                        bmp.recycle()
                    }
                }
            } catch (e: Exception) {
                // Fallback: ignore cover error
            }
        }

        // Determine if entries are grouped in distinct folders (e.g. chapters)
        val folderGroups = sortedImages.groupBy { (path, _) ->
            path.substringBeforeLast("/", "")
        }

        val totalPages = sortedImages.size
        val chapters = if (folderGroups.size > 1 && folderGroups.keys.all { it.isNotEmpty() }) {
            folderGroups.entries.mapIndexed { chIndex, (folderName, groupImages) ->
                val groupBlocks = groupImages.mapIndexed { imgIndex, (_, file) ->
                    FormattedBlock(
                        type = BlockType.IMAGE,
                        text = file.absolutePath,
                        subText = "${imgIndex + 1} / ${groupImages.size}"
                    )
                }
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = folderName.substringAfterLast("/"),
                    content = "",
                    blocks = groupBlocks,
                    order = chIndex
                )
            }
        } else {
            val blocks = sortedImages.mapIndexed { index, (_, file) ->
                FormattedBlock(
                    type = BlockType.IMAGE,
                    text = file.absolutePath,
                    subText = "${index + 1} / $totalPages"
                )
            }
            listOf(
                Chapter(
                    id = UUID.randomUUID().toString(),
                    title = cleanTitle,
                    content = "",
                    blocks = blocks,
                    order = 0
                )
            )
        }

        return Book(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            author = cleanAuthor,
            coverBase64 = coverBase64,
            format = BookFormat.CBZ,
            uriString = uriString,
            chapters = chapters,
            progressPercent = 0,
            series = series,
            seriesNumber = seriesNumber
        )
    }
}
