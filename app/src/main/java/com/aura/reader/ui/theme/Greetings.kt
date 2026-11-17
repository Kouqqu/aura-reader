package com.aura.reader.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import java.util.Calendar
import kotlinx.coroutines.delay

/** The four parts of the day the library header greets the reader by. */
enum class DayPeriod { MORNING, AFTERNOON, EVENING, NIGHT }

/**
 * Which greeting an hour of the wall clock belongs to: morning 5–11, afternoon 12–16,
 * evening 17–22, night 23–4. Kept as a plain function so the boundaries are pinned by a
 * unit test rather than by whatever the clock said when the screen was opened.
 */
fun dayPeriodForHour(hour: Int): DayPeriod {
    require(hour in 0..23) { "hour must be 0..23, was $hour" }
    return when (hour) {
        in 5..11 -> DayPeriod.MORNING
        in 12..16 -> DayPeriod.AFTERNOON
        in 17..22 -> DayPeriod.EVENING
        else -> DayPeriod.NIGHT
    }
}

/** The localized greeting for [period]. */
fun Strings.greetingFor(period: DayPeriod): String = when (period) {
    DayPeriod.MORNING -> greetingMorning
    DayPeriod.AFTERNOON -> greetingAfternoon
    DayPeriod.EVENING -> greetingEvening
    DayPeriod.NIGHT -> greetingNight
}

/**
 * The current hour, re-read on every whole minute: a library left open through midnight
 * changes its greeting on its own, without waiting for the next restart.
 */
@Composable
fun rememberCurrentHour(): Int {
    val hour by produceState(initialValue = currentHour()) {
        while (true) {
            delay(60_000L)
            value = currentHour()
        }
    }
    return hour
}

private fun currentHour(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
