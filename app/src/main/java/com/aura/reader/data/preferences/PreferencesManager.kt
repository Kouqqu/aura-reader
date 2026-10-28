package com.aura.reader.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.reader.data.model.Bookmark
import com.aura.reader.data.model.PageTurnAnimation
import com.aura.reader.data.model.Quote
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.AppDesignStyle
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
    internal val libraryMutex = kotlinx.coroutines.sync.Mutex()


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
        val FONT_NAME_KEY = stringPreferencesKey("font_name")
        val SYNC_THEMES_WITH_APP_KEY = booleanPreferencesKey("sync_themes_with_app")
        val APP_THEME_MODE_KEY = stringPreferencesKey("app_theme_mode")
        val RECENT_BOOKS_KEY = stringPreferencesKey("recent_books_json")
        val READING_STATS_KEY = stringPreferencesKey("reading_stats_json")
        val BOOKMARKS_KEY = stringPreferencesKey("bookmarks_json")
        val QUOTES_KEY = stringPreferencesKey("quotes_json")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val UPDATE_NOTIFICATIONS_KEY = booleanPreferencesKey("update_notifications_enabled")
        val READING_STATS_ENABLED_KEY = booleanPreferencesKey("reading_stats_enabled")
        val MATERIAL_YOU_ENABLED_KEY = booleanPreferencesKey("material_you_enabled")
        val OPDS_HISTORY_KEY = stringPreferencesKey("opds_search_history")
        val LIBRARY_SEARCH_HISTORY_KEY = stringPreferencesKey("library_search_history")
        val OPDS_BASE_URL_KEY = stringPreferencesKey("opds_base_url")
        val CUSTOM_OPDS_ENABLED_KEY = booleanPreferencesKey("custom_opds_enabled")
        val USER_COLLECTIONS_KEY = stringPreferencesKey("user_collections_json")
        val AVERAGE_WPM_KEY = floatPreferencesKey("user_average_wpm")
        val LIBRARY_VIEW_MODE_KEY = stringPreferencesKey("library_view_mode")
        val DEVELOPER_MODE_KEY = booleanPreferencesKey("developer_mode_enabled")
        val UPDATE_CHANNEL_KEY = stringPreferencesKey("update_channel")
        val VPN_NOTICE_DISMISSED_KEY = booleanPreferencesKey("vpn_notice_dismissed")
        val LIBRARY_SORT_ORDER_KEY = stringPreferencesKey("library_sort_order")
        val CUSTOM_BOOK_ORDER_KEY = stringPreferencesKey("custom_book_order_json")
        val SKIPPED_UPDATE_VERSION_KEY = stringPreferencesKey("skipped_update_version")
        val APP_FONT_KEY = stringPreferencesKey("app_ui_font")
        val SERIF_FONT_NAME_KEY = stringPreferencesKey("serif_font_name")
        val SANS_SERIF_FONT_NAME_KEY = stringPreferencesKey("sans_serif_font_name")
        val MONOSPACE_FONT_NAME_KEY = stringPreferencesKey("monospace_font_name")
        val ANONYMOUS_ANALYTICS_KEY = booleanPreferencesKey("anonymous_analytics_enabled")
        val HAS_TRACKED_FIRST_OPEN_KEY = booleanPreferencesKey("has_tracked_first_open")
        val LAST_TRACKED_VERSION_CODE_KEY = intPreferencesKey("last_tracked_version_code")
        val APP_DESIGN_STYLE_KEY = stringPreferencesKey("app_design_style")
        val GLASS_CLARITY_KEY = floatPreferencesKey("glass_clarity")
        val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
        val TTS_VOICE_NAME_KEY = stringPreferencesKey("tts_selected_voice_name")
        val TTS_SPEED_KEY = floatPreferencesKey("tts_speed")
        val TTS_PITCH_KEY = floatPreferencesKey("tts_pitch")
        val TTS_HIGHLIGHT_DELAY_KEY = intPreferencesKey("tts_highlight_delay_ms")
        val TTS_WORD_HIGHLIGHT_KEY = booleanPreferencesKey("tts_word_highlight_enabled")
        val SEASONAL_EFFECTS_ENABLED_KEY = booleanPreferencesKey("seasonal_effects_enabled")
        val HOLIDAY_OVERRIDE_KEY = stringPreferencesKey("holiday_season_override")
    }

        val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_COMPLETED_ONBOARDING_KEY] ?: false
    }

    suspend fun setHasCompletedOnboarding(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_COMPLETED_ONBOARDING_KEY] = completed
        }
    }

    val ttsSelectedVoiceName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TTS_VOICE_NAME_KEY]
    }

    suspend fun setTtsSelectedVoice(name: String?) {
        context.dataStore.edit { prefs ->
            if (name == null) {
                prefs.remove(TTS_VOICE_NAME_KEY)
            } else {
                prefs[TTS_VOICE_NAME_KEY] = name
            }
        }
    }

    val ttsSpeed: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[TTS_SPEED_KEY] ?: 1.0f
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[TTS_SPEED_KEY] = speed
        }
    }

    val ttsPitch: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[TTS_PITCH_KEY] ?: 1.0f
    }

    suspend fun setTtsPitch(pitch: Float) {
        context.dataStore.edit { prefs ->
            prefs[TTS_PITCH_KEY] = pitch
        }
    }

    val ttsHighlightDelayMs: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[TTS_HIGHLIGHT_DELAY_KEY] ?: -1
    }

    suspend fun setTtsHighlightDelay(delayMs: Int) {
        context.dataStore.edit { prefs ->
            prefs[TTS_HIGHLIGHT_DELAY_KEY] = delayMs
        }
    }

    val ttsWordHighlightEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TTS_WORD_HIGHLIGHT_KEY] ?: true
    }

    suspend fun setTtsWordHighlightEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[TTS_WORD_HIGHLIGHT_KEY] = enabled
        }
    }

    val appFont: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[APP_FONT_KEY] ?: "GOOGLE_SANS"
    }

    suspend fun setAppFont(font: String) {
        context.dataStore.edit { prefs ->
            prefs[APP_FONT_KEY] = font
        }
    }

    fun getCustomFontsDir(): java.io.File = java.io.File(context.filesDir, "custom_fonts").apply { mkdirs() }

    fun getCustomFonts(): List<java.io.File> {
        val dir = getCustomFontsDir()
        return dir.listFiles { file ->
            file.isFile && (file.extension.equals("ttf", ignoreCase = true) || file.extension.equals("otf", ignoreCase = true))
        }?.sortedBy { it.nameWithoutExtension.lowercase() } ?: emptyList()
    }

    suspend fun importCustomFont(uri: android.net.Uri): Result<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            var name = "font_${System.currentTimeMillis()}.ttf"
            context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        val fetched = cursor.getString(nameIdx)
                        if (!fetched.isNullOrBlank()) {
                            name = fetched
                        }
                    }
                }
            }
            val sanitizedName = name.replace(Regex("[^a-zA-Z0-9._\\- ]"), "_")
            val dir = getCustomFontsDir()
            val targetFile = java.io.File(dir, sanitizedName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: error("Cannot open input stream for $uri")

            val tf = android.graphics.Typeface.createFromFile(targetFile)
            if (tf == null) {
                targetFile.delete()
                error("Invalid font file")
            }
            sanitizedName
        }
    }

    suspend fun deleteCustomFont(fontFileName: String): Boolean = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val file = java.io.File(getCustomFontsDir(), fontFileName)
        val deleted = file.delete()
        context.dataStore.edit { prefs ->
            if (prefs[APP_FONT_KEY] == "CUSTOM:$fontFileName") {
                prefs[APP_FONT_KEY] = "GOOGLE_SANS"
            }
        }
        deleted
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

    val anonymousAnalyticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ANONYMOUS_ANALYTICS_KEY] ?: true
    }

    suspend fun setAnonymousAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[ANONYMOUS_ANALYTICS_KEY] = enabled
        }
    }

    val hasTrackedFirstOpen: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAS_TRACKED_FIRST_OPEN_KEY] ?: false
    }

    suspend fun setHasTrackedFirstOpen(tracked: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[HAS_TRACKED_FIRST_OPEN_KEY] = tracked
        }
    }

    val lastTrackedVersionCode: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[LAST_TRACKED_VERSION_CODE_KEY]
    }

    suspend fun setLastTrackedVersionCode(versionCode: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_TRACKED_VERSION_CODE_KEY] = versionCode
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

        val fontName = prefs[FONT_NAME_KEY] ?: ""
        val serifFontName = prefs[SERIF_FONT_NAME_KEY] ?: ""
        val sansSerifFontName = prefs[SANS_SERIF_FONT_NAME_KEY] ?: ""
        val monospaceFontName = prefs[MONOSPACE_FONT_NAME_KEY] ?: ""
        val syncThemesWithApp = prefs[SYNC_THEMES_WITH_APP_KEY] ?: false
        val appThemeModeStr = prefs[APP_THEME_MODE_KEY] ?: ReaderThemeMode.SYSTEM_DYNAMIC.name

        val appDesignStyleStr = prefs[APP_DESIGN_STYLE_KEY] ?: AppDesignStyle.LIQUID_GLASS.name
        val appDesignStyle = try {
            AppDesignStyle.valueOf(appDesignStyleStr)
        } catch (e: Exception) {
            AppDesignStyle.LIQUID_GLASS
        }

        val appThemeMode = try {
            ReaderThemeMode.valueOf(appThemeModeStr)
        } catch (e: Exception) {
            ReaderThemeMode.SYSTEM_DYNAMIC
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
            hapticFeedbackEnabled = hapticFeedbackEnabled,
            fontName = fontName,
            serifFontName = serifFontName,
            sansSerifFontName = sansSerifFontName,
            monospaceFontName = monospaceFontName,
            syncThemesWithApp = syncThemesWithApp,
            appThemeMode = appThemeMode,
            appDesignStyle = appDesignStyle,
            glassClarity = prefs[GLASS_CLARITY_KEY] ?: 0.85f
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
            if (prefs[SYNC_THEMES_WITH_APP_KEY] == true) {
                prefs[APP_THEME_MODE_KEY] = themeMode.name
            }
        }
    }

    suspend fun setAppDesignStyle(style: AppDesignStyle) {
        context.dataStore.edit { prefs ->
            prefs[APP_DESIGN_STYLE_KEY] = style.name
        }
    }

    suspend fun setGlassClarity(clarity: Float) {
        context.dataStore.edit { prefs ->
            prefs[GLASS_CLARITY_KEY] = clarity.coerceIn(0.2f, 1.0f)
        }
    }

    suspend fun updateAppThemeMode(themeMode: ReaderThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[APP_THEME_MODE_KEY] = themeMode.name
            if (prefs[SYNC_THEMES_WITH_APP_KEY] == true) {
                prefs[THEME_MODE_KEY] = themeMode.name
            }
        }
    }

    suspend fun updateSyncThemesWithApp(sync: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SYNC_THEMES_WITH_APP_KEY] = sync
            if (sync) {
                val currentReader = prefs[THEME_MODE_KEY] ?: ReaderThemeMode.SYSTEM_DYNAMIC.name
                prefs[APP_THEME_MODE_KEY] = currentReader
            }
        }
    }

    suspend fun updateFontName(fontName: String) {
        context.dataStore.edit { prefs ->
            prefs[FONT_NAME_KEY] = fontName
            val currentFamilyStr = prefs[FONT_FAMILY_KEY] ?: ReaderFontFamily.SERIF.name
            val currentFamily = try {
                ReaderFontFamily.valueOf(currentFamilyStr)
            } catch (e: Exception) {
                ReaderFontFamily.SERIF
            }
            when (currentFamily) {
                ReaderFontFamily.SERIF -> prefs[SERIF_FONT_NAME_KEY] = fontName
                ReaderFontFamily.SANS_SERIF -> prefs[SANS_SERIF_FONT_NAME_KEY] = fontName
                ReaderFontFamily.MONOSPACE -> prefs[MONOSPACE_FONT_NAME_KEY] = fontName
                ReaderFontFamily.SYSTEM_DEFAULT -> {}
            }
        }
    }

    suspend fun updateFontFamily(fontFamily: ReaderFontFamily) {
        context.dataStore.edit { prefs ->
            prefs[FONT_FAMILY_KEY] = fontFamily.name
            val rememberedFont = when (fontFamily) {
                ReaderFontFamily.SERIF -> prefs[SERIF_FONT_NAME_KEY] ?: ""
                ReaderFontFamily.SANS_SERIF -> prefs[SANS_SERIF_FONT_NAME_KEY] ?: ""
                ReaderFontFamily.MONOSPACE -> prefs[MONOSPACE_FONT_NAME_KEY] ?: ""
                ReaderFontFamily.SYSTEM_DEFAULT -> ""
            }
            prefs[FONT_NAME_KEY] = rememberedFont
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

    suspend fun resetReadingStats() {
        context.dataStore.edit { prefs ->
            prefs.remove(READING_STATS_KEY)
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

    suspend fun resetAverageWpm() {
        context.dataStore.edit { prefs ->
            prefs[AVERAGE_WPM_KEY] = 200f
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

    /** Apply a validated backup in one atomic edit. Existing books are retained. */
    internal suspend fun restoreBackupMetadata(manifest: JSONObject) {
        context.dataStore.edit { prefs ->
            fun merge(current: String?, incoming: JSONArray): String {
                val byId = linkedMapOf<String, JSONObject>()
                val old = JSONArray(current ?: "[]")
                for (i in 0 until old.length()) {
                    val item = old.getJSONObject(i)
                    byId[item.getString("id")] = item
                }
                for (i in 0 until incoming.length()) {
                    val item = incoming.getJSONObject(i)
                    byId[item.getString("id")] = item
                }
                return JSONArray(byId.values.toList()).toString()
            }
            prefs[RECENT_BOOKS_KEY] = merge(prefs[RECENT_BOOKS_KEY], JSONArray(manifest.getString("recentBooksJson")))
            manifest.optJSONArray("bookmarks")?.let { prefs[BOOKMARKS_KEY] = merge(prefs[BOOKMARKS_KEY], it) }
            manifest.optJSONArray("quotes")?.let { prefs[QUOTES_KEY] = merge(prefs[QUOTES_KEY], it) }
            manifest.optJSONArray("userCollections")?.let { incoming ->
                val names = linkedSetOf<String>()
                val old = JSONArray(prefs[USER_COLLECTIONS_KEY] ?: "[]")
                for (i in 0 until old.length()) names.add(old.getString(i))
                for (i in 0 until incoming.length()) names.add(incoming.getString(i))
                prefs[USER_COLLECTIONS_KEY] = JSONArray(names.toList()).toString()
            }
            manifest.optJSONObject("settings")?.let { settings ->
                if (settings.has("fontSize")) prefs[FONT_SIZE_KEY] = settings.getDouble("fontSize").toFloat()
                if (settings.has("lineHeight")) prefs[LINE_HEIGHT_KEY] = settings.getDouble("lineHeight").toFloat()
                if (settings.has("themeMode")) prefs[THEME_MODE_KEY] = settings.getString("themeMode")
                if (settings.has("fontFamily")) prefs[FONT_FAMILY_KEY] = settings.getString("fontFamily")
                if (settings.has("keepScreenOn")) prefs[KEEP_SCREEN_ON_KEY] = settings.getBoolean("keepScreenOn")
                if (settings.has("pagingMode")) prefs[PAGING_MODE_KEY] = settings.getBoolean("pagingMode")
                if (settings.has("statsEnabled")) prefs[READING_STATS_ENABLED_KEY] = settings.getBoolean("statsEnabled")
                if (settings.has("materialYou")) prefs[MATERIAL_YOU_ENABLED_KEY] = settings.getBoolean("materialYou")
                if (settings.has("updateNotifications")) prefs[UPDATE_NOTIFICATIONS_KEY] = settings.getBoolean("updateNotifications")
                if (settings.has("catalogUrl")) prefs[OPDS_BASE_URL_KEY] = settings.getString("catalogUrl")
                if (settings.has("appLanguage")) prefs[APP_LANGUAGE_KEY] = settings.getString("appLanguage")
            }
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

    val librarySearchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[LIBRARY_SEARCH_HISTORY_KEY] ?: return@map emptyList()
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveLibrarySearchQuery(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        context.dataStore.edit { prefs ->
            val raw = prefs[LIBRARY_SEARCH_HISTORY_KEY] ?: "[]"
            val current = try {
                val arr = JSONArray(raw)
                val list = mutableListOf<String>()
                for (i in 0 until arr.length()) list.add(arr.getString(i))
                list
            } catch (e: Exception) {
                mutableListOf<String>()
            }
            current.remove(q)
            current.add(0, q)
            val trimmed = current.take(10)
            val newArr = JSONArray()
            for (item in trimmed) newArr.put(item)
            prefs[LIBRARY_SEARCH_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun removeLibrarySearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[LIBRARY_SEARCH_HISTORY_KEY] ?: "[]"
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
            prefs[LIBRARY_SEARCH_HISTORY_KEY] = newArr.toString()
        }
    }

    suspend fun clearLibrarySearchHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(LIBRARY_SEARCH_HISTORY_KEY)
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

    // --- Developer Mode & Beta Channel ---
    val developerModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[DEVELOPER_MODE_KEY] ?: false
    }

    suspend fun setDeveloperModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DEVELOPER_MODE_KEY] = enabled
        }
    }

    val updateChannel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[UPDATE_CHANNEL_KEY] ?: "RELEASE"
    }

    suspend fun setUpdateChannel(channel: String) {
        context.dataStore.edit { prefs ->
            prefs[UPDATE_CHANNEL_KEY] = channel
        }
    }

    // --- Holiday & Seasonal Events ---
    val seasonalEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SEASONAL_EFFECTS_ENABLED_KEY] ?: true
    }

    suspend fun setSeasonalEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SEASONAL_EFFECTS_ENABLED_KEY] = enabled
        }
    }

    val holidayOverride: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[HOLIDAY_OVERRIDE_KEY] ?: "AUTO"
    }

    suspend fun setHolidayOverride(override: String) {
        context.dataStore.edit { prefs ->
            prefs[HOLIDAY_OVERRIDE_KEY] = override
        }
    }

    val skippedUpdateVersion: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[SKIPPED_UPDATE_VERSION_KEY] ?: ""
    }

    suspend fun setSkippedUpdateVersion(version: String) {
        context.dataStore.edit { prefs ->
            prefs[SKIPPED_UPDATE_VERSION_KEY] = version
        }
    }

    // --- VPN Notice ---
    val vpnNoticeDismissed: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[VPN_NOTICE_DISMISSED_KEY] ?: false
    }

    suspend fun setVpnNoticeDismissed(dismissed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[VPN_NOTICE_DISMISSED_KEY] = dismissed
        }
    }

    // --- Library Sorting & Custom Order ---
    val librarySortOrder: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LIBRARY_SORT_ORDER_KEY] ?: "RECENT"
    }

    suspend fun setLibrarySortOrder(order: String) {
        context.dataStore.edit { prefs ->
            prefs[LIBRARY_SORT_ORDER_KEY] = order
        }
    }

    val customBookOrder: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[CUSTOM_BOOK_ORDER_KEY] ?: "[]"
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until arr.length()) list.add(arr.getString(i))
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCustomBookOrder(order: List<String>) {
        context.dataStore.edit { prefs ->
            val arr = JSONArray()
            for (id in order) arr.put(id)
            prefs[CUSTOM_BOOK_ORDER_KEY] = arr.toString()
        }
    }
}
