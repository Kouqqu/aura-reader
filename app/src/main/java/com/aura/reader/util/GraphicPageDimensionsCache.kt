package com.aura.reader.util

import android.graphics.BitmapFactory
import com.aura.reader.data.parser.PdfPageCache
import java.io.File
import java.util.concurrent.ConcurrentHashMap

object GraphicPageDimensionsCache {
    private val ratioCache = ConcurrentHashMap<String, Float>()

    fun getAspectRatio(key: String): Float {
        return ratioCache[key] ?: 0.70f
    }

    fun setAspectRatio(key: String, ratio: Float) {
        if (ratio in 0.1f..10f) {
            ratioCache[key] = ratio
        }
    }

    fun getOrReadAspectRatio(pathOrUri: String): Float {
        ratioCache[pathOrUri]?.let { return it }

        if (pathOrUri.startsWith("aura-pdf:")) {
            val ratio = PdfPageCache.getPageAspectRatio(pathOrUri)
            if (ratio != null && ratio in 0.1f..10f) {
                ratioCache[pathOrUri] = ratio
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
                            ratioCache[pathOrUri] = ratio
                            return ratio
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        return 0.70f
    }
}
