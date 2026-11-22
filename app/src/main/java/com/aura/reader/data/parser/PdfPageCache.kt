package com.aura.reader.data.parser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

/** Disk cache for pages actually requested by the reader, bounded to [maxCachedPages] JPEGs. */
object PdfPageCache {
    var maxCachedPages: Int = 200

    private var activeSourcePath: String? = null
    private var activeDescriptor: ParcelFileDescriptor? = null
    private var activeRenderer: PdfRenderer? = null

    fun pageUri(source: File, page: Int): String = Uri.Builder().scheme("aura-pdf").path("page")
        .appendQueryParameter("source", source.absolutePath).appendQueryParameter("page", page.toString()).build().toString()

    fun getCachedFile(context: Context, uri: String): File? {
        val model = Uri.parse(uri)
        if (model.scheme != "aura-pdf") return null
        val sourceParam = model.getQueryParameter("source") ?: return null
        val pageParam = model.getQueryParameter("page") ?: return null
        val source = File(sourceParam)
        val index = pageParam.toIntOrNull() ?: return null
        val key = java.util.UUID.nameUUIDFromBytes((source.absolutePath + ":" + source.length() + ":" + source.lastModified() + ":" + index).toByteArray())
        val cache = File(context.cacheDir, "pdf_pages")
        val target = File(cache, key.toString() + ".jpg")
        return if (target.exists() && target.length() > 0L) target else null
    }

    @Synchronized
    private fun getRenderer(source: File): PdfRenderer {
        val path = source.canonicalPath
        if (activeSourcePath == path && activeRenderer != null) {
            return activeRenderer!!
        }
        closeRenderer()
        val desc = ParcelFileDescriptor.open(source, ParcelFileDescriptor.MODE_READ_ONLY)
        val rend = PdfRenderer(desc)
        activeSourcePath = path
        activeDescriptor = desc
        activeRenderer = rend
        return rend
    }

    @Synchronized
    fun getPageAspectRatio(uri: String): Float? {
        val model = Uri.parse(uri)
        if (model.scheme != "aura-pdf") return null
        val sourceParam = model.getQueryParameter("source") ?: return null
        val pageParam = model.getQueryParameter("page") ?: return null
        val source = File(sourceParam)
        val index = pageParam.toIntOrNull() ?: return null
        return try {
            val renderer = getRenderer(source)
            if (index in 0 until renderer.pageCount) {
                renderer.openPage(index).use { page ->
                    if (page.width > 0 && page.height > 0) page.width.toFloat() / page.height.toFloat() else null
                }
            } else null
        } catch (_: Exception) { null }
    }

    @Synchronized
    fun closeRenderer() {
        try { activeRenderer?.close() } catch (_: Exception) {}
        try { activeDescriptor?.close() } catch (_: Exception) {}
        activeRenderer = null
        activeDescriptor = null
        activeSourcePath = null
    }

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
        if (!target.exists() || target.length() == 0L) {
            val renderer = try {
                getRenderer(source)
            } catch (e: Exception) {
                closeRenderer()
                getRenderer(source)
            }
            require(index in 0 until renderer.pageCount)
            renderer.openPage(index).use { page ->
                val bitmap = bitmap(page, 1080)
                val pending = File(cache, target.name + ".tmp")
                try {
                    pending.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                    check(pending.renameTo(target)) { "Cannot cache PDF page" }
                } finally {
                    bitmap.recycle()
                    pending.delete()
                }
            }
            val files = cache.listFiles()?.filter { it.extension == "jpg" }
            if (files != null && files.size > maxCachedPages) {
                files.sortedByDescending { it.lastModified() }
                    .drop(maxCachedPages)
                    .forEach { it.delete() }
            }
        }
        target.setLastModified(System.currentTimeMillis())
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
