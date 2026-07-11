package com.dyk1323.booklogs.notification

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderSchedulerTest {

    private val zone = TimeZone.getTimeZone("Asia/Seoul")

    @Test
    fun nextTriggerMillis_laterToday_returnsSameDay() {
        val now = calendarAt(2026, Calendar.JULY, 11, 9, 0)

        val trigger = nextTriggerMillis(hour = 21, minute = 0, nowMillis = now.timeInMillis, zone = zone)

        val expected = calendarAt(2026, Calendar.JULY, 11, 21, 0)
        assertEquals(expected.timeInMillis, trigger)
    }

    @Test
    fun nextTriggerMillis_alreadyPassedToday_rollsToTomorrow() {
        val now = calendarAt(2026, Calendar.JULY, 11, 22, 0)

        val trigger = nextTriggerMillis(hour = 21, minute = 0, nowMillis = now.timeInMillis, zone = zone)

        val expected = calendarAt(2026, Calendar.JULY, 12, 21, 0)
        assertEquals(expected.timeInMillis, trigger)
    }

    @Test
    fun nextTriggerMillis_exactlyNow_rollsToTomorrow() {
        val now = calendarAt(2026, Calendar.JULY, 11, 21, 0)

        val trigger = nextTriggerMillis(hour = 21, minute = 0, nowMillis = now.timeInMillis, zone = zone)

        val expected = calendarAt(2026, Calendar.JULY, 12, 21, 0)
        assertEquals(expected.timeInMillis, trigger)
    }

    private fun calendarAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Calendar =
        Calendar.getInstance(zone).apply {
            set(year, month, day, hour, minute, 0)
            set(Calendar.MILLISECOND, 0)
        }
}
