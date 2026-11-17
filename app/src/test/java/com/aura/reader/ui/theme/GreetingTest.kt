package com.aura.reader.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the greeting shown under the app name on an ordinary day: which hours belong to
 * which part of the day, and that every one of the seven languages actually says
 * something different from the others.
 */
class GreetingTest {

    @Test
    fun everyHourMapsToExactlyOnePeriod() {
        val expected = buildMap {
            for (hour in 0..23) {
                put(
                    hour,
                    when (hour) {
                        in 5..11 -> DayPeriod.MORNING
                        in 12..16 -> DayPeriod.AFTERNOON
                        in 17..22 -> DayPeriod.EVENING
                        else -> DayPeriod.NIGHT
                    }
                )
            }
        }
        for (hour in 0..23) {
            assertEquals("hour $hour", expected.getValue(hour), dayPeriodForHour(hour))
        }
    }

    @Test
    fun boundariesSitWhereReadersExpectThem() {
        assertEquals(DayPeriod.NIGHT, dayPeriodForHour(4))
        assertEquals(DayPeriod.MORNING, dayPeriodForHour(5))
        assertEquals(DayPeriod.MORNING, dayPeriodForHour(11))
        assertEquals(DayPeriod.AFTERNOON, dayPeriodForHour(12))
        assertEquals(DayPeriod.AFTERNOON, dayPeriodForHour(16))
        assertEquals(DayPeriod.EVENING, dayPeriodForHour(17))
        assertEquals(DayPeriod.EVENING, dayPeriodForHour(22))
        assertEquals(DayPeriod.NIGHT, dayPeriodForHour(23))
    }

    @Test
    fun hoursOutsideTheClockAreRejected() {
        for (hour in listOf(-1, 24, 99)) {
            var threw = false
            try {
                dayPeriodForHour(hour)
            } catch (expected: IllegalArgumentException) {
                threw = true
            }
            assertTrue("hour $hour should be rejected", threw)
        }
    }

    @Test
    fun everyLanguageGreetsEveryPeriod() {
        for (language in AppLanguage.entries) {
            val strings = getStrings(language)
            for (period in DayPeriod.entries) {
                assertTrue(
                    "${language.code} has no greeting for ${period.name}",
                    strings.greetingFor(period).isNotBlank()
                )
            }
        }
    }

    @Test
    fun greetingsAreTranslatedNotCopiedFromRussian() {
        val ru = getStrings(AppLanguage.RU)
        val en = getStrings(AppLanguage.EN)
        val he = getStrings(AppLanguage.HE)
        assertNotEquals(ru.greetingMorning, en.greetingMorning)
        assertNotEquals(ru.greetingMorning, he.greetingMorning)
        assertNotEquals(ru.greetingEvening, he.greetingEvening)
    }
}
