package me.easynap

fun parseDurationMinutes(input: String): Float? {
    val v = input.trim().toFloatOrNull() ?: return null
    return if (v > 0f) v else null
}

fun formatRemainingTime(remainingMs: Long): String {
    val totalSeconds = (remainingMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

fun formatDurationLabel(minutes: Float): String =
    if (minutes % 1f == 0f) "${minutes.toInt()}" else "$minutes"

fun formatDurationCaption(minutes: Float): String =
    "${formatDurationLabel(minutes)}-minute nap"
