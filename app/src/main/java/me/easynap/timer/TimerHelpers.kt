package me.easynap.timer

import android.content.res.Resources
import me.easynap.R

fun Resources.notifDurationPrefix(durationSeconds: Int): String {
    val wholeMinutes = durationDisplayMinutesOrNull(durationSeconds)
    return if (wholeMinutes != null) {
        getQuantityString(R.plurals.notif_duration_min, wholeMinutes, wholeMinutes)
    } else {
        val seconds = durationSeconds.coerceAtLeast(0)
        getQuantityString(R.plurals.notif_duration_sec, seconds, seconds)
    }
}

fun parseDurationMinutes(input: String): Float? {
    val v = input.trim().toFloatOrNull() ?: return null
    return if (v > 0f) v else null
}

// Parses whole minutes ("25") or sub-minute seconds ("0:30") to total seconds.
// Returns null for empty, decimal, or incomplete input.
// Clamps seconds field to 0–59.
fun parseCustomDurationSeconds(input: String): Int? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null
    if (trimmed.contains('.') || trimmed.contains(',')) return null
    return if (':' in trimmed) {
        val idx = trimmed.indexOf(':')
        val mmStr = trimmed.substring(0, idx)
        val ssStr = trimmed.substring(idx + 1)
        if (mmStr.isEmpty() || ssStr.isEmpty()) return null
        val mm = mmStr.toIntOrNull() ?: return null
        if (mm != 0) return null
        val rawSs = ssStr.toIntOrNull() ?: return null
        val ss = rawSs.coerceIn(0, 59)
        mm * 60 + ss
    } else {
        val minutes = trimmed.toIntOrNull() ?: return null
        if (minutes < 0) return null
        minutes * 60
    }
}

const val MAX_CUSTOM_DURATION_MINUTES = 120

fun isCustomDurationInRange(seconds: Int): Boolean = seconds in 5..(MAX_CUSTOM_DURATION_MINUTES * 60)

fun durationDisplayMinutesOrNull(seconds: Int): Int? =
    if (seconds >= 60) seconds / 60 else null

// The 7 states a custom-duration buffer can be in; drives which keys are legal next.
enum class KeypadState {
    EMPTY, SUBMINUTE_START, SUBMINUTE_TENS, SUBMINUTE_FULL, MINUTE_ONES, MINUTE_TENS, MINUTE_HUNDREDS
}

fun keypadState(buffer: String): KeypadState = when {
    buffer.isEmpty() -> KeypadState.EMPTY
    buffer == "0" -> KeypadState.SUBMINUTE_START
    ':' in buffer -> if (buffer.substringAfter(':').length >= 2) {
        KeypadState.SUBMINUTE_FULL
    } else {
        KeypadState.SUBMINUTE_TENS
    }
    buffer.length == 1 -> KeypadState.MINUTE_ONES
    buffer.length == 2 -> KeypadState.MINUTE_TENS
    else -> KeypadState.MINUTE_HUNDREDS
}

// Whether pressing `digit` is legal for a buffer classified as `state`.
fun isDigitAllowed(state: KeypadState, buffer: String, digit: Int): Boolean = when (state) {
    KeypadState.EMPTY, KeypadState.MINUTE_ONES -> true
    KeypadState.SUBMINUTE_START -> digit in 0..5
    KeypadState.SUBMINUTE_TENS -> {
        val tensIsZero = buffer.last() == '0'
        digit in (if (tensIsZero) 5 else 0)..9
    }
    KeypadState.MINUTE_TENS -> buffer.toInt() * 10 + digit <= MAX_CUSTOM_DURATION_MINUTES
    KeypadState.SUBMINUTE_FULL, KeypadState.MINUTE_HUNDREDS -> false
}

// Appends a keypad key to the input buffer, rejecting digits invalid for the current state.
// Colon is auto-inserted when leaving SUBMINUTE_START rather than typed directly.
fun appendToBuffer(buffer: String, key: String): String {
    if (key == "⌫") return if (buffer.isNotEmpty()) buffer.dropLast(1) else buffer
    val digit = key.toIntOrNull() ?: return buffer
    val state = keypadState(buffer)
    if (!isDigitAllowed(state, buffer, digit)) return buffer
    return if (state == KeypadState.SUBMINUTE_START) "$buffer:$digit" else buffer + digit
}

fun formatRemainingTime(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(java.util.Locale.ROOT, minutes, seconds)
}

fun formatRemainingTimeRoundUp(remainingMs: Long): String {
    val totalSeconds = (remainingMs.coerceAtLeast(0) + 999) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(java.util.Locale.ROOT, minutes, seconds)
}

fun anticipatedProgressMs(remainingMs: Long, syncIntervalMs: Long): Long =
    (remainingMs - minOf(syncIntervalMs, remainingMs)).coerceAtLeast(0)

fun formatDurationLabel(seconds: Int): String =
    if (seconds % 60 == 0) {
        "${seconds / 60}"
    } else {
        "%d:%02d".format(java.util.Locale.ROOT, seconds / 60, seconds % 60)
    }

val SNOOZE_OPTIONS: List<Int> = listOf(60, 300, 600)
