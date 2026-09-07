package com.aura.reader.data.model

enum class ReaderThemeMode {
    SYSTEM_DYNAMIC, // Material You Dynamic Colors
    LIGHT,          // Crisp Clean White
    SEPIA,          // Soft Warm Paper
    AMOLED          // Pure Pitch Black
}

enum class ReaderFontFamily {
    SYSTEM_DEFAULT,
    SERIF,
    SANS_SERIF,
    MONOSPACE
}

data class ReaderSettings(
    val fontSizeSp: Float = 18f,
    val lineHeightMultiplier: Float = 1.5f,
    val themeMode: ReaderThemeMode = ReaderThemeMode.SYSTEM_DYNAMIC,
    val fontFamily: ReaderFontFamily = ReaderFontFamily.SERIF,
    val keepScreenOn: Boolean = true,
    val lightImageBackground: Boolean = true,
    val pagingMode: Boolean = false
)
