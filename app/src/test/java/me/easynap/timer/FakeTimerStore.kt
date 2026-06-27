package me.easynap.timer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.easynap.data.PersistedTimer
import me.easynap.data.TimerStore

class FakeTimerStore(
    initialHistory: List<Float> = listOf(5f, 10f, 30f),
    var activeTimer: PersistedTimer? = null
) : TimerStore {

    private val _history = MutableStateFlow(initialHistory)
    override val history: Flow<List<Float>> = _history

    private val _napDurationMinutes = MutableStateFlow(0f)
    override val napDurationMinutes: Flow<Float> = _napDurationMinutes

    var loadActiveTimerCallCount = 0

    override suspend fun loadActiveTimer(nowMillis: Long): PersistedTimer? {
        loadActiveTimerCallCount++
        return activeTimer
    }

    override suspend fun getNapDurationMinutes(): Float = _napDurationMinutes.value

    override suspend fun startTimer(endAtMillis: Long, durationMinutes: Float) {
        activeTimer = PersistedTimer(endAtMillis, durationMinutes)
        _napDurationMinutes.value = durationMinutes
    }

    override suspend fun clearActiveTimer() {
        activeTimer = null
    }

    override suspend fun addToHistory(minutes: Float, position: Int) {
        val current = _history.value.filterNot { it == minutes }
        val clamped = minOf(position, current.size)
        _history.value = (current.subList(0, clamped) + minutes + current.subList(clamped, current.size)).take(5)
    }

    override suspend fun removeFromHistory(minutes: Float) {
        _history.value = _history.value.filterNot { it == minutes }
    }
}
