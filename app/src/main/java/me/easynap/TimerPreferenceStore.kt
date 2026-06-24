package me.easynap

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.timerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "timer_preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, TimerPreferenceStore.LEGACY_PREFS_NAME))
    }
)

data class PersistedTimer(
    val endAtMillis: Long,
    val durationMinutes: Float
)

class TimerPreferenceStore(
    private val dataStore: DataStore<Preferences>
) {
    val activeTimer: Flow<PersistedTimer?> = dataStore.safeData.map { preferences ->
        preferences.toActiveTimer(System.currentTimeMillis())
    }

    val napDurationMinutes: Flow<Float> = dataStore.safeData.map { preferences ->
        preferences[KEY_DURATION] ?: 0f
    }

    val history: Flow<List<Float>> = dataStore.safeData.map { preferences ->
        withSeedDurations(preferences.parseStoredHistory())
    }

    val recentHistory: Flow<List<Float>> = dataStore.safeData.map { preferences ->
        preferences.parseStoredHistory().take(3)
    }

    suspend fun loadActiveTimer(nowMillis: Long = System.currentTimeMillis()): PersistedTimer? {
        return dataStore.safeData.first().toActiveTimer(nowMillis)
    }

    suspend fun getNapDurationMinutes(): Float {
        return napDurationMinutes.first()
    }

    suspend fun startTimer(endAtMillis: Long, durationMinutes: Float, updateHistory: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_END_AT] = endAtMillis
            if (updateHistory) {
                preferences[KEY_DURATION] = durationMinutes
                preferences[KEY_HISTORY] = updatedHistory(preferences.parseStoredHistory(), durationMinutes)
                    .joinToString(",")
            }
        }
    }

    suspend fun clearActiveTimer() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_END_AT)
        }
    }

    companion object {
        const val LEGACY_PREFS_NAME = "easynap_prefs"

        val DEFAULT_HISTORY = listOf(5f, 10f, 30f)

        private val KEY_END_AT = longPreferencesKey("end_at_millis")
        private val KEY_DURATION = floatPreferencesKey("nap_duration_minutes")
        private val KEY_HISTORY = stringPreferencesKey("duration_history")

        fun parseHistory(raw: String?): List<Float> {
            return raw
                ?.split(",")
                ?.mapNotNull { it.trim().toFloatOrNull() }
                ?: emptyList()
        }

        fun withSeedDurations(stored: List<Float>): List<Float> {
            return (stored + DEFAULT_HISTORY.filter { it !in stored }).take(6)
        }

        fun updatedHistory(current: List<Float>, minutes: Float): List<Float> {
            return current
                .filterNot { it == minutes }
                .let { listOf(minutes) + it }
                .take(6)
        }

        private fun Preferences.toActiveTimer(nowMillis: Long): PersistedTimer? {
            val endAtMillis = this[KEY_END_AT] ?: return null
            if (endAtMillis <= nowMillis) return null
            return PersistedTimer(
                endAtMillis = endAtMillis,
                durationMinutes = this[KEY_DURATION] ?: 0f
            )
        }

        private fun Preferences.parseStoredHistory(): List<Float> {
            return parseHistory(this[KEY_HISTORY])
        }
    }
}

private val DataStore<Preferences>.safeData: Flow<Preferences>
    get() = data.catch { exception ->
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
