package me.easynap

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimerHelpersTest {

    @Test
    fun `parseDurationMinutes accepts positive float`() {
        assertEquals(12.5f, parseDurationMinutes("12.5"))
    }

    @Test
    fun `parseDurationMinutes accepts positive integer`() {
        assertEquals(10f, parseDurationMinutes("10"))
    }

    @Test
    fun `parseDurationMinutes rejects empty string`() {
        assertNull(parseDurationMinutes(""))
    }

    @Test
    fun `parseDurationMinutes rejects zero`() {
        assertNull(parseDurationMinutes("0"))
    }

    @Test
    fun `parseDurationMinutes rejects negative`() {
        assertNull(parseDurationMinutes("-5"))
    }

    @Test
    fun `parseDurationMinutes rejects non-numeric`() {
        assertNull(parseDurationMinutes("abc"))
    }

    @Test
    fun `parseDurationMinutes trims whitespace`() {
        assertEquals(5f, parseDurationMinutes("  5  "))
    }

    @Test
    fun `formatRemainingTime formats mm colon ss correctly`() {
        assertEquals("01:30", formatRemainingTime(90_000))
    }

    @Test
    fun `formatRemainingTime pads single digits`() {
        assertEquals("00:05", formatRemainingTime(5_000))
    }

    @Test
    fun `formatRemainingTime handles zero`() {
        assertEquals("00:00", formatRemainingTime(0))
    }

    @Test
    fun `formatRemainingTime clamps negative to zero`() {
        assertEquals("00:00", formatRemainingTime(-1000))
    }

    @Test
    fun `formatRemainingTime handles large values`() {
        assertEquals("60:00", formatRemainingTime(3_600_000))
    }
}
