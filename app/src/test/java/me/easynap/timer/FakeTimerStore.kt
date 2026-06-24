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

    override suspend fun startTimer(endAtMillis: Long, durationMinutes: Float, updateHistory: Boolean) {
        activeTimer = PersistedTimer(endAtMillis, durationMinutes)
        if (updateHistory) {
            _napDurationMinutes.value = durationMinutes
        }
    }

    override suspend fun clearActiveTimer() {
        activeTimer = null
    }
}
