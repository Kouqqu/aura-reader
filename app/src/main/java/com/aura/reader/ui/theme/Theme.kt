package com.aura.reader.ui.theme

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.aura.reader.R
import com.aura.reader.data.model.HolidaySeason
import com.aura.reader.data.model.ReaderThemeMode

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = Color(0xFFE2E4EC),
    onSecondaryContainer = Color(0xFF161C24),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F7FA),
    surfaceContainer = Color(0xFFF1F1F5),
    surfaceContainerHigh = Color(0xFFE9E9EF),
    surfaceContainerHighest = Color(0xFFE2E2E8),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6CF)
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = Color(0xFF262626),
    onSecondaryContainer = Color(0xFFEDEDED),
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    surfaceContainerLowest = Color(0xFF0F0F0F),
    surfaceContainerLow = Color(0xFF161616),
    surfaceContainer = Color(0xFF1E1E1E),
    surfaceContainerHigh = Color(0xFF252525),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    outline = Color(0xFF383838),
    outlineVariant = Color(0xFF242424)
)

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = SepiaOnPrimary,
    primaryContainer = Color(0xFFE2D6C4),
    onPrimaryContainer = Color(0xFF2C1E12),
    secondary = SepiaSecondaryText,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE4DACB),
    onSecondaryContainer = Color(0xFF2C1E12),
    tertiary = Color(0xFF6B4E38),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFEADBCE),
    onTertiaryContainer = Color(0xFF2C1E12),
    background = SepiaBackground,
    onBackground = SepiaText,
    surface = SepiaSurface,
    onSurface = SepiaText,
    surfaceVariant = Color(0xFFE6DDD0),
    onSurfaceVariant = SepiaSecondaryText,
    surfaceContainerLowest = Color(0xFFFCF9F3),
    surfaceContainerLow = Color(0xFFF2ECE0),
    surfaceContainer = Color(0xFFECE4D6),
    surfaceContainerHigh = Color(0xFFE6DDCF),
    surfaceContainerHighest = Color(0xFFE0D6C6),
    outline = Color(0xFF8C7E6C),
    outlineVariant = Color(0xFFD4C8B5)
)

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimary,
    onPrimary = AmoledOnPrimary,
    // "Container" roles must stay visible on a black background. Using the
    // AMOLED surface here made selected cards and icon plates disappear.
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = AmoledSecondaryText,
    onSecondary = AmoledOnPrimary,
    background = AmoledBackground,
    onBackground = AmoledText,
    surface = AmoledSurface,
    onSurface = AmoledText,
    surfaceVariant = AmoledSurface,
    onSurfaceVariant = AmoledSecondaryText,
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF121212),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF1C1C1C)
)

private val ReaderDarkBackground = Color(0xFF151515)

/**
 * Reading pages use a neutral dark base instead of the wallpaper-tinted
 * dynamic surface, so the page colour does not shift between books or themes.
 */
private fun ColorScheme.withNeutralReaderSurfaces(background: Color): ColorScheme = copy(
    background = background,
    surface = background,
    surfaceContainerLowest = Color(0xFF0E0E0E),
    surfaceContainerLow = Color(0xFF181818),
    surfaceContainer = Color(0xFF1C1C1C),
    surfaceContainerHigh = Color(0xFF242424),
    surfaceContainerHighest = Color(0xFF2C2C2C)
)

private fun ColorScheme.withAmoledReaderSurfaces(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0A0A0A),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerHigh = Color(0xFF181818),
    surfaceContainerHighest = Color(0xFF202020)
)

@RequiresApi(Build.VERSION_CODES.S)
private fun dynamicDarkAdjusted(context: Context): ColorScheme {
    val dyn = dynamicDarkColorScheme(context)
    return dyn.copy(
        background = Color(0xFF121212),
        surface = Color(0xFF121212),
        surfaceContainerLowest = Color(0xFF0F0F0F),
        surfaceContainerLow = Color(0xFF161616),
        surfaceContainer = Color(0xFF1E1E1E),
        surfaceContainerHigh = Color(0xFF252525),
        surfaceContainerHighest = Color(0xFF2C2C2C)
    )
}

