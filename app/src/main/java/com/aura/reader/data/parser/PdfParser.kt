package com.aura.reader.data.parser

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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

object PdfParser {

    fun parse(
        inputStream: InputStream,
        uriString: String,
        fileName: String,
        imagesDir: File,
        pdfTempFile: File
    ): Book {
        if (!pdfTempFile.exists() || pdfTempFile.length() == 0L) {
            FileOutputStream(pdfTempFile).use { out ->
                inputStream.copyTo(out)
            }
        }

        val pfd = ParcelFileDescriptor.open(pdfTempFile, ParcelFileDescriptor.MODE_READ_ONLY)
            ?: throw IllegalStateException("Не удалось открыть файл PDF: $fileName")

        val renderer = PdfRenderer(pfd)
        val pageCount = renderer.pageCount
        if (pageCount == 0) {
            renderer.close()
            pfd.close()
            throw IllegalStateException("PDF документ «$fileName» не содержит страниц")
        }

        imagesDir.mkdirs()
        val chapters = mutableListOf<Chapter>()
        var coverBase64: String? = null

        val cleanTitle = fileName.substringBeforeLast(".")

        try {
            for (i in 0 until pageCount) {
                val pageFile = File(imagesDir, "page_${i}.jpg")
                if (!pageFile.exists() || pageFile.length() == 0L) {
                    val page = renderer.openPage(i)
                    val targetWidth = 1080
                    val scale = targetWidth.toFloat() / page.width.toFloat()
                    val targetHeight = (page.height * scale).toInt().coerceAtLeast(100)

                    val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()

                    FileOutputStream(pageFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }

                    if (i == 0 && coverBase64 == null) {
                        try {
                            val baos = ByteArrayOutputStream()
                            val thumb = Bitmap.createScaledBitmap(bitmap, 200, (targetHeight * (200f / targetWidth)).toInt(), true)
                            thumb.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                            coverBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
                            thumb.recycle()
                        } catch (e: Exception) {}
                    }

                    bitmap.recycle()
                }

                chapters.add(
                    Chapter(
                        id = "pdf_page_$i",
                        title = "Страница ${i + 1}",
                        content = "",
                        blocks = listOf(
                            FormattedBlock(
                                type = BlockType.IMAGE,
                                text = pageFile.absolutePath,
                                subText = "Страница ${i + 1} из $pageCount"
                            )
                        ),
                        order = i
                    )
                )
            }
        } finally {
            renderer.close()
            pfd.close()
        }

        return Book(
            id = UUID.randomUUID().toString(),
            title = cleanTitle,
            author = "",
            coverBase64 = coverBase64,
            format = BookFormat.PDF,
            uriString = uriString,
            chapters = chapters
        )
    }
}
