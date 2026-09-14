package com.aura.reader.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.reader.data.model.Bookmark
import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.Quote
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import com.aura.reader.data.model.TwoColumnMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.runtime.Immutable

private val Context.dataStore by preferencesDataStore(name = "reader_preferences")

@Immutable
data class DayReadingStat(
    val dateStr: String,
    val dayOfWeek: Int,
    val minutes: Int,
    val isToday: Boolean
)

@Immutable
data class ReadingStatsData(
    val todayMinutes: Int = 0,
    val totalMinutes: Int = 0,
    val currentStreakDays: Int = 0,
    val dailyAverageMinutes: Int = 0,
    val weeklyStats: List<DayReadingStat> = emptyList()
)

class PreferencesManager(private val context: Context) {

    companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("font_size_sp")
        val LINE_HEIGHT_KEY = floatPreferencesKey("line_height_multiplier")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
        val LIGHT_IMAGE_BG_KEY = booleanPreferencesKey("light_image_background")
        val PAGING_MODE_KEY = booleanPreferencesKey("paging_mode")
        val AUTO_HYPHENATION_KEY = booleanPreferencesKey("auto_hyphenation")
        val TWO_COLUMN_MODE_KEY = stringPreferencesKey("two_column_mode")
        val PAGE_ANIMATION_KEY = stringPreferencesKey("page_animation")
        val HAPTIC_FEEDBACK_KEY = booleanPreferencesKey("haptic_feedback_enabled")
        val RECENT_BOOKS_KEY = stringPreferencesKey("recent_books_json")
        val READING_STATS_KEY = stringPreferencesKey("reading_stats_json")
        val BOOKMARKS_KEY = stringPreferencesKey("bookmarks_json")
        val QUOTES_KEY = stringPreferencesKey("quotes_json")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val UPDATE_NOTIFICATIONS_KEY = booleanPreferencesKey("update_notifications_enabled")
        val READING_STATS_ENABLED_KEY = booleanPreferencesKey("reading_stats_enabled")
        val MATERIAL_YOU_ENABLED_KEY = booleanPreferencesKey("material_you_enabled")
        val OPDS_HISTORY_KEY = stringPreferencesKey("opds_search_history")
        val OPDS_BASE_URL_KEY = stringPreferencesKey("opds_base_url")
        val CUSTOM_OPDS_ENABLED_KEY = booleanPreferencesKey("custom_opds_enabled")
        val USER_COLLECTIONS_KEY = stringPreferencesKey("user_collections_json")
        val AVERAGE_WPM_KEY = floatPreferencesKey("user_average_wpm")
        val LIBRARY_VIEW_MODE_KEY = stringPreferencesKey("library_view_mode")
    }

    val libraryViewMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LIBRARY_VIEW_MODE_KEY] ?: "LIST"
    }

    suspend fun setLibraryViewMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[LIBRARY_VIEW_MODE_KEY] = mode
        }
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
        val autoHyphenation = prefs[AUTO_HYPHENATION_KEY] ?: true
        val twoColumnModeStr = prefs[TWO_COLUMN_MODE_KEY] ?: TwoColumnMode.AUTO.name
        val pageAnimationStr = prefs[PAGE_ANIMATION_KEY] ?: PageTurnAnimation.SLIDE.name
        val hapticFeedbackEnabled = prefs[HAPTIC_FEEDBACK_KEY] ?: true

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

        val twoColumnMode = try {
            TwoColumnMode.valueOf(twoColumnModeStr)
        } catch (e: Exception) {
            TwoColumnMode.AUTO
        }

        val pageAnimation = try {
            PageTurnAnimation.valueOf(pageAnimationStr)
        } catch (e: Exception) {
            PageTurnAnimation.SLIDE
        }

        ReaderSettings(
            fontSizeSp = fontSize,
            lineHeightMultiplier = lineHeight,
            themeMode = themeMode,
            fontFamily = fontFamily,
            keepScreenOn = keepScreenOn,
            lightImageBackground = lightImageBackground,
            pagingMode = pagingMode,
            autoHyphenation = autoHyphenation,
            twoColumnMode = twoColumnMode,
            pageAnimation = pageAnimation,
            hapticFeedbackEnabled = hapticFeedbackEnabled
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

    suspend fun updateAutoHyphenation(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_HYPHENATION_KEY] = enabled
        }
    }

    suspend fun updateTwoColumnMode(mode: TwoColumnMode) {
        context.dataStore.edit { prefs ->
            prefs[TWO_COLUMN_MODE_KEY] = mode.name
        }
    }

    suspend fun updatePageAnimation(animation: PageTurnAnimation) {
        context.dataStore.edit { prefs ->
            prefs[PAGE_ANIMATION_KEY] = animation.name
        }
    }

    suspend fun updateHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAPTIC_FEEDBACK_KEY] = enabled
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

    val readingStatsData: Flow<ReadingStatsData> = context.dataStore.data.map { prefs ->
        val json = prefs[READING_STATS_KEY] ?: "{}"
        try {
            val obj = JSONObject(json)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val todayCal = Calendar.getInstance()
            val todayStr = sdf.format(todayCal.time)
            val todaySeconds = obj.optLong(todayStr, 0L)
            val todayMins = (todaySeconds / 60).toInt()

            var totalSecs = 0L
            var activeDays = 0
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val s = obj.optLong(k, 0L)
                if (s > 0) {
                    totalSecs += s
                    activeDays++
                }
            }
            val totalMins = (totalSecs / 60).toInt()
            val avgMins = if (activeDays > 0) (totalMins / activeDays) else todayMins

            var streak = 0
            val checkCal = Calendar.getInstance()
            val todayHasReading = todaySeconds > 0
            if (!todayHasReading) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                val yestStr = sdf.format(checkCal.time)
                if (obj.optLong(yestStr, 0L) > 0) {
                    streak = 1
                    checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    while (true) {
                        val dStr = sdf.format(checkCal.time)
                        if (obj.optLong(dStr, 0L) > 0) {
                            streak++
                            checkCal.add(Calendar.DAY_OF_YEAR, -1)
                        } else {
                            break
                        }
                    }
                }
            } else {
                streak = 1
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
                while (true) {
                    val dStr = sdf.format(checkCal.time)
                    if (obj.optLong(dStr, 0L) > 0) {
                        streak++
                        checkCal.add(Calendar.DAY_OF_YEAR, -1)
                    } else {
                        break
                    }
                }
            }

            val weekly = mutableListOf<DayReadingStat>()
            for (offset in 6 downTo 0) {
                val dayCal = Calendar.getInstance()
                dayCal.add(Calendar.DAY_OF_YEAR, -offset)
                val dStr = sdf.format(dayCal.time)
                val s = obj.optLong(dStr, 0L)
                weekly.add(
                    DayReadingStat(
                        dateStr = dStr,
                        dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK),
                        minutes = (s / 60).toInt(),
                        isToday = (offset == 0)
                    )
                )
            }

            ReadingStatsData(
                todayMinutes = todayMins,
                totalMinutes = totalMins,
                currentStreakDays = streak,
                dailyAverageMinutes = avgMins,
                weeklyStats = weekly
            )
        } catch (e: Exception) {
            ReadingStatsData()
        }
    }

    val userAverageWpm: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[AVERAGE_WPM_KEY] ?: 200f
    }

    suspend fun updateAverageWpm(wpm: Float) {
        context.dataStore.edit { prefs ->
            prefs[AVERAGE_WPM_KEY] = wpm.coerceIn(80f, 600f)
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
                        timestamp = o.optLong("timestamp", System.currentTimeMillis()),
                        color = o.optLong("color", 0xFFFFF59DL)
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
                put("color", quote.color)
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

    // --- OPDS Catalog Integration ---
    val catalogSearchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[OPDS_HISTORY_KEY] ?: return@map emptyList()
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCatalogSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[OPDS_HISTORY_KEY] ?: "[]"
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
            prefs[OPDS_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun removeCatalogSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[OPDS_HISTORY_KEY] ?: "[]"
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
            prefs[OPDS_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun clearCatalogSearchHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(OPDS_HISTORY_KEY)
        }
    }

    val catalogBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[OPDS_BASE_URL_KEY] ?: com.aura.reader.data.opds.OpdsService.DEFAULT_BASE_URL
    }

    suspend fun setCatalogBaseUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[OPDS_BASE_URL_KEY] = url.trim()
        }
    }

    val customOpdsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_OPDS_ENABLED_KEY] ?: false
    }

    suspend fun setCustomOpdsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CUSTOM_OPDS_ENABLED_KEY] = enabled
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
