package me.easynap.data

import kotlinx.coroutines.flow.Flow

data class PersistedTimer(
    val endAtMillis: Long,
    val durationSeconds: Int
)

interface TimerStore {
    val history: Flow<List<Int>>
    val napDurationSeconds: Flow<Int>
    suspend fun loadActiveTimer(nowMillis: Long = System.currentTimeMillis()): PersistedTimer?
    suspend fun getNapDurationSeconds(): Int
    suspend fun startTimer(endAtMillis: Long, durationSeconds: Int, updateNapDuration: Boolean = true)
    suspend fun clearActiveTimer()
    suspend fun addToHistory(seconds: Int, position: Int)
    suspend fun removeFromHistory(seconds: Int)
}
