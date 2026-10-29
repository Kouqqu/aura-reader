package com.aura.reader.ui.theme

import com.aura.reader.data.model.HolidaySeason

/**
 * Localized label for a holiday season, used by the library header badge.
 *
 * The label lives here rather than on [HolidaySeason] so the data model stays free of
 * user-visible text and every language can name the season in its own words.
 */
fun Strings.holidayDisplayName(season: HolidaySeason): String = when (season) {
    HolidaySeason.NONE -> holidayNone
    HolidaySeason.HALLOWEEN -> holidayHalloween
    HolidaySeason.CHRISTMAS_WESTERN,
    HolidaySeason.CHRISTMAS_ORTHODOX -> holidayChristmas
    HolidaySeason.NEW_YEAR -> holidayNewYear
    HolidaySeason.OLD_NEW_YEAR -> holidayWinterHolidays
}

/** Localized one-line greeting shown under the library title. */
fun Strings.holidayGreeting(season: HolidaySeason): String = when (season) {
    HolidaySeason.NONE -> ""
    HolidaySeason.HALLOWEEN -> holidayGreetingHalloween
    HolidaySeason.CHRISTMAS_WESTERN -> holidayGreetingChristmasWestern
    HolidaySeason.CHRISTMAS_ORTHODOX -> holidayGreetingChristmasOrthodox
    HolidaySeason.NEW_YEAR -> holidayGreetingNewYear
    HolidaySeason.OLD_NEW_YEAR -> holidayGreetingWinter
}
