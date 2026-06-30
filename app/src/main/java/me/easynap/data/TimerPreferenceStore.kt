package me.easynap.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
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

@Singleton
class TimerPreferenceStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TimerStore {

    override val napDurationSeconds: Flow<Int> = dataStore.safeData.map { preferences ->
        preferences[KEY_DURATION_SECONDS] ?: 0
    }

    override val history: Flow<List<Int>> = dataStore.safeData.map { preferences ->
        val raw = preferences[KEY_HISTORY_SECONDS]
        if (raw == null) DEFAULT_HISTORY else parseHistory(raw)
    }

    override suspend fun loadActiveTimer(nowMillis: Long): PersistedTimer? {
        return dataStore.safeData.first().toActiveTimer(nowMillis)
    }

    override suspend fun getNapDurationSeconds(): Int {
        return napDurationSeconds.first()
    }

    override suspend fun startTimer(endAtMillis: Long, durationSeconds: Int, updateNapDuration: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_END_AT] = endAtMillis
            if (updateNapDuration) preferences[KEY_DURATION_SECONDS] = durationSeconds
        }
    }

    override suspend fun addToHistory(seconds: Int, position: Int) {
        dataStore.edit { preferences ->
            val current = preferences.parseStoredHistory().filterNot { it == seconds }
            val clamped = minOf(position, current.size)
            val updated = (current.subList(0, clamped) + seconds + current.subList(clamped, current.size)).take(5)
            preferences[KEY_HISTORY_SECONDS] = updated.joinToString(",")
        }
    }

    override suspend fun removeFromHistory(seconds: Int) {
        dataStore.edit { preferences ->
            val current = preferences.parseStoredHistory()
            if (seconds !in current) return@edit
            preferences[KEY_HISTORY_SECONDS] = current.filterNot { it == seconds }.joinToString(",")
        }
    }

    override suspend fun clearActiveTimer() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_END_AT)
        }
    }

    companion object {
        const val LEGACY_PREFS_NAME = "easynap_prefs"

        val DEFAULT_HISTORY = listOf(300, 600, 1800)

        private val KEY_END_AT = longPreferencesKey("end_at_millis")
        private val KEY_DURATION_SECONDS = intPreferencesKey("nap_duration_seconds")
        private val KEY_HISTORY_SECONDS = stringPreferencesKey("duration_history_seconds")

        fun parseHistory(raw: String?): List<Int> {
            return raw
                ?.split(",")
                ?.mapNotNull { it.trim().toIntOrNull() }
                ?: emptyList()
        }

        private fun Preferences.toActiveTimer(nowMillis: Long): PersistedTimer? {
            val endAtMillis = this[KEY_END_AT] ?: return null
            if (endAtMillis <= nowMillis) return null
            return PersistedTimer(
                endAtMillis = endAtMillis,
                durationSeconds = this[KEY_DURATION_SECONDS] ?: 0
            )
        }

        private fun Preferences.parseStoredHistory(): List<Int> {
            val raw = this[KEY_HISTORY_SECONDS] ?: return DEFAULT_HISTORY
            return parseHistory(raw)
        }
    }
}

private val DataStore<Preferences>.safeData: Flow<Preferences>
    get() = data.catch { exception ->
        when (exception) {
            is CancellationException -> throw exception
            is IOException -> emit(emptyPreferences())
            else -> throw exception
        }
    }
