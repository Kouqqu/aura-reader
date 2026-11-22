package com.aura.reader.data.model

import androidx.compose.runtime.Immutable
import com.aura.reader.ui.theme.AppLanguage

/**
 * Unified domain model for all application-wide settings and preferences.
 * Replaces loose multi-file prop-drilling with a single immutable state and update seam.
 */
@Immutable
data class AppSettings(
    val themeMode: ReaderThemeMode = ReaderThemeMode.SYSTEM_DYNAMIC,
    val language: AppLanguage = AppLanguage.RU,
    val appDesignStyle: AppDesignStyle = AppDesignStyle.LIQUID_GLASS,
    val glassClarity: Float = 0.85f,
    val appFont: String = "GOOGLE_SANS",
    val updateNotificationsEnabled: Boolean = true,
    val anonymousAnalyticsEnabled: Boolean = false,
    val readingStatsEnabled: Boolean = true,
    val materialYouEnabled: Boolean = false,
    val colorPreset: String = "PURPLE",
    val seasonalEffectsEnabled: Boolean = true,
    val holidayOverride: String = "AUTO",
    val customOpdsEnabled: Boolean = false,
    val customOpdsUrl: String = "",
    val developerModeEnabled: Boolean = false,
    val updateChannel: String = "RELEASE",
    val autoFetchMetadataEnabled: Boolean = true,
    val libraryViewMode: String = "LIST",
    val eInkModeEnabled: Boolean = false
)
