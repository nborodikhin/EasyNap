package me.easynap

sealed class TimerState {
    object Idle : TimerState()
    data class Running(val endAtMillis: Long) : TimerState()
}
