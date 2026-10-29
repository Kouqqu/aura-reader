package com.aura.reader.ui.theme

import com.aura.reader.data.model.HolidaySeason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the beta.37 localization split: the holiday label and greeting used to be
 * hardcoded Russian on [HolidaySeason], and the settings strings were interface
 * defaults that returned Russian for every language without an override.
 */
class HolidayStringsTest {

    private fun allLanguages(): List<Pair<AppLanguage, Strings>> =
        AppLanguage.entries.map { it to getStrings(it) }

    @Test
    fun everyLanguageNamesEverySeason() {
        for ((language, strings) in allLanguages()) {
            for (season in HolidaySeason.entries) {
                assertTrue(
                    "${language.code} has no label for ${season.name}",
                    strings.holidayDisplayName(season).isNotBlank()
                )
            }
        }
    }

    @Test
    fun everyLanguageGreetsEveryActiveSeason() {
        for ((language, strings) in allLanguages()) {
            assertEquals("", strings.holidayGreeting(HolidaySeason.NONE))
            for (season in HolidaySeason.entries.filter { it != HolidaySeason.NONE }) {
                assertTrue(
                    "${language.code} has no greeting for ${season.name}",
                    strings.holidayGreeting(season).isNotBlank()
                )
            }
        }
    }

    @Test
    fun everyLanguageDefinesTheHolidaySettingsStrings() {
        for ((language, strings) in allLanguages()) {
            val values = listOf(
                strings.seasonalEffectsToggle,
                strings.seasonalEffectsSubtitle,
                strings.holidayModeTitle,
                strings.holidayModeSubtitle,
                strings.holidayModeAuto,
                strings.holidayModeOff
            )
            values.forEach {
                assertTrue("${language.code} has a blank holiday settings string", it.isNotBlank())
            }
        }
    }

    @Test
    fun seasonNamesActuallyDifferBetweenLanguages() {
        val ru = getStrings(AppLanguage.RU)
        val en = getStrings(AppLanguage.EN)
        val he = getStrings(AppLanguage.HE)
        assertNotEquals(ru.holidayGreetingHalloween, en.holidayGreetingHalloween)
        assertNotEquals(ru.holidayGreetingHalloween, he.holidayGreetingHalloween)
        assertNotEquals(ru.holidayWinterHolidays, en.holidayWinterHolidays)
    }

    @Test
    fun westernAndOrthodoxChristmasShareTheNameButNotTheGreeting() {
        val strings = getStrings(AppLanguage.RU)
        assertEquals(
            strings.holidayDisplayName(HolidaySeason.CHRISTMAS_WESTERN),
            strings.holidayDisplayName(HolidaySeason.CHRISTMAS_ORTHODOX)
        )
        assertNotEquals(
            strings.holidayGreeting(HolidaySeason.CHRISTMAS_WESTERN),
            strings.holidayGreeting(HolidaySeason.CHRISTMAS_ORTHODOX)
        )
    }
}
