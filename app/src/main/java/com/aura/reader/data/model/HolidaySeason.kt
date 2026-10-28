package com.aura.reader.data.model

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import java.util.Calendar

enum class HolidaySeason(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val badgeText: String,
    val greetingText: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val burstColors: List<Color>,
    val burstSymbols: List<String>
) {
    NONE(
        id = "NONE",
        displayName = "Обычный",
        iconEmoji = "",
        badgeText = "",
        greetingText = "",
        primaryAccent = Color.Unspecified,
        secondaryAccent = Color.Unspecified,
        burstColors = listOf(
            Color(0xFFFFD700), // Gold
            Color(0xFFFF69B4), // Hot Pink
            Color(0xFF00E5FF), // Cyan
            Color(0xFF76FF03), // Lime
            Color(0xFFFF9100), // Orange
            Color(0xFFE040FB)  // Purple
        ),
        burstSymbols = listOf("✨", "⭐", "🎉", "✦", "★")
    ),
    HALLOWEEN(
        id = "HALLOWEEN",
        displayName = "Хэллоуин",
        iconEmoji = "🎃",
        badgeText = "🎃 Хэллоуин",
        greetingText = "Жутко интересного чтения!",
        primaryAccent = Color(0xFFFF7518), // Pumpkin Orange
        secondaryAccent = Color(0xFF8A2BE2), // Witch Purple
        burstColors = listOf(
            Color(0xFFFF7518), // Pumpkin Orange
            Color(0xFFFFA500), // Bright Orange
            Color(0xFF8A2BE2), // Witch Violet
            Color(0xFF9C27B0), // Purple
            Color(0xFF32CD32), // Slime Green
            Color(0xFFFFD700), // Gold
            Color(0xFFFF5722)  // Deep Orange
        ),
        burstSymbols = listOf("🎃", "👻", "🦇", "🍬", "🕷️", "✨", "🕸️", "🕯️")
    ),
    NEW_YEAR(
        id = "NEW_YEAR",
        displayName = "Новый год",
        iconEmoji = "❄️",
        badgeText = "❄️ Зимнее чтение",
        greetingText = "Уютного новогоднего чтения!",
        primaryAccent = Color(0xFF4FC3F7), // Frost Blue
        secondaryAccent = Color(0xFFE53935), // Holiday Red
        burstColors = listOf(
            Color(0xFF4FC3F7), // Frost Blue
            Color(0xFFE53935), // Holiday Red
            Color(0xFF43A047), // Pine Green
            Color(0xFFFFD700), // Sparkle Gold
            Color(0xFFFFFFFF), // Snow White
            Color(0xFF81D4FA), // Ice Blue
            Color(0xFFFFB300)  // Warm Amber
        ),
        burstSymbols = listOf("❄️", "🎄", "⛄", "🎁", "✨", "🔔", "⭐", "🌟")
    );

    companion object {
        /**
         * Resolves the active holiday season based on calendar date or manual override.
         * override: "AUTO", "HALLOWEEN", "NEW_YEAR", "OFF"
         *
         * Default calendar dates:
         * Halloween: Oct 28 - Nov 3
         * New Year & Christmas: Dec 20 - Jan 15
         */
        fun resolve(
            enabled: Boolean = true,
            override: String = "AUTO",
            calendar: Calendar = Calendar.getInstance()
        ): HolidaySeason {
            if (!enabled) return NONE

            return when (override.uppercase()) {
                "HALLOWEEN" -> HALLOWEEN
                "NEW_YEAR", "CHRISTMAS" -> NEW_YEAR
                "OFF", "NONE" -> NONE
                else -> calculateFromCalendar(calendar)
            }
        }

        private fun calculateFromCalendar(calendar: Calendar): HolidaySeason {
            val month = calendar.get(Calendar.MONTH) // 0-indexed: 0 = Jan, 9 = Oct, 10 = Nov, 11 = Dec
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Halloween: Oct 28 to Nov 3
            if ((month == Calendar.OCTOBER && day >= 28) || (month == Calendar.NOVEMBER && day <= 3)) {
                return HALLOWEEN
            }

            // New Year & Christmas: Dec 20 to Jan 15
            if ((month == Calendar.DECEMBER && day >= 20) || (month == Calendar.JANUARY && day <= 15)) {
                return NEW_YEAR
            }

            return NONE
        }
    }
}

val LocalHolidaySeason = staticCompositionLocalOf { HolidaySeason.NONE }
