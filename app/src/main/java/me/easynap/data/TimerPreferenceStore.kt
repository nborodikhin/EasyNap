package me.easynap.data

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

    override val napDurationMinutes: Flow<Float> = dataStore.safeData.map { preferences ->
        preferences[KEY_DURATION] ?: 0f
    }

    override val history: Flow<List<Float>> = dataStore.safeData.map { preferences ->
        val raw = preferences[KEY_HISTORY]
        if (raw == null) DEFAULT_HISTORY else parseHistory(raw)
    }

    override suspend fun loadActiveTimer(nowMillis: Long): PersistedTimer? {
        return dataStore.safeData.first().toActiveTimer(nowMillis)
    }

    override suspend fun getNapDurationMinutes(): Float {
        return napDurationMinutes.first()
    }

    override suspend fun startTimer(endAtMillis: Long, durationMinutes: Float, updateHistory: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_END_AT] = endAtMillis
            if (updateHistory) {
                preferences[KEY_DURATION] = durationMinutes
                preferences[KEY_HISTORY] = updatedHistory(preferences.parseStoredHistory(), durationMinutes)
                    .joinToString(",")
            }
        }
    }

    override suspend fun clearActiveTimer() {
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
            val raw = this[KEY_HISTORY] ?: return DEFAULT_HISTORY
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
