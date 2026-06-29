package me.easynap.timer

import kotlin.math.roundToInt

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

fun isCustomDurationInRange(seconds: Int): Boolean = seconds in 5..7200

fun durationTotalSeconds(minutes: Float): Int = (minutes * 60f).roundToInt()

fun durationDisplayMinutesOrNull(minutes: Float): Int? {
    val totalSeconds = durationTotalSeconds(minutes)
    return if (totalSeconds >= 60) totalSeconds / 60 else null
}

// Appends a keypad key to the input buffer, enforcing max-length rules.
fun appendToBuffer(buffer: String, key: String): String = when (key) {
    "⌫" -> if (buffer.isNotEmpty()) buffer.dropLast(1) else buffer
    ":" -> if (canAppendColon(buffer)) buffer + ":" else buffer
    else -> {
        if (':' in buffer) {
            if (buffer.substringAfter(':').length >= 2) buffer else buffer + key
        } else {
            if (buffer.length >= 3) buffer else buffer + key
        }
    }
}

fun canAppendColon(buffer: String): Boolean {
    if (buffer.isEmpty() || ':' in buffer) return false
    return buffer.toIntOrNull() == 0
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

fun formatDurationLabel(minutes: Float): String =
    if (minutes % 1f == 0f) {
        "${minutes.toInt()}"
    } else {
        val totalSeconds = (minutes * 60f).toInt()
        "%d:%02d".format(java.util.Locale.ROOT, totalSeconds / 60, totalSeconds % 60)
    }

val SNOOZE_OPTIONS: List<Float> = listOf(1f, 5f, 10f)
