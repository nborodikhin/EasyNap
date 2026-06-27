package me.easynap.data

import kotlinx.coroutines.flow.Flow

data class PersistedTimer(
    val endAtMillis: Long,
    val durationMinutes: Float
)

interface TimerStore {
    val history: Flow<List<Float>>
    val napDurationMinutes: Flow<Float>
    suspend fun loadActiveTimer(nowMillis: Long = System.currentTimeMillis()): PersistedTimer?
    suspend fun getNapDurationMinutes(): Float
    suspend fun startTimer(endAtMillis: Long, durationMinutes: Float, updateNapDuration: Boolean = true)
    suspend fun clearActiveTimer()
    suspend fun addToHistory(minutes: Float, position: Int)
    suspend fun removeFromHistory(minutes: Float)
}
