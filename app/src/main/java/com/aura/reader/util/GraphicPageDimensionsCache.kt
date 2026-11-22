package com.aura.reader.util

import android.graphics.BitmapFactory
import com.aura.reader.data.parser.PdfPageCache
import java.io.File
import java.util.LinkedHashMap

object GraphicPageDimensionsCache {
    private const val MAX_ENTRIES = 500

    @Suppress("SerialVersionUIDInSerializableClass")
    private val ratioCache = object : LinkedHashMap<String, Float>(MAX_ENTRIES, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Float>?): Boolean {
            return size > MAX_ENTRIES
        }
    }
    private val lock = Any()

    fun getAspectRatio(key: String): Float = synchronized(lock) {
        ratioCache[key] ?: 0.70f
    }

    fun setAspectRatio(key: String, ratio: Float) {
        if (ratio in 0.1f..10f) {
            synchronized(lock) {
                ratioCache[key] = ratio
            }
        }
    }

    fun clear() = synchronized(lock) {
        ratioCache.clear()
    }

    fun getOrReadAspectRatio(pathOrUri: String): Float {
        synchronized(lock) {
            ratioCache[pathOrUri]?.let { return it }
        }

        if (pathOrUri.startsWith("aura-pdf:")) {
            val ratio = PdfPageCache.getPageAspectRatio(pathOrUri)
            if (ratio != null && ratio in 0.1f..10f) {
                setAspectRatio(pathOrUri, ratio)
                return ratio
            }
        } else {
            val file = File(pathOrUri)
            if (file.exists() && file.length() > 0L) {
                try {
                    val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, opts)
                    if (opts.outWidth > 0 && opts.outHeight > 0) {
                        val ratio = opts.outWidth.toFloat() / opts.outHeight.toFloat()
                        if (ratio in 0.1f..10f) {
                            setAspectRatio(pathOrUri, ratio)
                            return ratio
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return 0.70f
    }
}
