package me.easynap.timer

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val endAtMillis: Long, val durationMinutes: Float, val isSnooze: Boolean = false) : TimerState()
}
