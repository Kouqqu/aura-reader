package com.aura.reader.data

import com.aura.reader.data.model.HolidaySeason
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class HolidaySeasonTest {

    private fun createCalendar(year: Int, month: Int, day: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
    }

    @Test
    fun testHalloweenDateRange() {
        // Oct 27 -> NONE
        val oct27 = createCalendar(2026, Calendar.OCTOBER, 27)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = oct27))

        // Oct 28 -> HALLOWEEN
        val oct28 = createCalendar(2026, Calendar.OCTOBER, 28)
        assertEquals(HolidaySeason.HALLOWEEN, HolidaySeason.resolve(calendar = oct28))

        // Oct 31 -> HALLOWEEN
        val oct31 = createCalendar(2026, Calendar.OCTOBER, 31)
        assertEquals(HolidaySeason.HALLOWEEN, HolidaySeason.resolve(calendar = oct31))

        // Nov 3 -> HALLOWEEN
        val nov3 = createCalendar(2026, Calendar.NOVEMBER, 3)
        assertEquals(HolidaySeason.HALLOWEEN, HolidaySeason.resolve(calendar = nov3))

        // Nov 4 -> NONE
        val nov4 = createCalendar(2026, Calendar.NOVEMBER, 4)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = nov4))
    }

    @Test
    fun testNewYearDateRange() {
        // Dec 19 -> NONE
        val dec19 = createCalendar(2026, Calendar.DECEMBER, 19)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = dec19))

        // Dec 20 -> NEW_YEAR
        val dec20 = createCalendar(2026, Calendar.DECEMBER, 20)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = dec20))

        // Dec 31 -> NEW_YEAR
        val dec31 = createCalendar(2026, Calendar.DECEMBER, 31)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = dec31))

        // Jan 1 -> NEW_YEAR
        val jan1 = createCalendar(2027, Calendar.JANUARY, 1)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = jan1))

        // Jan 15 -> NEW_YEAR
        val jan15 = createCalendar(2027, Calendar.JANUARY, 15)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = jan15))

        // Jan 16 -> NONE
        val jan16 = createCalendar(2027, Calendar.JANUARY, 16)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = jan16))
    }

    @Test
    fun testOffSeason() {
        val summerDay = createCalendar(2026, Calendar.JULY, 15)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = summerDay))
    }

    @Test
    fun testDisabledSetting() {
        val halloween = createCalendar(2026, Calendar.OCTOBER, 31)
        // Disabled should always return NONE
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(enabled = false, calendar = halloween))

        val newYear = createCalendar(2026, Calendar.DECEMBER, 31)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(enabled = false, calendar = newYear))
    }

    @Test
    fun testManualOverrides() {
        val summerDay = createCalendar(2026, Calendar.JULY, 15)

        // Force Halloween in summer
        assertEquals(
            HolidaySeason.HALLOWEEN,
            HolidaySeason.resolve(enabled = true, override = "HALLOWEEN", calendar = summerDay)
        )

        // Force New Year in summer
        assertEquals(
            HolidaySeason.NEW_YEAR,
            HolidaySeason.resolve(enabled = true, override = "NEW_YEAR", calendar = summerDay)
        )

        val halloween = createCalendar(2026, Calendar.OCTOBER, 31)
        // Force OFF on Halloween
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(enabled = true, override = "OFF", calendar = halloween)
        )
    }

    @Test
    fun testBurstPalettesAndSymbols() {
        assertTrue(HolidaySeason.HALLOWEEN.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.HALLOWEEN.burstSymbols.contains("🎃"))
        assertTrue(HolidaySeason.HALLOWEEN.burstSymbols.contains("🦇"))

        assertTrue(HolidaySeason.NEW_YEAR.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.NEW_YEAR.burstSymbols.contains("❄️"))
        assertTrue(HolidaySeason.NEW_YEAR.burstSymbols.contains("🎄"))

        assertTrue(HolidaySeason.NONE.burstColors.isNotEmpty())
    }
}
