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
    fun testWinterHolidaysDateRange() {
        // Dec 19 -> NONE
        val dec19 = createCalendar(2026, Calendar.DECEMBER, 19)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = dec19))

        // Dec 20..26 -> Western / Catholic Christmas (25 Dec)
        val dec20 = createCalendar(2026, Calendar.DECEMBER, 20)
        assertEquals(HolidaySeason.CHRISTMAS_WESTERN, HolidaySeason.resolve(calendar = dec20))
        val dec25 = createCalendar(2026, Calendar.DECEMBER, 25)
        assertEquals(HolidaySeason.CHRISTMAS_WESTERN, HolidaySeason.resolve(calendar = dec25))
        val dec26 = createCalendar(2026, Calendar.DECEMBER, 26)
        assertEquals(HolidaySeason.CHRISTMAS_WESTERN, HolidaySeason.resolve(calendar = dec26))

        // Dec 27..Jan 5 -> New Year (31 Dec / 1 Jan)
        val dec27 = createCalendar(2026, Calendar.DECEMBER, 27)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = dec27))
        val dec31 = createCalendar(2026, Calendar.DECEMBER, 31)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = dec31))
        val jan1 = createCalendar(2027, Calendar.JANUARY, 1)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = jan1))
        val jan5 = createCalendar(2027, Calendar.JANUARY, 5)
        assertEquals(HolidaySeason.NEW_YEAR, HolidaySeason.resolve(calendar = jan5))

        // Jan 6..8 -> Orthodox Christmas (7 Jan)
        val jan6 = createCalendar(2027, Calendar.JANUARY, 6)
        assertEquals(HolidaySeason.CHRISTMAS_ORTHODOX, HolidaySeason.resolve(calendar = jan6))
        val jan7 = createCalendar(2027, Calendar.JANUARY, 7)
        assertEquals(HolidaySeason.CHRISTMAS_ORTHODOX, HolidaySeason.resolve(calendar = jan7))
        val jan8 = createCalendar(2027, Calendar.JANUARY, 8)
        assertEquals(HolidaySeason.CHRISTMAS_ORTHODOX, HolidaySeason.resolve(calendar = jan8))

        // Jan 9..15 -> Old New Year & Winter Yuletide (13-14 Jan)
        val jan9 = createCalendar(2027, Calendar.JANUARY, 9)
        assertEquals(HolidaySeason.OLD_NEW_YEAR, HolidaySeason.resolve(calendar = jan9))
        val jan14 = createCalendar(2027, Calendar.JANUARY, 14)
        assertEquals(HolidaySeason.OLD_NEW_YEAR, HolidaySeason.resolve(calendar = jan14))
        val jan15 = createCalendar(2027, Calendar.JANUARY, 15)
        assertEquals(HolidaySeason.OLD_NEW_YEAR, HolidaySeason.resolve(calendar = jan15))

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
    fun testValentineDateRange() {
        // Feb 9 -> NONE
        val feb9 = createCalendar(2027, Calendar.FEBRUARY, 9)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = feb9))

        // Feb 10..15 -> VALENTINE
        val feb10 = createCalendar(2027, Calendar.FEBRUARY, 10)
        assertEquals(HolidaySeason.VALENTINE, HolidaySeason.resolve(calendar = feb10))
        val feb14 = createCalendar(2027, Calendar.FEBRUARY, 14)
        assertEquals(HolidaySeason.VALENTINE, HolidaySeason.resolve(calendar = feb14))
        val feb15 = createCalendar(2027, Calendar.FEBRUARY, 15)
        assertEquals(HolidaySeason.VALENTINE, HolidaySeason.resolve(calendar = feb15))

        // Feb 16 -> NONE
        val feb16 = createCalendar(2027, Calendar.FEBRUARY, 16)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(calendar = feb16))
    }

    @Test
    fun testEasterDateRange() {
        // 2025: Western and Orthodox Easter both fall on Apr 20 -> window Apr 18..21
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2025, Calendar.APRIL, 18))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2025, Calendar.APRIL, 17))
        )
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2025, Calendar.APRIL, 21))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2025, Calendar.APRIL, 22))
        )

        // 2026: Western Easter Apr 5 (window Apr 3..6), Orthodox Easter Apr 12 (Apr 10..13)
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2026, Calendar.APRIL, 4))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2026, Calendar.APRIL, 8))
        )
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2026, Calendar.APRIL, 11))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2026, Calendar.APRIL, 14))
        )

        // 2027: Western Easter Mar 28 (window Mar 26..29), Orthodox Easter May 2 (Apr 30..May 3)
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2027, Calendar.MARCH, 27))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2027, Calendar.APRIL, 15))
        )
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(calendar = createCalendar(2027, Calendar.MAY, 1))
        )
        assertEquals(
            HolidaySeason.NONE,
            HolidaySeason.resolve(calendar = createCalendar(2027, Calendar.MAY, 4))
        )
    }

    @Test
    fun testDisabledSetting() {
        val halloween = createCalendar(2026, Calendar.OCTOBER, 31)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(enabled = false, calendar = halloween))

        val christmas = createCalendar(2026, Calendar.DECEMBER, 25)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(enabled = false, calendar = christmas))

        val newYear = createCalendar(2026, Calendar.DECEMBER, 31)
        assertEquals(HolidaySeason.NONE, HolidaySeason.resolve(enabled = false, calendar = newYear))
    }

    @Test
    fun testManualOverrides() {
        val summerDay = createCalendar(2026, Calendar.JULY, 15)

        assertEquals(
            HolidaySeason.HALLOWEEN,
            HolidaySeason.resolve(enabled = true, override = "HALLOWEEN", calendar = summerDay)
        )
        assertEquals(
            HolidaySeason.CHRISTMAS_WESTERN,
            HolidaySeason.resolve(enabled = true, override = "CHRISTMAS_WESTERN", calendar = summerDay)
        )
        assertEquals(
            HolidaySeason.NEW_YEAR,
            HolidaySeason.resolve(enabled = true, override = "NEW_YEAR", calendar = summerDay)
        )
        assertEquals(
            HolidaySeason.CHRISTMAS_ORTHODOX,
            HolidaySeason.resolve(enabled = true, override = "CHRISTMAS_ORTHODOX", calendar = summerDay)
        )
        assertEquals(
            HolidaySeason.VALENTINE,
            HolidaySeason.resolve(enabled = true, override = "VALENTINE", calendar = summerDay)
        )
        assertEquals(
            HolidaySeason.EASTER,
            HolidaySeason.resolve(enabled = true, override = "EASTER", calendar = summerDay)
        )

        val halloween = createCalendar(2026, Calendar.OCTOBER, 31)
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

        assertTrue(HolidaySeason.CHRISTMAS_WESTERN.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.CHRISTMAS_WESTERN.burstSymbols.contains("🎄"))

        assertTrue(HolidaySeason.NEW_YEAR.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.NEW_YEAR.burstSymbols.contains("❄️"))

        assertTrue(HolidaySeason.CHRISTMAS_ORTHODOX.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.CHRISTMAS_ORTHODOX.burstSymbols.contains("⭐"))

        assertTrue(HolidaySeason.VALENTINE.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.VALENTINE.burstSymbols.contains("❤️"))

        assertTrue(HolidaySeason.EASTER.burstColors.isNotEmpty())
        assertTrue(HolidaySeason.EASTER.burstSymbols.contains("🥚"))

        assertTrue(HolidaySeason.NONE.burstColors.isNotEmpty())
    }
}
