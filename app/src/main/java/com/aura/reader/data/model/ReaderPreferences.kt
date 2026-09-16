package com.aura.reader.data.model

import androidx.compose.runtime.Immutable

enum class ReaderThemeMode {
    LIGHT,          // Crisp Clean White
    DARK,           // Modern Dark Slate
    AMOLED,         // Pure Pitch Black
    SEPIA,          // Soft Warm Paper
    SYSTEM_DYNAMIC  // Legacy fallback
}

enum class ReaderFontFamily {
    SYSTEM_DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE
}

enum class PageTurnAnimation {
    SLIDE,
    INSTANT,
    CURL,
    REALISTIC_CURL
}

enum class TwoColumnMode {
    AUTO,
    OFF,
    ALWAYS
}

@Immutable
data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.5f,
    val themeMode: ReaderThemeMode = ReaderThemeMode.SYSTEM_DYNAMIC,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SERIF,
    val keepScreenOn: Boolean = true,
    val lightImageBackground: Boolean = true,
    val pagingMode: Boolean = false,
    val autoHyphenation: Boolean = true,
    val twoColumnMode: TwoColumnMode = TwoColumnMode.AUTO,
    val pageAnimation: PageTurnAnimation = PageTurnAnimation.SLIDE,
    val hapticFeedbackEnabled: Boolean = true,
    val fontName: String = "",
    val syncThemesWithApp: Boolean = false,
    val appThemeMode: ReaderThemeMode = ReaderThemeMode.SYSTEM_DYNAMIC
)
