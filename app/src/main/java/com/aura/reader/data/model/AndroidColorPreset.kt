package com.aura.reader.data.model

import androidx.compose.ui.graphics.Color

/**
 * Standard Android Material 3 preset color solutions ("Основные цвета").
 * Each preset provides matched primary/secondary/container tokens for Light, Dark,
 * Sepia and AMOLED palettes, as well as a circular swatch color for the UI.
 */
enum class AndroidColorPreset(
    val id: String,
    val swatchColor: Color,
    val lightPrimary: Color,
    val lightPrimaryContainer: Color,
    val lightOnPrimaryContainer: Color,
    val lightSecondary: Color,
    val darkPrimary: Color,
    val darkPrimaryContainer: Color,
    val darkOnPrimaryContainer: Color,
    val darkSecondary: Color
) {
    PURPLE(
        id = "PURPLE",
        swatchColor = Color(0xFF6750A4),
        lightPrimary = Color(0xFF6750A4),
        lightPrimaryContainer = Color(0xFFEADDFF),
        lightOnPrimaryContainer = Color(0xFF21005D),
        lightSecondary = Color(0xFF625B71),
        darkPrimary = Color(0xFFD0BCFF),
        darkPrimaryContainer = Color(0xFF4F378B),
        darkOnPrimaryContainer = Color(0xFFEADDFF),
        darkSecondary = Color(0xFFCCC2DC)
    ),
    BLUE(
        id = "BLUE",
        swatchColor = Color(0xFF0061A4),
        lightPrimary = Color(0xFF0061A4),
        lightPrimaryContainer = Color(0xFFD1E4FF),
        lightOnPrimaryContainer = Color(0xFF001D36),
        lightSecondary = Color(0xFF535F70),
        darkPrimary = Color(0xFF9ECAFF),
        darkPrimaryContainer = Color(0xFF00497D),
        darkOnPrimaryContainer = Color(0xFFD1E4FF),
        darkSecondary = Color(0xFFBBC7DB)
    ),
    TEAL(
        id = "TEAL",
        swatchColor = Color(0xFF006A60),
        lightPrimary = Color(0xFF006A60),
        lightPrimaryContainer = Color(0xFF74F8E5),
        lightOnPrimaryContainer = Color(0xFF00201C),
        lightSecondary = Color(0xFF4A635F),
        darkPrimary = Color(0xFF53DBC9),
        darkPrimaryContainer = Color(0xFF005048),
        darkOnPrimaryContainer = Color(0xFF74F8E5),
        darkSecondary = Color(0xFFB1CCC6)
    ),
    GREEN(
        id = "GREEN",
        swatchColor = Color(0xFF2E6B27),
        lightPrimary = Color(0xFF2E6B27),
        lightPrimaryContainer = Color(0xFFAEF3A0),
        lightOnPrimaryContainer = Color(0xFF002203),
        lightSecondary = Color(0xFF53634F),
        darkPrimary = Color(0xFF93D787),
        darkPrimaryContainer = Color(0xFF145212),
        darkOnPrimaryContainer = Color(0xFFAEF3A0),
        darkSecondary = Color(0xFFB9CCB4)
    ),
    ORANGE(
        id = "ORANGE",
        swatchColor = Color(0xFF984715),
        lightPrimary = Color(0xFF984715),
        lightPrimaryContainer = Color(0xFFFFDBCF),
        lightOnPrimaryContainer = Color(0xFF351000),
        lightSecondary = Color(0xFF77574B),
        darkPrimary = Color(0xFFFFB59D),
        darkPrimaryContainer = Color(0xFF7B2F00),
        darkOnPrimaryContainer = Color(0xFFFFDBCF),
        darkSecondary = Color(0xFFE7BEAF)
    ),
    ROSE(
        id = "ROSE",
        swatchColor = Color(0xFF994060),
        lightPrimary = Color(0xFF994060),
        lightPrimaryContainer = Color(0xFFFFD9E2),
        lightOnPrimaryContainer = Color(0xFF3E001D),
        lightSecondary = Color(0xFF74565F),
        darkPrimary = Color(0xFFFFB1C8),
        darkPrimaryContainer = Color(0xFF7B2948),
        darkOnPrimaryContainer = Color(0xFFFFD9E2),
        darkSecondary = Color(0xFFE3BDC6)
    ),
    INDIGO(
        id = "INDIGO",
        swatchColor = Color(0xFF4355B9),
        lightPrimary = Color(0xFF4355B9),
        lightPrimaryContainer = Color(0xFFDEE0FF),
        lightOnPrimaryContainer = Color(0xFF00105C),
        lightSecondary = Color(0xFF5B5D72),
        darkPrimary = Color(0xFFBAC3FF),
        darkPrimaryContainer = Color(0xFF2A3C9F),
        darkOnPrimaryContainer = Color(0xFFDEE0FF),
        darkSecondary = Color(0xFFC3C5DD)
    ),
    NEUTRAL(
        id = "NEUTRAL",
        swatchColor = Color(0xFF5E5E62),
        lightPrimary = Color(0xFF5E5E62),
        lightPrimaryContainer = Color(0xFFE2E2E6),
        lightOnPrimaryContainer = Color(0xFF1B1B1F),
        lightSecondary = Color(0xFF5E5E62),
        darkPrimary = Color(0xFFC7C6CA),
        darkPrimaryContainer = Color(0xFF46464A),
        darkOnPrimaryContainer = Color(0xFFE2E2E6),
        darkSecondary = Color(0xFFC7C6CA)
    );

    companion object {
        fun fromId(id: String?): AndroidColorPreset {
            if (id.isNullOrBlank()) return PURPLE
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PURPLE
        }
    }
}
