package com.aura.reader.data.parser

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.aura.reader.data.model.BlockType
import com.aura.reader.data.model.Book
import com.aura.reader.data.model.BookFormat
import com.aura.reader.data.model.Chapter
import com.aura.reader.data.model.FormattedBlock
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object CbtParser {

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
        val targetDir = imagesDir ?: File.createTempFile("cbt_", "_dir").apply {
            delete()
            mkdirs()
        }
        targetDir.mkdirs()

        val imageExtensions = setOf("jpg", "jpeg", "png", "webp", "gif")
        val imageFiles = mutableListOf<Pair<String, File>>()

        val headerBuffer = ByteArray(512)
        var counter = 0

        while (true) {
            val bytesRead = readFully(inputStream, headerBuffer)
            if (bytesRead < 512) break

            // Check for end of archive (block of zeros)
            if (headerBuffer.all { it == 0.toByte() }) break

            // TAR header:
            // 0..99: File name (null terminated ASCII)
            val nameBytes = headerBuffer.copyOfRange(0, 100)
            val rawName = String(nameBytes, Charsets.US_ASCII).substringBefore('\u0000').trim()
            if (rawName.isEmpty()) break

            // 124..135: Size in octal ASCII (null or space terminated)
            val sizeBytes = headerBuffer.copyOfRange(124, 136)
            val sizeStr = String(sizeBytes, Charsets.US_ASCII).substringBefore('\u0000').trim()
            val fileSize = sizeStr.toLongOrNull(8) ?: 0L

            val isDirectory = headerBuffer[156] == '5'.code.toByte() || rawName.endsWith("/")
            val cleanName = rawName.replace("\\", "/").substringAfterLast("/")
            val ext = cleanName.substringAfterLast(".", "").lowercase()

            val paddedSize = ((fileSize + 511) / 512) * 512
            val remainingPadding = paddedSize - fileSize

            if (!isDirectory && ext in imageExtensions && fileSize > 0) {
                counter++
                val safeFileName = "page_${counter.toString().padStart(5, '0')}_${cleanName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
                val outFile = File(targetDir, safeFileName)
                FileOutputStream(outFile).use { fos ->
                    copyBounded(inputStream, fos, fileSize)
                }
                skipBytes(inputStream, remainingPadding)
                imageFiles.add(rawName to outFile)
            } else {
                skipBytes(inputStream, paddedSize)
            }
        }

        if (imageFiles.isEmpty()) {
            throw IllegalArgumentException("Архив CBT «$fileName» не содержит поддерживаемых изображений.")
        }

        val sortedImages = imageFiles.sortedWith(compareBy(naturalOrderComparator) { it.first })
        val cleanTitle = fileName.removeSuffix(".cbt").removeSuffix(".tar")

        // Generate cover
        var coverBase64: String? = null
        val firstFile = sortedImages.firstOrNull()?.second
        if (firstFile != null && firstFile.exists()) {
            try {
                val bmp = BitmapFactory.decodeFile(firstFile.absolutePath)
                if (bmp != null) {
                    val targetW = 220
                    val scaledH = (bmp.height * (targetW.toFloat() / bmp.width)).toInt().coerceAtLeast(1)
                    val thumb = Bitmap.createScaledBitmap(bmp, targetW, scaledH, true)
                    val baos = ByteArrayOutputStream()
                    thumb.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                    if (thumb != bmp) thumb.recycle()
                    bmp.recycle()
                    coverBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                }
            } catch (_: Exception) {}
        }

        val totalPages = sortedImages.size
        val chapters = sortedImages.mapIndexed { index, (_, file) ->
            Chapter(
                id = UUID.randomUUID().toString(),
                title = "Страница ${index + 1}",
                content = "",
                blocks = listOf(
                    FormattedBlock(
                        type = BlockType.IMAGE,
                        text = file.absolutePath,
                        subText = "Страница ${index + 1} из $totalPages"
                    )
                ),
                order = index
            )
        }

        return Book(
            id = UUID.nameUUIDFromBytes("${uriString}_CBT".toByteArray()).toString(),
            title = cleanTitle,
            author = "",
            coverBase64 = coverBase64,
            format = BookFormat.CBZ,
            uriString = uriString,
            chapters = chapters,
            progressPercent = 0
        )
    }

    private fun readFully(input: InputStream, buffer: ByteArray): Int {
        var total = 0
        while (total < buffer.size) {
            val r = input.read(buffer, total, buffer.size - total)
            if (r == -1) break
            total += r
        }
        return total
    }

    private fun copyBounded(input: InputStream, output: FileOutputStream, maxBytes: Long) {
        val buf = ByteArray(8192)
        var remaining = maxBytes
        while (remaining > 0) {
            val toRead = minOf(buf.size.toLong(), remaining).toInt()
            val r = input.read(buf, 0, toRead)
            if (r == -1) break
            output.write(buf, 0, r)
            remaining -= r
        }
    }

    private fun skipBytes(input: InputStream, bytes: Long) {
        var remaining = bytes
        val buf = ByteArray(8192)
        while (remaining > 0) {
            val toSkip = minOf(buf.size.toLong(), remaining).toInt()
            val r = input.read(buf, 0, toSkip)
            if (r == -1) break
            remaining -= r
        }
    }
}
