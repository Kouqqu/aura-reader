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
    val highResUrl: String,
    val source: String = ""
)

object CoverSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    fun cleanBookTitle(raw: String): String {
        var t = raw.replace(Regex("""\.(fb2|epub|pdf|txt)(\.zip)?$""", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("""\[(СИ|Самиздат|Серия[^\]]*|Том[^\]]*)\]""", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("""\((СИ|Самиздат|Серия[^\)]*|Том[^\)]*|РОСМЭН|АСТ|Эксмо)\)""", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("""\s+"""), " ").trim(' ', '.', ',', '-', '_', ':', ';')
        return t
    }

    fun cleanAuthorName(raw: String): String {
        var a = raw.replace(Regex("""\s+"""), " ").trim(' ', '.', ',', '-', '_', ':', ';')
        // Strip parenthetical notes from author
        a = a.replace(Regex("""\(.*?\)"""), "").trim()
        return a
    }

    suspend fun searchCovers(
        query: String,
        fallbackTitle: String? = null
    ): List<OnlineCover> = withContext(Dispatchers.IO) {
        val cleanQuery = cleanBookTitle(query)
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val queriesToTry = mutableListOf<String>()
        queriesToTry.add(cleanQuery)

        // If query has multiple words or author, also prepare a shorter title-only fallback query
        if (!fallbackTitle.isNullOrBlank()) {
            val cTitle = cleanBookTitle(fallbackTitle)
            if (cTitle.isNotBlank() && !cTitle.equals(cleanQuery, ignoreCase = true)) {
                queriesToTry.add(cTitle)
            }
        }

        val results = mutableListOf<OnlineCover>()
        val seenUrls = mutableSetOf<String>()

        for (q in queriesToTry) {
            val encodedQuery = URLEncoder.encode(q, "UTF-8")

            // 1. Litres Search API (Best for Russian and translated books)
            try {
                val litresUrl = "https://api.litres.ru/foundation/api/search?q=$encodedQuery&types=text_book&types=audiobook&limit=20"
                val request = Request.Builder()
                    .url(litresUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonStr = response.body?.string()
                    if (!jsonStr.isNullOrBlank()) {
                        val root = JSONObject(jsonStr)
                        val dataArr = root.optJSONObject("payload")?.optJSONArray("data")
                        if (dataArr != null) {
                            for (i in 0 until dataArr.length()) {
                                val item = dataArr.getJSONObject(i)
                                val inst = item.optJSONObject("instance") ?: item
                                val coverUrl = inst.optString("cover_url", "")
                                if (coverUrl.isNotBlank() && coverUrl.endsWith(".jpg", ignoreCase = true) || coverUrl.endsWith(".png", ignoreCase = true)) {
                                    val fullUrl = "https://cv9.litres.ru$coverUrl"
                                    val thumbUrl = "https://cv9.litres.ru${coverUrl.replace("/cover/", "/cover_330/")}"
                                    if (seenUrls.add(fullUrl)) {
                                        val title = inst.optString("title", "")
                                        val personsArr = inst.optJSONArray("persons")
                                        val authorList = mutableListOf<String>()
                                        if (personsArr != null) {
                                            for (pIdx in 0 until personsArr.length()) {
                                                val p = personsArr.getJSONObject(pIdx)
                                                if (p.optString("role") == "author") {
                                                    authorList.add(p.optString("full_name"))
                                                }
                                            }
                                        }
                                        results.add(
                                            OnlineCover(
                                                title = title,
                                                author = authorList.joinToString(", "),
                                                thumbnailUrl = thumbUrl,
                                                highResUrl = fullUrl,
                                                source = "ЛитРес"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}

            // 2. FantLab API (Best for fantasy, sci-fi, classics, niche editions)
            try {
                val fantlabUrl = "https://api.fantlab.ru/search-works?q=$encodedQuery"
                val request = Request.Builder()
                    .url(fantlabUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val jsonStr = response.body?.string()
                    if (!jsonStr.isNullOrBlank()) {
                        val root = JSONObject(jsonStr)
                        val matches = root.optJSONArray("matches")
                        if (matches != null) {
                            for (i in 0 until matches.length()) {
                                val match = matches.getJSONObject(i)
                                val picId = match.optLong("pic_edition_id_auto", 0L).takeIf { it > 0 }
                                    ?: match.optLong("pic_edition_id", 0L).takeIf { it > 0 }
                                if (picId != null && picId > 0) {
                                    val fullUrl = "https://data.fantlab.ru/images/editions/big/$picId"
                                    val thumbUrl = "https://data.fantlab.ru/images/editions/small/$picId"
                                    if (seenUrls.add(fullUrl)) {
                                        val title = match.optString("rusname", match.optString("name", ""))
                                        val author = match.optString("autor_rusname", match.optString("all_autor_rusname", ""))
                                        results.add(
                                            OnlineCover(
                                                title = title,
                                                author = author,
                                                thumbnailUrl = thumbUrl,
                                                highResUrl = fullUrl,
                                                source = "ФантЛаб"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}

            // 3. iTunes Books API (International & modern books)
            try {
                val itunesUrl = "https://itunes.apple.com/search?term=$encodedQuery&entity=ebook&limit=15"
                val request = Request.Builder()
                    .url(itunesUrl)
                    .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
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
                                                highResUrl = highRes,
                                                source = "Apple"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}

            // 4. OpenLibrary API
            try {
                val olUrl = "https://openlibrary.org/search.json?q=$encodedQuery&limit=10"
                val request = Request.Builder()
                    .url(olUrl)
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
                                                highResUrl = highRes,
                                                source = "OpenLibrary"
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {}

            // If we found enough results from first query, don't need second query
            if (results.size >= 8) {
                break
            }
        }

        return@withContext results
    }

    suspend fun downloadCoverAsBase64(imageUrl: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
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
