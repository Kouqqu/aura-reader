package com.aura.reader.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun createAppTypography(fontFamily: FontFamily): Typography {
    val displayLargeBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    )
    val displayMediumBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.15).sp
    )
    val displaySmallBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
    val headlineLargeBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.3).sp
    )
    val headlineMediumBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.2).sp
    )
    val headlineSmallBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.1).sp
    )
    val titleLargeBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )
    val titleMediumBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp
    )
    val titleSmallBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val bodyLargeBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.4.sp
    )
    val bodyMediumBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
    val bodySmallBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    val labelLargeBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    val labelMediumBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )
    val labelSmallBase = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    return Typography(
        displayLarge = displayLargeBase,
        displayMedium = displayMediumBase,
        displaySmall = displaySmallBase,
        headlineLarge = headlineLargeBase,
        headlineMedium = headlineMediumBase,
        headlineSmall = headlineSmallBase,
        titleLarge = titleLargeBase,
        titleMedium = titleMediumBase,
        titleSmall = titleSmallBase,
        bodyLarge = bodyLargeBase,
        bodyMedium = bodyMediumBase,
        bodySmall = bodySmallBase,
        labelLarge = labelLargeBase,
        labelMedium = labelMediumBase,
        labelSmall = labelSmallBase,
        displayLargeEmphasized = displayLargeBase.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
        displayMediumEmphasized = displayMediumBase.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.35).sp),
        displaySmallEmphasized = displaySmallBase.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.25).sp),
        headlineLargeEmphasized = headlineLargeBase.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.4).sp),
        headlineMediumEmphasized = headlineMediumBase.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
        headlineSmallEmphasized = headlineSmallBase.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.15).sp),
        titleLargeEmphasized = titleLargeBase.copy(fontWeight = FontWeight.Bold),
        titleMediumEmphasized = titleMediumBase.copy(fontWeight = FontWeight.Bold),
        titleSmallEmphasized = titleSmallBase.copy(fontWeight = FontWeight.Bold),
        bodyLargeEmphasized = bodyLargeBase.copy(fontWeight = FontWeight.Medium),
        bodyMediumEmphasized = bodyMediumBase.copy(fontWeight = FontWeight.Medium),
        bodySmallEmphasized = bodySmallBase.copy(fontWeight = FontWeight.Medium),
        labelLargeEmphasized = labelLargeBase.copy(fontWeight = FontWeight.Bold),
        labelMediumEmphasized = labelMediumBase.copy(fontWeight = FontWeight.Bold),
        labelSmallEmphasized = labelSmallBase.copy(fontWeight = FontWeight.Bold)
    )
}

val Typography = createAppTypography(FontFamily.Default)
