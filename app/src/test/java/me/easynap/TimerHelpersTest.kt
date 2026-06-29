package me.easynap

import me.easynap.timer.SNOOZE_OPTIONS
import me.easynap.timer.anticipatedProgressMs
import me.easynap.timer.appendToBuffer
import me.easynap.timer.durationDisplayMinutesOrNull
import me.easynap.timer.durationTotalSeconds
import me.easynap.timer.formatRemainingTime
import me.easynap.timer.formatRemainingTimeRoundUp
import me.easynap.timer.isCustomDurationInRange
import me.easynap.timer.parseCustomDurationSeconds
import me.easynap.timer.parseDurationMinutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `durationTotalSeconds rounds stored minute fractions to seconds`() {
        assertEquals(80, durationTotalSeconds(80f / 60f))
    }

    @Test
    fun `durationDisplayMinutesOrNull floors durations one minute or longer`() {
        assertEquals(1, durationDisplayMinutesOrNull(80f / 60f))
        assertEquals(2, durationDisplayMinutesOrNull(179f / 60f))
    }

    @Test
    fun `durationDisplayMinutesOrNull returns null for sub-minute durations`() {
        assertNull(durationDisplayMinutesOrNull(59f / 60f))
    }

    // formatRemainingTimeRoundUp — ceiling to nearest second
    @Test
    fun `formatRemainingTimeRoundUp rounds 1001ms up to two seconds`() {
        assertEquals("00:02", formatRemainingTimeRoundUp(1001))
    }

    @Test
    fun `formatRemainingTimeRoundUp keeps exact 1000ms as one second`() {
        assertEquals("00:01", formatRemainingTimeRoundUp(1000))
    }

    @Test
    fun `formatRemainingTimeRoundUp rounds 999ms up to one second`() {
        assertEquals("00:01", formatRemainingTimeRoundUp(999))
    }

    @Test
    fun `formatRemainingTimeRoundUp rounds 1ms up to one second`() {
        assertEquals("00:01", formatRemainingTimeRoundUp(1))
    }

    @Test
    fun `formatRemainingTimeRoundUp returns zero for 0ms`() {
        assertEquals("00:00", formatRemainingTimeRoundUp(0))
    }

    @Test
    fun `formatRemainingTimeRoundUp clamps negative to zero`() {
        assertEquals("00:00", formatRemainingTimeRoundUp(-5000))
    }

    // anticipatedProgressMs — progress target for next sync moment
    @Test
    fun `anticipatedProgressMs subtracts full sync interval when remaining exceeds it`() {
        assertEquals(4000L, anticipatedProgressMs(5000L, 1000L))
    }

    @Test
    fun `anticipatedProgressMs clamps to zero on final partial interval`() {
        assertEquals(0L, anticipatedProgressMs(500L, 1000L))
    }

    @Test
    fun `anticipatedProgressMs returns zero when remaining equals sync interval`() {
        assertEquals(0L, anticipatedProgressMs(1000L, 1000L))
    }

    @Test
    fun `anticipatedProgressMs returns zero when remaining is zero`() {
        assertEquals(0L, anticipatedProgressMs(0L, 1000L))
    }

    @Test
    fun `anticipatedProgressMs clamps to zero for negative remaining`() {
        assertEquals(0L, anticipatedProgressMs(-500L, 1000L))
    }

    @Test
    fun `snooze options contains expected durations in order`() {
        assertEquals(listOf(1f, 5f, 10f), SNOOZE_OPTIONS)
    }

    @Test
    fun `snooze options has exactly three entries`() {
        assertEquals(3, SNOOZE_OPTIONS.size)
    }

    // parseCustomDurationSeconds — whole minutes
    @Test
    fun `parseCustomDurationSeconds whole minutes returns correct seconds`() {
        assertEquals(25 * 60, parseCustomDurationSeconds("25"))
    }

    @Test
    fun `parseCustomDurationSeconds single digit whole minute`() {
        assertEquals(5 * 60, parseCustomDurationSeconds("5"))
    }

    @Test
    fun `parseCustomDurationSeconds zero whole minutes returns zero`() {
        assertEquals(0, parseCustomDurationSeconds("0"))
    }

    // parseCustomDurationSeconds — 0:ss
    @Test
    fun `parseCustomDurationSeconds rejects non-zero minute field with colon`() {
        assertNull(parseCustomDurationSeconds("12:30"))
    }

    @Test
    fun `parseCustomDurationSeconds zero minutes with seconds`() {
        assertEquals(5, parseCustomDurationSeconds("0:05"))
    }

    // parseCustomDurationSeconds — seconds clamping
    @Test
    fun `parseCustomDurationSeconds clamps seconds above 59`() {
        assertEquals(59, parseCustomDurationSeconds("0:99"))
    }

    @Test
    fun `parseCustomDurationSeconds clamps zero-minute seconds exactly 60 to 59`() {
        assertEquals(59, parseCustomDurationSeconds("0:60"))
    }

    // parseCustomDurationSeconds — decimal rejection
    @Test
    fun `parseCustomDurationSeconds rejects decimal point`() {
        assertNull(parseCustomDurationSeconds("12.5"))
    }

    @Test
    fun `parseCustomDurationSeconds rejects decimal comma`() {
        assertNull(parseCustomDurationSeconds("12,5"))
    }

    // parseCustomDurationSeconds — invalid / empty
    @Test
    fun `parseCustomDurationSeconds rejects empty string`() {
        assertNull(parseCustomDurationSeconds(""))
    }

    @Test
    fun `parseCustomDurationSeconds rejects trailing colon only`() {
        assertNull(parseCustomDurationSeconds("12:"))
    }

    @Test
    fun `parseCustomDurationSeconds rejects leading colon only`() {
        assertNull(parseCustomDurationSeconds(":30"))
    }

    @Test
    fun `parseCustomDurationSeconds rejects non-numeric`() {
        assertNull(parseCustomDurationSeconds("abc"))
    }

    // isCustomDurationInRange
    @Test
    fun `isCustomDurationInRange accepts minimum 5 seconds`() {
        assertTrue(isCustomDurationInRange(5))
    }

    @Test
    fun `isCustomDurationInRange accepts maximum 7200 seconds`() {
        assertTrue(isCustomDurationInRange(7200))
    }

    @Test
    fun `isCustomDurationInRange rejects 4 seconds`() {
        assertFalse(isCustomDurationInRange(4))
    }

    @Test
    fun `isCustomDurationInRange rejects 0 seconds`() {
        assertFalse(isCustomDurationInRange(0))
    }

    @Test
    fun `isCustomDurationInRange rejects 7201 seconds`() {
        assertFalse(isCustomDurationInRange(7201))
    }

    @Test
    fun `isCustomDurationInRange accepts mid-range value`() {
        assertTrue(isCustomDurationInRange(30 * 60))
    }

    // appendToBuffer — keypad input logic
    @Test
    fun `appendToBuffer appends digit within limit`() {
        assertEquals("12", appendToBuffer("1", "2"))
    }

    @Test
    fun `appendToBuffer rejects digit at three-digit limit`() {
        assertEquals("123", appendToBuffer("123", "4"))
    }

    @Test
    fun `appendToBuffer inserts colon after zero minute field`() {
        assertEquals("0:", appendToBuffer("0", ":"))
        assertEquals("00:", appendToBuffer("00", ":"))
    }

    @Test
    fun `appendToBuffer rejects colon after non-zero minute field`() {
        assertEquals("12", appendToBuffer("12", ":"))
    }

    @Test
    fun `appendToBuffer rejects colon when already present`() {
        assertEquals("0:3", appendToBuffer("0:3", ":"))
    }

    @Test
    fun `appendToBuffer removes last character on backspace`() {
        assertEquals("12", appendToBuffer("123", "⌫"))
    }

    @Test
    fun `appendToBuffer no-op backspace on empty buffer`() {
        assertEquals("", appendToBuffer("", "⌫"))
    }

    @Test
    fun `appendToBuffer rejects digit when seconds field already has two digits`() {
        assertEquals("0:30", appendToBuffer("0:30", "5"))
    }

    @Test
    fun `appendToBuffer allows second digit in seconds field`() {
        assertEquals("0:35", appendToBuffer("0:3", "5"))
    }
}
