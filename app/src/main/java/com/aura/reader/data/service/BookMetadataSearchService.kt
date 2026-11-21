package com.aura.reader.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class OnlineBookMetadata(
    val title: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genres: List<String> = emptyList(),
    val series: String? = null,
    val seriesNumber: Int? = null,
    val source: String = ""
)

object BookMetadataSearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(7, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMetadata(
        title: String,
        author: String? = null
    ): OnlineBookMetadata? = withContext(Dispatchers.IO) {
        val cleanTitle = CoverSearchService.cleanBookTitle(title)
        val cleanAuthor = author?.let { CoverSearchService.cleanAuthorName(it) }.orEmpty()

        if (cleanTitle.isBlank()) return@withContext null

        val queries = mutableListOf<String>()
        if (cleanAuthor.isNotBlank()) {
            queries.add("$cleanTitle $cleanAuthor")
        }
        queries.add(cleanTitle)

        // 1. Try LitRes first (best for Russian and translated books)
        for (q in queries) {
            val litresMeta = fetchLitRes(q)
            if (litresMeta != null && (!litresMeta.description.isNullOrBlank() || litresMeta.genres.isNotEmpty())) {
                return@withContext litresMeta
            }
        }

        // 2. Try FantLab next (sci-fi, fantasy, series, classics)
        for (q in queries) {
            val fantlabMeta = fetchFantLab(q)
            if (fantlabMeta != null && (!fantlabMeta.description.isNullOrBlank() || fantlabMeta.genres.isNotEmpty())) {
                return@withContext fantlabMeta
            }
        }

        // 3. Try OpenLibrary next (international books)
        for (q in queries) {
            val olMeta = fetchOpenLibrary(q)
            if (olMeta != null && (!olMeta.description.isNullOrBlank() || olMeta.genres.isNotEmpty())) {
                return@withContext olMeta
            }
        }

        return@withContext null
    }

    private fun fetchLitRes(query: String): OnlineBookMetadata? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://api.litres.ru/foundation/api/search?q=$encoded&types=text_book&limit=3"
            val req = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val root = JSONObject(body)
            val dataArr = root.optJSONObject("payload")?.optJSONArray("data") ?: return null
            if (dataArr.length() == 0) return null

            val firstItem = dataArr.getJSONObject(0)
            val inst = firstItem.optJSONObject("instance") ?: firstItem
            val artId = inst.optLong("id", 0L)
            if (artId <= 0L) return null

            val artUrl = "https://api.litres.ru/foundation/api/arts/$artId"
            val artReq = Request.Builder()
                .url(artUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val artResp = client.newCall(artReq).execute()
            if (!artResp.isSuccessful) return null
            val artBody = artResp.body?.string() ?: return null
            val artRoot = JSONObject(artBody)
            val artData = artRoot.optJSONObject("payload")?.optJSONObject("data") ?: return null

            // Extract genres
            val genresList = mutableListOf<String>()
            val genresArr = artData.optJSONArray("genres")
            if (genresArr != null) {
                for (i in 0 until genresArr.length()) {
                    val gObj = genresArr.getJSONObject(i)
                    val gName = gObj.optString("name", "").trim()
                    if (gName.isNotBlank()) {
                        genresList.add(gName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                    }
                }
            }

            // Extract description / annotation
            val rawHtml = artData.optString("html_annotation", "")
            val cleanDesc = if (rawHtml.isNotBlank()) {
                cleanAnnotationText(rawHtml)
            } else null

            // Series
            val seriesObj = artData.optJSONObject("series")
            val seriesName = seriesObj?.optString("name", "")?.takeIf { it.isNotBlank() }

            OnlineBookMetadata(
                title = inst.optString("title", "").takeIf { it.isNotBlank() },
                description = cleanDesc,
                genres = genresList.distinct(),
                series = seriesName,
                source = "ЛитРес"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchFantLab(query: String): OnlineBookMetadata? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://api.fantlab.ru/search-works?q=$encoded"
            val req = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val root = JSONObject(body)
            val matches = root.optJSONArray("matches") ?: return null
            if (matches.length() == 0) return null

            val firstMatch = matches.getJSONObject(0)
            val workId = firstMatch.optLong("work_id", 0L)
            if (workId <= 0L) return null

            val workUrl = "https://api.fantlab.ru/work/$workId"
            val workReq = Request.Builder()
                .url(workUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val workResp = client.newCall(workReq).execute()
            if (!workResp.isSuccessful) return null
            val workBody = workResp.body?.string() ?: return null
            val workRoot = JSONObject(workBody)

            val rawDesc = workRoot.optString("work_description", "")
            val cleanDesc = if (rawDesc.isNotBlank()) {
                cleanAnnotationText(rawDesc)
            } else null

            val genresList = mutableListOf<String>()
            val workType = workRoot.optString("work_type", "").trim()
            if (workType.isNotBlank() && !workType.equals("произведение", ignoreCase = true)) {
                genresList.add(workType.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
            }

            val seriesName = workRoot.optString("work_saga", "").takeIf { it.isNotBlank() }
                ?: workRoot.optString("work_root_saga", "").takeIf { it.isNotBlank() }

            OnlineBookMetadata(
                title = workRoot.optString("work_name", "").takeIf { it.isNotBlank() },
                description = cleanDesc,
                genres = genresList.distinct(),
                series = seriesName,
                source = "ФантЛаб"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun fetchOpenLibrary(query: String): OnlineBookMetadata? {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://openlibrary.org/search.json?q=$encoded&limit=2"
            val req = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "AuraReader/2.3.0")
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) return null
            val body = resp.body?.string() ?: return null
            val root = JSONObject(body)
            val docs = root.optJSONArray("docs") ?: return null
            if (docs.length() == 0) return null

            val doc = docs.getJSONObject(0)
            val workKey = doc.optString("key", "")

            val genresList = mutableListOf<String>()
            val subjectsArr = doc.optJSONArray("subject")
            if (subjectsArr != null) {
                for (i in 0 until minOf(subjectsArr.length(), 6)) {
                    val s = subjectsArr.optString(i, "").trim()
                    if (s.isNotBlank() && s.length < 30) {
                        genresList.add(s.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
                    }
                }
            }

            var desc: String? = null
            if (workKey.isNotBlank()) {
                val workUrl = "https://openlibrary.org$workKey.json"
                val wReq = Request.Builder()
                    .url(workUrl)
                    .header("User-Agent", "AuraReader/2.3.0")
                    .build()
                val wResp = client.newCall(wReq).execute()
                if (wResp.isSuccessful) {
                    val wBody = wResp.body?.string()
                    if (!wBody.isNullOrBlank()) {
                        val wRoot = JSONObject(wBody)
                        val dVal = wRoot.opt("description")
                        if (dVal is String && dVal.isNotBlank()) {
                            desc = cleanAnnotationText(dVal)
                        } else if (dVal is JSONObject) {
                            val v = dVal.optString("value", "")
                            if (v.isNotBlank()) desc = cleanAnnotationText(v)
                        }
                    }
                }
            }

            OnlineBookMetadata(
                title = doc.optString("title", "").takeIf { it.isNotBlank() },
                description = desc,
                genres = genresList.distinct(),
                source = "OpenLibrary"
            )
        } catch (e: Exception) {
            null
        }
    }

    fun cleanAnnotationText(raw: String): String {
        return try {
            val doc = Jsoup.parse(raw)
            // Replace paragraph breaks and line breaks with explicit newlines before extracting text
            doc.select("p").prepend("\\n\\n")
            doc.select("br").append("\\n")
            val fullText = doc.text().replace("\\n", "\n")

            val rawParagraphs = fullText.split(Regex("""\n{2,}"""))
                .map { it.trim() }
                .filter { it.isNotBlank() }

            val cleanedParagraphs = mutableListOf<String>()

            for (p in rawParagraphs) {
                // If paragraph is entirely a known legal/service/marketing disclaimer, drop only this paragraph
                if (isNoiseParagraph(p)) {
                    continue
                }

                var cleanedP = p
                // Strip known inline legal warning prefixes if present at the start of legitimate text
                cleanedP = cleanedP.replace(Regex("""^НЕЗАКОННОЕ\s+ПОТРЕБЛЕНИЕ\s+НАРКОТИЧЕСКИХ\s+СРЕДСТВ[^\.]*\.\s*""", RegexOption.IGNORE_CASE), "")
                cleanedP = cleanedP.replace(Regex("""^Содержит\s+нецензурную\s+брань[^\.]*\.\s*""", RegexOption.IGNORE_CASE), "")
                cleanedP = cleanedP.replace(Regex("""^Возрастное\s+ограничение:\s*\d+\+\.?\s*""", RegexOption.IGNORE_CASE), "")
                cleanedP = cleanedP.trim()

                if (cleanedP.isNotBlank() && !isNoiseParagraph(cleanedP)) {
                    cleanedParagraphs.add(cleanedP)
                }
            }

            cleanedParagraphs.joinToString("\n\n").trim()
        } catch (e: Exception) {
            raw.replace(Regex("<[^>]*>"), " ").trim()
        }
    }

    private fun isNoiseParagraph(p: String): Boolean {
        val lower = p.lowercase()
        return lower.contains("администрация сайта литрес не несет ответственности") ||
                lower.contains("могут иметься медицинские противопоказания") ||
                (lower.contains("необходима консультация специалиста") && lower.length < 150) ||
                lower.contains("в формате pdf a4 сохранен издательский макет") ||
                lower.contains("изображения для книги и обложка были сгенерированы") ||
                lower.contains("публикуется в авторской редакции с сохранением") ||
                lower.contains("читайте в приложении литрес") ||
                lower.contains("в нашей библиотеке вы можете скачать") ||
                lower.contains("купить и скачать электронную книгу") ||
                (lower.startsWith("возрастное ограничение:") && lower.length < 40) ||
                (lower.startsWith("по промокоду") && lower.length < 80) ||
                (lower.contains("незаконное потребление наркотических средств") && lower.length < 150) ||
                (lower.contains("содержит нецензурную брань") && lower.length < 100)
    }
}
