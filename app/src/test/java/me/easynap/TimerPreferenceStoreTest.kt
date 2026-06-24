package me.easynap

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerPreferenceStoreTest {

    private val tempFiles = mutableListOf<File>()

    @After
    fun tearDown() {
        tempFiles.forEach { it.delete() }
    }

    @Test
    fun `history is seeded when no stored history exists`() = runTest {
        val store = newStore("seeded-history")

        assertEquals(listOf(5f, 10f, 30f), store.history.first())
    }

    @Test
    fun `start timer persists active timer and duration`() = runTest {
        val store = newStore("active-timer")
        val endAt = 2_000L

        store.startTimer(endAtMillis = endAt, durationMinutes = 12.5f, updateHistory = true)

        assertEquals(PersistedTimer(endAt, 12.5f), store.loadActiveTimer(nowMillis = 1_000L))
        assertEquals(12.5f, store.getNapDurationMinutes())
    }

    @Test
    fun `expired active timer is ignored`() = runTest {
        val store = newStore("expired-timer")

        store.startTimer(endAtMillis = 2_000L, durationMinutes = 10f, updateHistory = false)

        assertNull(store.loadActiveTimer(nowMillis = 2_000L))
    }

    @Test
    fun `history dedupes moves selected duration to front and caps at six`() = runTest {
        val store = newStore("history-rules")

        listOf(5f, 10f, 30f, 45f, 60f, 90f, 10f).forEachIndexed { index, minutes ->
            store.startTimer(
                endAtMillis = 10_000L + index,
                durationMinutes = minutes,
                updateHistory = true
            )
        }

        assertEquals(listOf(10f, 90f, 60f, 45f, 30f, 5f), store.history.first())
    }

    @Test
    fun `migration helper preserves existing SharedPreferences values`() {
        val migratedRawHistory = "25,10,5"

        assertEquals(listOf(25f, 10f, 5f), TimerPreferenceStore.parseHistory(migratedRawHistory))
        assertTrue(TimerPreferenceStore.withSeedDurations(listOf(25f, 10f, 5f)).containsAll(listOf(25f, 10f, 5f)))
    }

    private fun TestScope.newStore(name: String): TimerPreferenceStore {
        val file = File.createTempFile(name, ".preferences_pb").also { tempFiles += it }
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        return TimerPreferenceStore(dataStore)
    }
}
