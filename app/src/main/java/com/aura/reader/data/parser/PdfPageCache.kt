package com.aura.reader.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/** Disk cache for pages actually requested by the reader, bounded to twelve JPEGs. */
object PdfPageCache {
    fun pageUri(source: File, page: Int): String = Uri.Builder().scheme("aura-pdf").path("page")
        .appendQueryParameter("source", source.absolutePath).appendQueryParameter("page", page.toString()).build().toString()

    @Synchronized
    fun render(context: Context, uri: String): File {
        val model = Uri.parse(uri)
        require(model.scheme == "aura-pdf")
        val source = File(requireNotNull(model.getQueryParameter("source")))
        require(source.canonicalPath.startsWith(context.filesDir.canonicalPath + File.separator))
        val index = requireNotNull(model.getQueryParameter("page")).toInt()
        val key = java.util.UUID.nameUUIDFromBytes((source.absolutePath + ":" + source.length() + ":" + source.lastModified() + ":" + index).toByteArray())
        val cache = File(context.cacheDir, "pdf_pages").apply { mkdirs() }
        val target = File(cache, key.toString() + ".jpg")
        if (!target.exists()) {
            ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                PdfRenderer(descriptor).use { renderer ->
                    require(index in 0 until renderer.pageCount)
                    renderer.openPage(index).use { page ->
                        val bitmap = bitmap(page, 1080)
                        val pending = File(cache, target.name + ".tmp")
                        try {
                            pending.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                            check(pending.renameTo(target)) { "Cannot cache PDF page" }
                        } finally { bitmap.recycle(); pending.delete() }
                    }
                }
            }
        }
        target.setLastModified(System.currentTimeMillis())
        cache.listFiles()?.filter { it.extension == "jpg" && it != target }
            ?.sortedByDescending { it.lastModified() }?.drop(11)?.forEach { it.delete() }
        return target
    }

    internal fun bitmap(page: PdfRenderer.Page, desiredWidth: Int): Bitmap {
        val scale = minOf(desiredWidth.toDouble() / page.width, kotlin.math.sqrt(4_000_000.0 / (page.width.toDouble() * page.height)))
        val width = (page.width * scale).toInt().coerceAtLeast(1)
        val height = (page.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also {
            it.eraseColor(Color.WHITE)
            page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        }
    }
}
