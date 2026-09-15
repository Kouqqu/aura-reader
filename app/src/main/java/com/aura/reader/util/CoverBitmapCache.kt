package com.aura.reader.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

object CoverBitmapCache {
    // Cache up to 64 decoded covers in memory
    private val memoryCache = object : LruCache<String, ImageBitmap>(64) {}

    fun getOrDecode(key: String, base64: String, targetWidth: Int = 480, targetHeight: Int = 720): ImageBitmap? {
        val cached = memoryCache.get(key)
        if (cached != null) return cached

        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)

            // Step 1: Read only dimensions without allocating full bitmap
            val boundsOpts = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOpts)

            // Step 2: Compute power-of-two inSampleSize to avoid allocating massive 4K textures
            var sampleSize = 1
            if (boundsOpts.outHeight > targetHeight || boundsOpts.outWidth > targetWidth) {
                val halfHeight = boundsOpts.outHeight / 2
                val halfWidth = boundsOpts.outWidth / 2
                while ((halfHeight / sampleSize) >= targetHeight && (halfWidth / sampleSize) >= targetWidth) {
                    sampleSize *= 2
                }
            }

            // Step 3: Decode with RGB_565 for 50% RAM savings and 2x faster decode speed
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
                inDither = true
            }

            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
            if (bmp != null) {
                val imgBmp = bmp.asImageBitmap()
                memoryCache.put(key, imgBmp)
                imgBmp
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun getOrDecodeFull(key: String, base64: String): ImageBitmap? {
        val fullKey = "${key}_full"
        val cached = memoryCache.get(fullKey)
        if (cached != null) return cached

        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = 1
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inDither = true
            }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOpts)
            if (bmp != null) {
                val imgBmp = bmp.asImageBitmap()
                memoryCache.put(fullKey, imgBmp)
                imgBmp
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