@RequiresApi(Build.VERSION_CODES.S)
private fun dynamicAmoledAdjusted(context: Context): ColorScheme {
    val dyn = dynamicDarkColorScheme(context)
    return dyn.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF080808),
        surfaceContainer = Color(0xFF101010),
        surfaceContainerHigh = Color(0xFF181818),
        surfaceContainerHighest = Color(0xFF202020)
    )
}

val LocalColorPreset = androidx.compose.runtime.compositionLocalOf { "PURPLE" }

@Composable
fun AuraReaderTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.DARK,
    materialYou: Boolean = false,
    colorPreset: String = "PURPLE",
    appFont: String = "GOOGLE_SANS",
    currentLanguage: AppLanguage = AppLanguage.RU,
    readerMode: Boolean = false,
    holidaySeason: HolidaySeason = HolidaySeason.NONE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val hasDynamic = materialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val systemDark = isSystemInDarkTheme()

    val typography = remember(appFont, currentLanguage) {
        val fontFamily = resolveAppFontFamily(context, appFont, currentLanguage)
        createAppTypography(fontFamily)
    }

    val preset = com.aura.reader.data.model.AndroidColorPreset.fromId(colorPreset)
    val baseLight = LightColorScheme.copy(
        primary = preset.lightPrimary,
        primaryContainer = preset.lightPrimaryContainer,
        onPrimaryContainer = preset.lightOnPrimaryContainer,
        secondary = preset.lightSecondary,
        secondaryContainer = preset.lightPrimaryContainer.copy(alpha = 0.45f),
        onSecondaryContainer = preset.lightOnPrimaryContainer
    )
    val baseDark = DarkColorScheme.copy(
        primary = preset.darkPrimary,
        primaryContainer = preset.darkPrimaryContainer,
        onPrimaryContainer = preset.darkOnPrimaryContainer,
        secondary = preset.darkSecondary,
        secondaryContainer = preset.darkPrimaryContainer.copy(alpha = 0.45f),
        onSecondaryContainer = preset.darkOnPrimaryContainer
    )
    val baseAmoled = AmoledColorScheme.copy(
        primary = preset.darkPrimary,
        primaryContainer = preset.darkPrimaryContainer,
        onPrimaryContainer = preset.darkOnPrimaryContainer,
        secondary = preset.darkSecondary,
        secondaryContainer = preset.darkPrimaryContainer.copy(alpha = 0.45f),
        onSecondaryContainer = preset.darkOnPrimaryContainer
    )
    val baseSepia = SepiaColorScheme

    val colorScheme = when (themeMode) {
        ReaderThemeMode.LIGHT -> {
            if (hasDynamic) {
                dynamicLightColorScheme(context)
            } else {
                baseLight
            }
        }
        ReaderThemeMode.DARK -> {
            if (hasDynamic) {
                dynamicDarkAdjusted(context)
            } else {
                baseDark
            }
        }
        ReaderThemeMode.AMOLED -> {
            if (hasDynamic) {
                dynamicAmoledAdjusted(context)
            } else {
                baseAmoled
            }
        }
        ReaderThemeMode.SEPIA -> {
            baseSepia
        }
        ReaderThemeMode.SYSTEM_DYNAMIC -> {
            if (systemDark) {
                if (hasDynamic) dynamicDarkAdjusted(context) else baseDark
            } else {
                if (hasDynamic) dynamicLightColorScheme(context) else baseLight
            }
        }
    }

    val finalColorScheme = if (!readerMode) {
        colorScheme
    } else {
        when (themeMode) {
            ReaderThemeMode.DARK -> colorScheme.withNeutralReaderSurfaces(ReaderDarkBackground)
            ReaderThemeMode.AMOLED -> colorScheme.withAmoledReaderSurfaces()
            ReaderThemeMode.SYSTEM_DYNAMIC ->
                if (systemDark) colorScheme.withNeutralReaderSurfaces(ReaderDarkBackground) else colorScheme
            else -> colorScheme
        }
    }

    val festiveColorScheme = if (holidaySeason != HolidaySeason.NONE && !readerMode) {
        val isDark = when (themeMode) {
            ReaderThemeMode.LIGHT -> false
            ReaderThemeMode.DARK, ReaderThemeMode.AMOLED -> true
            ReaderThemeMode.SEPIA -> false
            ReaderThemeMode.SYSTEM_DYNAMIC -> systemDark
        }
        val festivePrimary = holidaySeason.primaryAccent
        val festiveOnPrimary = if (isDark) Color(0xFF1E0A00) else Color(0xFFFFFFFF)
        val festiveContainer = if (isDark) {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFF2C1608)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFF320E0E)
                HolidaySeason.NEW_YEAR -> Color(0xFF002238)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFF2A2000)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFF002238)
                HolidaySeason.VALENTINE -> Color(0xFF3A0A1C)
                HolidaySeason.EASTER -> Color(0xFF1B2A0F)
                HolidaySeason.NONE -> finalColorScheme.primaryContainer
            }
        } else {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFFFFE0D0)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFFFFDAD6)
                HolidaySeason.NEW_YEAR -> Color(0xFFC7E7FF)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFFFFE082)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFFC7E7FF)
                HolidaySeason.VALENTINE -> Color(0xFFFFD9E2)
                HolidaySeason.EASTER -> Color(0xFFDCEDC8)
                HolidaySeason.NONE -> finalColorScheme.primaryContainer
            }
        }
        val festiveOnContainer = if (isDark) {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFFFFB787)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFFFFDAD6)
                HolidaySeason.NEW_YEAR -> Color(0xFFC7E7FF)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFFFFF8E1)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFFC7E7FF)
                HolidaySeason.VALENTINE -> Color(0xFFFFB1C8)
                HolidaySeason.EASTER -> Color(0xFFC5E1A5)
                HolidaySeason.NONE -> finalColorScheme.onPrimaryContainer
            }
        } else {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFF381400)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFF410002)
                HolidaySeason.NEW_YEAR -> Color(0xFF001E30)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFF261900)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFF001E30)
                HolidaySeason.VALENTINE -> Color(0xFF3E001D)
                HolidaySeason.EASTER -> Color(0xFF1B2A0F)
                HolidaySeason.NONE -> finalColorScheme.onPrimaryContainer
            }
        }
        finalColorScheme.copy(
            primary = festivePrimary,
            onPrimary = festiveOnPrimary,
            primaryContainer = festiveContainer,
            onPrimaryContainer = festiveOnContainer
        )
    } else {
        finalColorScheme
    }

    MaterialTheme(
        colorScheme = festiveColorScheme,
        typography = typography,
        content = content
    )
}


fun resolveAppFontFamily(
    context: android.content.Context,
    appFont: String,
    currentLanguage: AppLanguage = AppLanguage.RU
): FontFamily {
    return when {
        appFont == "SYSTEM" || appFont == "DEFAULT" -> FontFamily.Default
        appFont == "ZAKON" -> {
            if (currentLanguage == AppLanguage.UK || currentLanguage == AppLanguage.EN) {
                FontFamily(Font(R.font.zakon_regular))
            } else {
                FontFamily(Font(R.font.google_sans_regular))
            }
        }
        appFont.startsWith("CUSTOM:") -> {
            val fileName = appFont.removePrefix("CUSTOM:")
            val file = java.io.File(context.filesDir, "custom_fonts/$fileName")
            if (file.exists()) {
                runCatching {
                    val tf = android.graphics.Typeface.createFromFile(file)
                    if (tf != null) FontFamily(tf) else FontFamily(Font(R.font.google_sans_regular))
                }.getOrDefault(FontFamily(Font(R.font.google_sans_regular)))
            } else {
                FontFamily(Font(R.font.google_sans_regular))
            }
        }
        else -> FontFamily(Font(R.font.google_sans_regular))
    }
}
