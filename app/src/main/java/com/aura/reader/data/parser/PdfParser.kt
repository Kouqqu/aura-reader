package com.aura.reader.data.parser

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Base64
import com.aura.reader.data.model.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object PdfParser {
    /** Read metadata and a thumbnail only. Full-size pages are rendered on demand. */
    fun parse(source: File, persistentSource: File, uriString: String, fileName: String): Book {
        ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
            PdfRenderer(descriptor).use { renderer ->
                val count = renderer.pageCount
                require(count > 0) { "PDF contains no pages" }
                val cover = renderer.openPage(0).use { page ->
                    val thumb = PdfPageCache.bitmap(page, 200)
                    try {
                        val output = ByteArrayOutputStream()
                        thumb.compress(Bitmap.CompressFormat.JPEG, 75, output)
                        Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
                    } finally { thumb.recycle() }
                }
                return Book(
                    id = UUID.randomUUID().toString(), title = fileName.substringBeforeLast("."),
                    coverBase64 = cover, format = BookFormat.PDF, uriString = uriString,
                    chapters = (0 until count).map { index ->
                        Chapter("pdf_page_" + index, "Страница " + (index + 1), "",
                            listOf(FormattedBlock(BlockType.IMAGE, PdfPageCache.pageUri(persistentSource, index),
                                "Страница " + (index + 1) + " из " + count)), index)
                    }
                )
            }
        }
    }
}
