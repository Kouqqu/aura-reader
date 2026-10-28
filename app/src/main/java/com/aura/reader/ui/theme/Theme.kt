package com.aura.reader.ui.theme

import android.os.Build
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
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFFF3EDF7),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFFECE6F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF1E2125),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF282C32)
)

private val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = SepiaOnPrimary,
    primaryContainer = SepiaSurface,
    onPrimaryContainer = SepiaText,
    secondary = SepiaSecondaryText,
    onSecondary = SepiaOnPrimary,
    background = SepiaBackground,
    onBackground = SepiaText,
    surface = SepiaSurface,
    onSurface = SepiaText,
    surfaceVariant = SepiaSurface,
    onSurfaceVariant = SepiaSecondaryText,
    surfaceContainer = androidx.compose.ui.graphics.Color(0xFFEBE3D3),
    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFFE3DAC8)
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

@Composable
fun AuraReaderTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.DARK,
    materialYou: Boolean = false,
    appFont: String = "GOOGLE_SANS",
    readerMode: Boolean = false,
    holidaySeason: HolidaySeason = HolidaySeason.NONE,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val hasDynamic = materialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val systemDark = isSystemInDarkTheme()

    val typography = remember(appFont) {
        val fontFamily = resolveAppFontFamily(context, appFont)
        createAppTypography(fontFamily)
    }

    val colorScheme = when (themeMode) {
        ReaderThemeMode.LIGHT -> {
            if (hasDynamic) {
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
        }
        ReaderThemeMode.DARK -> {
            if (hasDynamic) {
                dynamicDarkColorScheme(context)
            } else {
                DarkColorScheme
            }
        }
        ReaderThemeMode.AMOLED -> {
            if (hasDynamic) {
                val dyn = dynamicDarkColorScheme(context)
                dyn.copy(
                    background = androidx.compose.ui.graphics.Color.Black,
                    surface = androidx.compose.ui.graphics.Color.Black,
                    surfaceContainer = androidx.compose.ui.graphics.Color(0xFF101010),
                    surfaceContainerHigh = androidx.compose.ui.graphics.Color(0xFF181818)
                )
            } else {
                AmoledColorScheme
            }
        }
        ReaderThemeMode.SEPIA -> {
            if (hasDynamic) {
                val dyn = dynamicLightColorScheme(context)
                SepiaColorScheme.copy(
                    primary = dyn.primary,
                    onPrimary = dyn.onPrimary,
                    primaryContainer = dyn.primaryContainer,
                    onPrimaryContainer = dyn.onPrimaryContainer,
                    secondary = dyn.secondary
                )
            } else {
                SepiaColorScheme
            }
        }
        ReaderThemeMode.SYSTEM_DYNAMIC -> {
            if (systemDark) {
                if (hasDynamic) dynamicDarkColorScheme(context) else DarkColorScheme
            } else {
                if (hasDynamic) dynamicLightColorScheme(context) else LightColorScheme
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
                HolidaySeason.NONE -> finalColorScheme.primaryContainer
            }
        } else {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFFFFE0D0)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFFFFDAD6)
                HolidaySeason.NEW_YEAR -> Color(0xFFC7E7FF)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFFFFE082)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFFC7E7FF)
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
                HolidaySeason.NONE -> finalColorScheme.onPrimaryContainer
            }
        } else {
            when (holidaySeason) {
                HolidaySeason.HALLOWEEN -> Color(0xFF381400)
                HolidaySeason.CHRISTMAS_WESTERN -> Color(0xFF410002)
                HolidaySeason.NEW_YEAR -> Color(0xFF001E30)
                HolidaySeason.CHRISTMAS_ORTHODOX -> Color(0xFF261900)
                HolidaySeason.OLD_NEW_YEAR -> Color(0xFF001E30)
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


fun resolveAppFontFamily(context: android.content.Context, appFont: String): FontFamily {
    return when {
        appFont == "SYSTEM" || appFont == "DEFAULT" -> FontFamily.Default
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
