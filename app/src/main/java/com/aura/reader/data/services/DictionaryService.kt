package com.aura.reader.data.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class WordDefinition(
    val word: String,
    val title: String,
    val extract: String,
    val source: String = "Викисловарь"
)

object DictionaryService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val definitionCache = mutableMapOf<String, WordDefinition>()
    private val translationCache = mutableMapOf<String, String>()

    suspend fun lookupDefinition(word: String): Result<WordDefinition> = withContext(Dispatchers.IO) {
        val cleanWord = word.trim().removeSurrounding("«", "»")
            .removeSurrounding("\"", "\"")
            .removeSurrounding("'", "'")
            .removeSuffix(".").removeSuffix(",").removeSuffix("!").removeSuffix("?")
            .removeSuffix(";").removeSuffix(":")
            .trim()

        if (cleanWord.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Empty word"))
        }

        val cacheKey = cleanWord.lowercase()
        definitionCache[cacheKey]?.let {
            return@withContext Result.success(it)
        }

        // 1. Primary: Wiktionary (Викисловарь) for true explanatory dictionary meanings
        try {
            val encoded = URLEncoder.encode(cleanWord.lowercase(), "UTF-8")
            val url = "https://ru.wiktionary.org/w/api.php?action=query&prop=extracts&explaintext=1&titles=" + encoded + "&format=json"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AuraReader/1.3 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val pages = json.optJSONObject("query")?.optJSONObject("pages")
                        if (pages != null) {
                            for (key in pages.keys()) {
                                if (key != "-1") {
                                    val pageObj = pages.getJSONObject(key)
                                    val extract = pageObj.optString("extract", "")
                                    val parsedMeaning = extractWiktionaryMeanings(extract)
                                    if (!parsedMeaning.isNullOrBlank()) {
                                        val def = WordDefinition(
                                            word = cleanWord,
                                            title = cleanWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                                            extract = parsedMeaning,
                                            source = "Викисловарь"
                                        )
                                        definitionCache[cacheKey] = def
                                        return@withContext Result.success(def)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // 2. Fallback: Russian Wikipedia summary API (for names, terms, historical concepts)
        try {
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = "https://ru.wikipedia.org/api/rest_v1/page/summary/" + encoded
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AuraReader/1.3 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val extract = json.optString("extract", "")
                        val title = json.optString("title", cleanWord)
                        if (extract.isNotBlank() && !extract.contains("может означать:")) {
                            val def = WordDefinition(
                                word = cleanWord,
                                title = title,
                                extract = extract,
                                source = "Википедия"
                            )
                            definitionCache[cacheKey] = def
                            return@withContext Result.success(def)
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        // 3. Fallback: English Wikipedia if word has Latin characters
        try {
            val encoded = URLEncoder.encode(cleanWord, "UTF-8")
            val url = "https://en.wikipedia.org/api/rest_v1/page/summary/" + encoded
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AuraReader/1.3 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val json = JSONObject(body)
                        val extract = json.optString("extract", "")
                        val title = json.optString("title", cleanWord)
                        if (extract.isNotBlank()) {
                            val def = WordDefinition(
                                word = cleanWord,
                                title = title,
                                extract = extract,
                                source = "Wikipedia (EN)"
                            )
                            definitionCache[cacheKey] = def
                            return@withContext Result.success(def)
                        }
                    }
                }
            }
        } catch (_: Exception) { }

        Result.failure(NoSuchElementException("Толкование для «$cleanWord» не найдено"))
    }

    private fun extractWiktionaryMeanings(rawText: String): String? {
        if (rawText.isBlank()) return null
        val pattern = Pattern.compile("==== Значение ====(.*?)(?:====|===|$)", Pattern.DOTALL)
        val matcher = pattern.matcher(rawText)
        if (matcher.find()) {
            val section = matcher.group(1) ?: return null
            val rawLines = section.split("\n")
                .map { it.trim() }
                .filter { it.isNotBlank() && !it.startsWith("?") && !it.startsWith("—") }

            if (rawLines.isNotEmpty()) {
                val cleanLines = mutableListOf<String>()
                for ((idx, line) in rawLines.withIndex()) {
                    val meaningText = line.split("◆")[0].trim()
                    if (meaningText.isNotBlank()) {
                        if (rawLines.size > 1) {
                            cleanLines.add("${idx + 1}. $meaningText")
                        } else {
                            cleanLines.add(meaningText)
                        }
                    }
                }
                if (cleanLines.isNotEmpty()) {
                    return cleanLines.joinToString("\n\n")
                }
            }
        }
        return null
    }

    suspend fun translateText(text: String, targetLang: String = "ru"): Result<String> = withContext(Dispatchers.IO) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Empty text"))
        }

        val cacheKey = "$targetLang:$cleanText"
        translationCache[cacheKey]?.let {
            return@withContext Result.success(it)
        }

        try {
            val encoded = URLEncoder.encode(cleanText, "UTF-8")
            val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLang + "&dt=t&q=" + encoded
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    if (!body.isNullOrBlank()) {
                        val arr = JSONArray(body)
                        val partsArr = arr.optJSONArray(0)
                        if (partsArr != null) {
                            val sb = StringBuilder()
                            for (i in 0 until partsArr.length()) {
                                val item = partsArr.optJSONArray(i)
                                if (item != null && item.length() > 0) {
                                    sb.append(item.optString(0, ""))
                                }
                            }
                            val translated = sb.toString().trim()
                            if (translated.isNotBlank()) {
                                translationCache[cacheKey] = translated
                                return@withContext Result.success(translated)
                            }
                        }
                    }
                }
            }
            Result.failure(IllegalStateException("Не удалось получить перевод"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
