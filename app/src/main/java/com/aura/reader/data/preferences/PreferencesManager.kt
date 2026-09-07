package com.aura.reader.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aura.reader.data.model.ReaderFontFamily
import com.aura.reader.data.model.ReaderSettings
import com.aura.reader.data.model.ReaderThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reader_preferences")

class PreferencesManager(private val context: Context) {

    companion object {
        val FONT_SIZE_KEY = floatPreferencesKey("font_size_sp")
        val LINE_HEIGHT_KEY = floatPreferencesKey("line_height_multiplier")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val FONT_FAMILY_KEY = stringPreferencesKey("font_family")
        val KEEP_SCREEN_ON_KEY = booleanPreferencesKey("keep_screen_on")
        val LIGHT_IMAGE_BG_KEY = booleanPreferencesKey("light_image_background")
        val RECENT_BOOKS_KEY = stringPreferencesKey("recent_books_json")
    }

    val readerSettings: Flow<ReaderSettings> = context.dataStore.data.map { prefs ->
        val fontSize = prefs[FONT_SIZE_KEY] ?: 18f
        val lineHeight = prefs[LINE_HEIGHT_KEY] ?: 1.5f
        val themeModeStr = prefs[THEME_MODE_KEY] ?: ReaderThemeMode.SYSTEM_DYNAMIC.name
        val fontFamilyStr = prefs[FONT_FAMILY_KEY] ?: ReaderFontFamily.SERIF.name
        val keepScreenOn = prefs[KEEP_SCREEN_ON_KEY] ?: true
        val lightImageBackground = prefs[LIGHT_IMAGE_BG_KEY] ?: true

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
            lightImageBackground = lightImageBackground
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

    val recentBooksJson: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[RECENT_BOOKS_KEY]
    }

    suspend fun saveRecentBooksJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[RECENT_BOOKS_KEY] = json
        }
    }
}
