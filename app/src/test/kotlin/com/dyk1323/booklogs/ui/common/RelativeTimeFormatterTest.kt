package com.dyk1323.booklogs.ui.common

import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeFormatterTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `under a minute is 방금 전`() {
        assertEquals("방금 전", formatRelativeTime(now - TimeUnit.SECONDS.toMillis(30), now))
    }

    @Test
    fun `minutes ago is N분 전`() {
        assertEquals("3분 전", formatRelativeTime(now - TimeUnit.MINUTES.toMillis(3), now))
    }

    @Test
    fun `hours ago is N시간 전`() {
        assertEquals("2시간 전", formatRelativeTime(now - TimeUnit.HOURS.toMillis(2), now))
    }

    @Test
    fun `days ago is N일 전`() {
        assertEquals("3일 전", formatRelativeTime(now - TimeUnit.DAYS.toMillis(3), now))
    }
}
