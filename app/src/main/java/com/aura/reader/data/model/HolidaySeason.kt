package com.aura.reader.data.model

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.Calendar

enum class HolidaySeason(
    val id: String,
    val iconEmoji: String,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val burstColors: List<Color>,
    val burstSymbols: List<String>
) {
    NONE(
        id = "NONE",
        iconEmoji = "",
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
        iconEmoji = "🎃",
        primaryAccent = Color(0xFFFF6D00), // Rich Vivid Pumpkin Orange
        secondaryAccent = Color(0xFF9C27B0), // Witch Violet
        burstColors = listOf(
            Color(0xFFFF6D00), // Pumpkin Orange
            Color(0xFFFF9100), // Bright Orange
            Color(0xFFFFAB40), // Warm Amber
            Color(0xFF9C27B0), // Witch Violet
            Color(0xFFBA68C8), // Soft Purple
            Color(0xFF32CD32), // Slime Green
            Color(0xFFFFD700)  // Gold
        ),
        burstSymbols = listOf("🎃", "👻", "🦇", "🍬", "🕷️", "✨", "🕸️", "🕯️")
    ),
    CHRISTMAS_WESTERN(
        id = "CHRISTMAS_WESTERN",
        iconEmoji = "🎄",
        primaryAccent = Color(0xFFE53935), // Holiday Crimson Red
        secondaryAccent = Color(0xFF2E7D32), // Pine Tree Green
        burstColors = listOf(
            Color(0xFFE53935), // Crimson Red
            Color(0xFFFF5252), // Bright Red
            Color(0xFF2E7D32), // Pine Green
            Color(0xFF4CAF50), // Festive Green
            Color(0xFFFFD700), // Sparkle Gold
            Color(0xFFFFFFFF)  // Snow White
        ),
        burstSymbols = listOf("🎄", "⭐", "🎁", "🕯️", "🔔", "✨", "❄️")
    ),
    NEW_YEAR(
        id = "NEW_YEAR",
        iconEmoji = "❄️",
        primaryAccent = Color(0xFF00B0FF), // Vibrant Frost Ice Blue
        secondaryAccent = Color(0xFFFFD700), // Sparkle Gold
        burstColors = listOf(
            Color(0xFF00B0FF), // Ice Blue
            Color(0xFF40C4FF), // Sky Blue
            Color(0xFF80D8FF), // Light Ice
            Color(0xFFFFD700), // Sparkle Gold
            Color(0xFFFFFFFF), // Snow White
            Color(0xFFE040FB)  // Festive Magenta
        ),
        burstSymbols = listOf("❄️", "🍾", "🎉", "⛄", "✨", "🌟", "🎊", "⭐")
    ),
    CHRISTMAS_ORTHODOX(
        id = "CHRISTMAS_ORTHODOX",
        iconEmoji = "⭐",
        primaryAccent = Color(0xFFFFB300), // Star Gold
        secondaryAccent = Color(0xFF4FC3F7), // Heavenly Blue
        burstColors = listOf(
            Color(0xFFFFB300), // Star Gold
            Color(0xFFFFD54F), // Light Gold
            Color(0xFF4FC3F7), // Frost Blue
            Color(0xFF81D4FA), // Ice Blue
            Color(0xFFFFFFFF), // Pure White
            Color(0xFFFFF9C4)  // Candle Light
        ),
        burstSymbols = listOf("⭐", "🌟", "🕊️", "🕯️", "✨", "❄️", "🎄")
    ),
    OLD_NEW_YEAR(
        id = "OLD_NEW_YEAR",
        iconEmoji = "❄️",
        primaryAccent = Color(0xFF29B6F6), // Crystal Blue
        secondaryAccent = Color(0xFFB0BEC5), // Frost Silver
        burstColors = listOf(
            Color(0xFF29B6F6), // Crystal Blue
            Color(0xFF81D4FA), // Soft Blue
            Color(0xFFB0BEC5), // Silver
            Color(0xFFFFFFFF), // White
            Color(0xFFFFD700)  // Star Gold
        ),
        burstSymbols = listOf("❄️", "⛄", "✨", "🌟", "☕", "🤍")
    ),
    VALENTINE(
        id = "VALENTINE",
        iconEmoji = "💗",
        primaryAccent = Color(0xFFE91E63), // Valentine Rose
        secondaryAccent = Color(0xFFFF80AB), // Soft Pink
        burstColors = listOf(
            Color(0xFFE91E63), // Rose
            Color(0xFFFF4081), // Bright Pink
            Color(0xFFFF80AB), // Soft Pink
            Color(0xFFF8BBD0), // Blush
            Color(0xFFFFD54F), // Warm Gold
            Color(0xFFFFFFFF)  // White
        ),
        burstSymbols = listOf("❤️", "💕", "🌹", "💌", "✨", "💗")
    ),
    EASTER(
        id = "EASTER",
        iconEmoji = "🐣",
        primaryAccent = Color(0xFF7CB342), // Spring Green
        secondaryAccent = Color(0xFFFFC107), // Easter Gold
        burstColors = listOf(
            Color(0xFF7CB342), // Spring Green
            Color(0xFFAED581), // Light Green
            Color(0xFFFFC107), // Easter Gold
            Color(0xFFFFE082), // Soft Gold
            Color(0xFFF48FB1), // Pastel Pink
            Color(0xFFFFFFFF)  // White
        ),
        burstSymbols = listOf("🐣", "🥚", "🌷", "🐰", "✨", "🌼")
    );

    val isWinterSeason: Boolean
        get() = this == CHRISTMAS_WESTERN || this == NEW_YEAR || this == CHRISTMAS_ORTHODOX || this == OLD_NEW_YEAR

    val isHalloween: Boolean
        get() = this == HALLOWEEN

    companion object {
        /**
         * Resolves the active holiday season based on calendar date or manual override.
         * override: "AUTO", "HALLOWEEN", "CHRISTMAS_WESTERN", "NEW_YEAR", "CHRISTMAS_ORTHODOX",
         * "OLD_NEW_YEAR", "VALENTINE", "EASTER", "OFF"
         *
         * Default calendar dates:
         * - Halloween: Oct 28 - Nov 3
         * - Western / Catholic Christmas: Dec 20 - Dec 26
         * - New Year: Dec 27 - Jan 5
         * - Orthodox Christmas: Jan 6 - Jan 8
         * - Old New Year & Winter Yuletide: Jan 9 - Jan 15
         * - Valentine's Day: Feb 10 - Feb 15
         * - Easter: two days before to one day after the Western and the Orthodox Easter
         */
        fun resolve(
            enabled: Boolean = true,
            override: String = "AUTO",
            calendar: Calendar = Calendar.getInstance()
        ): HolidaySeason {
            if (!enabled) return NONE

            return when (override.uppercase()) {
                "HALLOWEEN" -> HALLOWEEN
                "CHRISTMAS_WESTERN", "CHRISTMAS" -> CHRISTMAS_WESTERN
                "NEW_YEAR" -> NEW_YEAR
                "CHRISTMAS_ORTHODOX" -> CHRISTMAS_ORTHODOX
                "OLD_NEW_YEAR", "WINTER" -> OLD_NEW_YEAR
                "VALENTINE", "VALENTINES" -> VALENTINE
                "EASTER" -> EASTER
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

            // Western / Catholic Christmas: Dec 20 to Dec 26
            if (month == Calendar.DECEMBER && day in 20..26) {
                return CHRISTMAS_WESTERN
            }

            // New Year: Dec 27 to Jan 5
            if ((month == Calendar.DECEMBER && day >= 27) || (month == Calendar.JANUARY && day <= 5)) {
                return NEW_YEAR
            }

            // Orthodox Christmas: Jan 6 to Jan 8
            if (month == Calendar.JANUARY && day in 6..8) {
                return CHRISTMAS_ORTHODOX
            }

            // Old New Year & Winter Yuletide: Jan 9 to Jan 15
            if (month == Calendar.JANUARY && day in 9..15) {
                return OLD_NEW_YEAR
            }

            // Valentine's Day: Feb 10 to Feb 15
            if (month == Calendar.FEBRUARY && day in 10..15) {
                return VALENTINE
            }

            // Easter: the Western and the Orthodox dates can differ (or coincide),
            // so both short windows are accepted with the same palette and greeting.
            if (isEasterWindow(calendar.toLocalDate())) {
                return EASTER
            }

            return NONE
        }

        private fun Calendar.toLocalDate(): LocalDate =
            LocalDate.of(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH))

        private fun isEasterWindow(date: LocalDate): Boolean {
            val easterDates = listOf(
                westernEaster(date.year),
                orthodoxEaster(date.year)
            )
            return easterDates.any { easter ->
                !date.isBefore(easter.minusDays(2)) && !date.isAfter(easter.plusDays(1))
            }
        }

        /** Anonymous Gregorian computus (Meeus/Jones/Butcher). */
        private fun westernEaster(year: Int): LocalDate {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = (h + l - 7 * m + 114) % 31 + 1
            return LocalDate.of(year, month, day)
        }

        /**
         * Meeus Julian computus converted to the Gregorian calendar.
         * The 13-day offset is valid for 1900-2099, which covers the supported app lifetime.
         */
        private fun orthodoxEaster(year: Int): LocalDate {
            val a = year % 4
            val b = year % 7
            val c = year % 19
            val d = (19 * c + 15) % 30
            val e = (2 * a + 4 * b - d + 34) % 7
            val month = (d + e + 114) / 31
            val day = (d + e + 114) % 31 + 1
            return LocalDate.of(year, month, day).plusDays(13)
        }
    }
}

val LocalHolidaySeason = staticCompositionLocalOf { HolidaySeason.NONE }
