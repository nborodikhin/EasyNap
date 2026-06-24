package me.easynap

fun parseDurationMinutes(input: String): Float? {
    val v = input.trim().toFloatOrNull() ?: return null
    return if (v > 0f) v else null
}

// Parses whole minutes ("25") or mm:ss ("12:30") to total seconds.
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
        if (mm < 0) return null
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

fun formatNapDescription(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    val minuteWord = if (minutes == 1) "minute" else "minutes"
    val secondWord = if (secs == 1) "second" else "seconds"
    return when {
        secs == 0 -> "$minutes $minuteWord"
        minutes == 0 -> "$secs $secondWord"
        else -> "$minutes $minuteWord $secs $secondWord"
    }
}

// Appends a keypad key to the input buffer, enforcing max-length rules.
fun appendToBuffer(buffer: String, key: String): String = when (key) {
    "⌫" -> if (buffer.isNotEmpty()) buffer.dropLast(1) else buffer
    ":" -> if (':' in buffer) buffer else buffer + ":"
    else -> {
        if (':' in buffer) {
            if (buffer.substringAfter(':').length >= 2) buffer else buffer + key
        } else {
            if (buffer.length >= 3) buffer else buffer + key
        }
    }
}

fun formatRemainingTime(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatDurationLabel(minutes: Float): String =
    if (minutes % 1f == 0f) {
        "${minutes.toInt()}"
    } else {
        val totalSeconds = (minutes * 60f).toInt()
        "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
    }

fun formatDurationUnit(minutes: Float): String =
    if (minutes % 1f == 0f) "min" else "min:sec"

fun formatDurationCaption(minutes: Float): String =
    if (minutes % 1f == 0f) {
        "${minutes.toInt()}-minute nap"
    } else {
        val totalSeconds = (minutes * 60f).toInt()
        "%d:%02d nap".format(totalSeconds / 60, totalSeconds % 60)
    }

val SNOOZE_OPTIONS: List<Pair<String, Float>> = listOf(
    "+1 min" to 1f,
    "+5 min" to 5f,
    "+10 min" to 10f
)
