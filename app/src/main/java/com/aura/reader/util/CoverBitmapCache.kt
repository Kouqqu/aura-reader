package com.aura.reader.util

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object CoverBitmapCache {
    private val memoryCache = object : LruCache<String, ImageBitmap>(64) {}

    fun getOrDecode(key: String, base64: String): ImageBitmap? {
        val cached = memoryCache.get(key)
        if (cached != null) return cached

        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) {
                val imgBmp = bmp.asImageBitmap()
                memoryCache.put(key, imgBmp)
                imgBmp
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
