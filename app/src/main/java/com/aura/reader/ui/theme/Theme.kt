package com.aura.reader.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
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
    primaryContainer = AmoledSurface,
    onPrimaryContainer = AmoledText,
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

@Composable
fun AuraReaderTheme(
    themeMode: ReaderThemeMode = ReaderThemeMode.DARK,
    materialYou: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val hasDynamic = materialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

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
            if (hasDynamic) dynamicDarkColorScheme(context) else DarkColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
