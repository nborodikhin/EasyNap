package me.easynap

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
    fun `snooze options contains expected labels in order`() {
        val labels = SNOOZE_OPTIONS.map { it.first }
        assertEquals(listOf("+1 min", "+5 min", "+10 min"), labels)
    }

    @Test
    fun `snooze options contains expected durations in order`() {
        val durations = SNOOZE_OPTIONS.map { it.second }
        assertEquals(listOf(1f, 5f, 10f), durations)
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

    // parseCustomDurationSeconds — mm:ss
    @Test
    fun `parseCustomDurationSeconds mm colon ss returns correct seconds`() {
        assertEquals(12 * 60 + 30, parseCustomDurationSeconds("12:30"))
    }

    @Test
    fun `parseCustomDurationSeconds zero minutes with seconds`() {
        assertEquals(5, parseCustomDurationSeconds("0:05"))
    }

    // parseCustomDurationSeconds — seconds clamping
    @Test
    fun `parseCustomDurationSeconds clamps seconds above 59`() {
        assertEquals(12 * 60 + 59, parseCustomDurationSeconds("12:99"))
    }

    @Test
    fun `parseCustomDurationSeconds clamps seconds exactly 60 to 59`() {
        assertEquals(1 * 60 + 59, parseCustomDurationSeconds("1:60"))
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
}
