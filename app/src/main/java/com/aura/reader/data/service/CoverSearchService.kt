package com.aura.reader.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class OnlineCover(
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val highResUrl: String
)

object CoverSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun searchCovers(query: String): List<OnlineCover> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<OnlineCover>()
        val seenUrls = mutableSetOf<String>()

        // 1. iTunes Books API (extremely fast, high-quality covers, no rate limit)
        try {
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = "https://itunes.apple.com/search?term=$encodedQuery&entity=ebook&limit=25"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:120.0)")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string()
                if (!jsonStr.isNullOrBlank()) {
                    val root = JSONObject(jsonStr)
                    val items = root.optJSONArray("results")
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            val rawArtwork = item.optString("artworkUrl100", "")
                            if (rawArtwork.isNotBlank()) {
                                val highRes = rawArtwork.replace("100x100bb.jpg", "600x600bb.jpg")
                                if (seenUrls.add(highRes)) {
                                    results.add(
                                        OnlineCover(
                                            title = item.optString("trackName", item.optString("collectionName", "")),
                                            author = item.optString("artistName", ""),
                                            thumbnailUrl = rawArtwork,
                                            highResUrl = highRes
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log or ignore iTunes error and proceed to secondary source
        }

        // 2. OpenLibrary Search API (secondary / fallback)
        try {
            val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
            val url = "https://openlibrary.org/search.json?q=$encodedQuery&limit=15"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AuraReader/1.4.0")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonStr = response.body?.string()
                if (!jsonStr.isNullOrBlank()) {
                    val root = JSONObject(jsonStr)
                    val docs = root.optJSONArray("docs")
                    if (docs != null) {
                        for (i in 0 until docs.length()) {
                            val doc = docs.getJSONObject(i)
                            if (doc.has("cover_i") && !doc.isNull("cover_i")) {
                                val coverId = doc.getInt("cover_i")
                                val thumb = "https://covers.openlibrary.org/b/id/$coverId-M.jpg"
                                val highRes = "https://covers.openlibrary.org/b/id/$coverId-L.jpg"
                                if (seenUrls.add(highRes)) {
                                    val authorArr = doc.optJSONArray("author_name")
                                    val author = if (authorArr != null && authorArr.length() > 0) authorArr.getString(0) else ""
                                    results.add(
                                        OnlineCover(
                                            title = doc.optString("title", ""),
                                            author = author,
                                            thumbnailUrl = thumb,
                                            highResUrl = highRes
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // OpenLibrary fallback ignored on timeout
        }

        return@withContext results
    }

    suspend fun downloadCoverAsBase64(imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val bytes = response.body?.bytes() ?: return@withContext null
            return@withContext compressBytesToBase64(bytes)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun processUriAsBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            return@withContext compressBytesToBase64(bytes)
        } catch (e: Exception) {
            null
        }
    }

    private fun compressBytesToBase64(rawBytes: ByteArray): String? {
        try {
            // 1. Check dimensions
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

            val maxDim = 1200
            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight

            while (w / sampleSize > maxDim || h / sampleSize > maxDim) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions)
                ?: return null

            val outStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, outStream)
            bitmap.recycle()

            return Base64.encodeToString(outStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            return null
        }
    }
}
