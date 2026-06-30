package me.easynap.timer

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.easynap.data.PersistedTimer
import me.easynap.data.TimerStore

class FakeTimerStore(
    initialHistory: List<Int> = listOf(300, 600, 1800),
    var activeTimer: PersistedTimer? = null
) : TimerStore {

    private val _history = MutableStateFlow(initialHistory)
    override val history: Flow<List<Int>> = _history

    private val _napDurationSeconds = MutableStateFlow(0)
    override val napDurationSeconds: Flow<Int> = _napDurationSeconds

    var loadActiveTimerCallCount = 0

    override suspend fun loadActiveTimer(nowMillis: Long): PersistedTimer? {
        loadActiveTimerCallCount++
        return activeTimer
    }

    override suspend fun getNapDurationSeconds(): Int = _napDurationSeconds.value

    override suspend fun startTimer(endAtMillis: Long, durationSeconds: Int, updateNapDuration: Boolean) {
        activeTimer = PersistedTimer(endAtMillis, durationSeconds)
        if (updateNapDuration) _napDurationSeconds.value = durationSeconds
    }

    override suspend fun clearActiveTimer() {
        activeTimer = null
    }

    override suspend fun addToHistory(seconds: Int, position: Int) {
        val current = _history.value.filterNot { it == seconds }
        val clamped = minOf(position, current.size)
        _history.value = (current.subList(0, clamped) + seconds + current.subList(clamped, current.size)).take(5)
    }

    override suspend fun removeFromHistory(seconds: Int) {
        _history.value = _history.value.filterNot { it == seconds }
    }
}
