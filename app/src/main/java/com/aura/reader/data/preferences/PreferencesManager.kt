package com.aura.reader.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.reader.data.model.Bookmark
import com.aura.reader.data.model.Quote
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "reader_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("font_size_sp")
        val LINE_HEIGHT_KEY = floatPreferencesKey("line_height_multiplier")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
        val LIGHT_IMAGE_BG_KEY = booleanPreferencesKey("light_image_background")
        val PAGING_MODE_KEY = booleanPreferencesKey("paging_mode")
        val RECENT_BOOKS_KEY = stringPreferencesKey("recent_books_json")
        val READING_STATS_KEY = stringPreferencesKey("reading_stats_json")
        val BOOKMARKS_KEY = stringPreferencesKey("bookmarks_json")
        val QUOTES_KEY = stringPreferencesKey("quotes_json")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val UPDATE_NOTIFICATIONS_KEY = booleanPreferencesKey("update_notifications_enabled")
        val READING_STATS_ENABLED_KEY = booleanPreferencesKey("reading_stats_enabled")
        val MATERIAL_YOU_ENABLED_KEY = booleanPreferencesKey("material_you_enabled")
        val FLIBUSTA_HISTORY_KEY = stringPreferencesKey("flibusta_search_history")
        val FLIBUSTA_BASE_URL_KEY = stringPreferencesKey("flibusta_base_url")
        val USER_COLLECTIONS_KEY = stringPreferencesKey("user_collections_json")
    }

    val readingStatsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[READING_STATS_ENABLED_KEY] ?: true
    }

    suspend fun setReadingStatsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[READING_STATS_ENABLED_KEY] = enabled
        }
    }

    val materialYouEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[MATERIAL_YOU_ENABLED_KEY] ?: false
    }

    suspend fun setMaterialYouEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[MATERIAL_YOU_ENABLED_KEY] = enabled
        }
    }

    val appLanguage: Flow<com.aura.reader.ui.theme.AppLanguage> = context.dataStore.data.map { prefs ->
        val code = prefs[APP_LANGUAGE_KEY] ?: com.aura.reader.ui.theme.AppLanguage.RU.code
        com.aura.reader.ui.theme.AppLanguage.fromCode(code)
    }

    suspend fun updateAppLanguage(language: com.aura.reader.ui.theme.AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[APP_LANGUAGE_KEY] = language.code
        }
    }

    val updateNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[UPDATE_NOTIFICATIONS_KEY] ?: true
    }

    suspend fun setUpdateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[UPDATE_NOTIFICATIONS_KEY] = enabled
        }
    }

    val readerSettings: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        val fontSize = prefs[FONT_SIZE_KEY] ?: 18f
        val lineHeight = prefs[LINE_HEIGHT_KEY] ?: 1.5f
        val themeModeStr = prefs[THEME_MODE_KEY] ?: ReaderThemeMode.SYSTEM_DYNAMIC.name
        val fontFamilyStr = prefs[FONT_FAMILY_KEY] ?: ReaderFontFamily.SERIF.name
        val keepScreenOn = prefs[KEEP_SCREEN_ON_KEY] ?: true
        val lightImageBackground = prefs[LIGHT_IMAGE_BG_KEY] ?: true
        val pagingMode = prefs[PAGING_MODE_KEY] ?: false

        val themeMode = try {
            ReaderThemeMode.valueOf(themeModeStr)
        } catch (e: Exception) {
            ReaderThemeMode.SYSTEM_DYNAMIC
        }

        val fontFamily = try {
            ReaderFontFamily.valueOf(fontFamilyStr)
        } catch (e: Exception) {
            ReaderFontFamily.SERIF
        }

        ReaderSettings(
            fontSizeSp = fontSize,
            lineHeightMultiplier = lineHeight,
            themeMode = themeMode,
            fontFamily = fontFamily,
            keepScreenOn = keepScreenOn,
            lightImageBackground = lightImageBackground,
            pagingMode = pagingMode
        )
    }

    suspend fun updateFontSize(fontSizeSp: Float) {
        context.dataStore.edit { prefs ->
            prefs[FONT_SIZE_KEY] = fontSizeSp
        }
    }

    suspend fun updateLineHeight(multiplier: Float) {
        context.dataStore.edit { prefs ->
            prefs[LINE_HEIGHT_KEY] = multiplier
        }
    }

    suspend fun updateThemeMode(themeMode: ReaderThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun updateFontFamily(fontFamily: ReaderFontFamily) {
        context.dataStore.edit { prefs ->
            prefs[FONT_FAMILY_KEY] = fontFamily.name
        }
    }

    suspend fun updateLightImageBackground(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[LIGHT_IMAGE_BG_KEY] = enabled
        }
    }

    suspend fun updateKeepScreenOn(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEEP_SCREEN_ON_KEY] = enabled
        }
    }

    suspend fun updatePagingMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PAGING_MODE_KEY] = enabled
        }
    }

    // --- Reading Time Stats ---
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val todayReadingMinutes: Flow<Int> = context.dataStore.data.map { prefs ->
        val json = prefs[READING_STATS_KEY] ?: "{}"
        try {
            val obj = JSONObject(json)
            val today = getTodayDateString()
            val seconds = obj.optLong(today, 0L)
            (seconds / 60).toInt()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun addReadingSeconds(seconds: Long) {
        if (seconds <= 0) return
        val today = getTodayDateString()
        context.dataStore.edit { prefs ->
            if (prefs[READING_STATS_ENABLED_KEY] == false) return@edit
            val json = prefs[READING_STATS_KEY] ?: "{}"
            val obj = try { JSONObject(json) } catch (e: Exception) { JSONObject() }
            val current = obj.optLong(today, 0L)
            obj.put(today, current + seconds)
            prefs[READING_STATS_KEY] = obj.toString()
        }
    }

    // --- Bookmarks Management ---
    val bookmarks: Flow<List<Bookmark>> = context.dataStore.data.map { prefs ->
        val json = prefs[BOOKMARKS_KEY] ?: "[]"
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<Bookmark>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Bookmark(
                        id = o.getString("id"),
                        bookId = o.getString("bookId"),
                        chapterIndex = o.getInt("chapterIndex"),
                        scrollOffset = o.optInt("scrollOffset", 0),
                        chapterTitle = o.getString("chapterTitle"),
                        previewText = o.optString("previewText", ""),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addBookmark(bookmark: Bookmark) {
        context.dataStore.edit { prefs ->
            val json = prefs[BOOKMARKS_KEY] ?: "[]"
            val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
            val o = JSONObject().apply {
                put("id", bookmark.id)
                put("bookId", bookmark.bookId)
                put("chapterIndex", bookmark.chapterIndex)
                put("scrollOffset", bookmark.scrollOffset)
                put("chapterTitle", bookmark.chapterTitle)
                put("previewText", bookmark.previewText)
                put("timestamp", bookmark.timestamp)
            }
            arr.put(o)
            prefs[BOOKMARKS_KEY] = arr.toString()
        }
    }

    suspend fun removeBookmark(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[BOOKMARKS_KEY] ?: "[]"
            val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.getString("id") != id) {
                    newArr.put(o)
                }
            }
            prefs[BOOKMARKS_KEY] = newArr.toString()
        }
    }

    // --- Quotes Management ---
    val quotes: Flow<List<Quote>> = context.dataStore.data.map { prefs ->
        val json = prefs[QUOTES_KEY] ?: "[]"
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<Quote>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    Quote(
                        id = o.getString("id"),
                        bookId = o.getString("bookId"),
                        bookTitle = o.getString("bookTitle"),
                        chapterIndex = o.getInt("chapterIndex"),
                        text = o.getString("text"),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addQuote(quote: Quote) {
        context.dataStore.edit { prefs ->
            val json = prefs[QUOTES_KEY] ?: "[]"
            val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
            val o = JSONObject().apply {
                put("id", quote.id)
                put("bookId", quote.bookId)
                put("bookTitle", quote.bookTitle)
                put("chapterIndex", quote.chapterIndex)
                put("text", quote.text)
                put("timestamp", quote.timestamp)
            }
            arr.put(o)
            prefs[QUOTES_KEY] = arr.toString()
        }
    }

    suspend fun removeQuote(id: String) {
        context.dataStore.edit { prefs ->
            val json = prefs[QUOTES_KEY] ?: "[]"
            val arr = try { JSONArray(json) } catch (e: Exception) { JSONArray() }
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.getString("id") != id) {
                    newArr.put(o)
                }
            }
            prefs[QUOTES_KEY] = newArr.toString()
        }
    }

    // --- Recent Books ---
    val recentBooksJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[RECENT_BOOKS_KEY]
    }

    suspend fun saveRecentBooksJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[RECENT_BOOKS_KEY] = json
        }
    }

    // --- Flibusta OPDS Integration ---
    val flibustaSearchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[FLIBUSTA_HISTORY_KEY] ?: return@map emptyList()
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveFlibustaSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[FLIBUSTA_HISTORY_KEY] ?: "[]"
            val current = try {
                val arr = JSONArray(raw)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                list
            } catch (e: Exception) {
                mutableListOf<String>()
            }
            current.remove(query)
            current.add(0, query)
            val trimmed = current.take(10)
            val newArr = JSONArray()
            for (item in trimmed) newArr.put(item)
            prefs[FLIBUSTA_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun removeFlibustaSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[FLIBUSTA_HISTORY_KEY] ?: "[]"
            val current = try {
                val arr = JSONArray(raw)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                list
            } catch (e: Exception) {
                mutableListOf<String>()
            }
            current.remove(query)
            val newArr = JSONArray()
            for (item in current) newArr.put(item)
            prefs[FLIBUSTA_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun clearFlibustaSearchHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(FLIBUSTA_HISTORY_KEY)
        }
    }

    val flibustaBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[FLIBUSTA_BASE_URL_KEY] ?: "http://flibusta.is/opds"
    }

    suspend fun setFlibustaBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[FLIBUSTA_BASE_URL_KEY] = url.trim()
        }
    }

    // --- User Collections ---
    val userCollections: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[USER_COLLECTIONS_KEY] ?: return@map emptyList()
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveUserCollections(collections: List<String>) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray()
            for (c in collections) arr.put(c)
            prefs[USER_COLLECTIONS_KEY] = arr.toString()
        }
    }

    suspend fun addUserCollection(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[USER_COLLECTIONS_KEY] ?: "[]"
            val arr = try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            if (!list.contains(trimmed)) {
                list.add(trimmed)
                val newArr = JSONArray()
                for (item in list) newArr.put(item)
                prefs[USER_COLLECTIONS_KEY] = newArr.toString()
            }
        }
    }

    suspend fun removeUserCollection(name: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[USER_COLLECTIONS_KEY] ?: "[]"
            val arr = try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
            val newArr = JSONArray()
            for (i in 0 until arr.length()) {
                val item = arr.getString(i)
                if (item != name) newArr.put(item)
            }
            prefs[USER_COLLECTIONS_KEY] = newArr.toString()
        }
    }
}
