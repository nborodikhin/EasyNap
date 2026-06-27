package me.easynap

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import me.easynap.data.PersistedTimer
import me.easynap.data.TimerPreferenceStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

        store.startTimer(endAtMillis = endAt, durationMinutes = 12.5f)

        assertEquals(PersistedTimer(endAt, 12.5f), store.loadActiveTimer(nowMillis = 1_000L))
        assertEquals(12.5f, store.getNapDurationMinutes())
    }

    @Test
    fun `expired active timer is ignored`() = runTest {
        val store = newStore("expired-timer")

        store.startTimer(endAtMillis = 2_000L, durationMinutes = 10f)

        assertNull(store.loadActiveTimer(nowMillis = 2_000L))
    }

    @Test
    fun `seed durations are preserved after first use`() = runTest {
        val store = newStore("seed-preserved")

        store.addToHistory(20f, 0)

        val history = store.history.first()
        assertTrue(20f in history)
        assertTrue(5f in history)
        assertTrue(10f in history)
        assertTrue(30f in history)
    }

    // addToHistory tests (task 10.1)

    @Test
    fun `addToHistory dedupes moves selected duration to front and caps at five`() = runTest {
        val store = newStore("history-rules")

        listOf(5f, 10f, 30f, 45f, 60f, 90f, 10f).forEach { store.addToHistory(it, 0) }

        assertEquals(listOf(10f, 90f, 60f, 45f, 30f), store.history.first())
    }

    @Test
    fun `addToHistory at position 0 prepends to history`() = runTest {
        val store = newStore("add-position-0")

        store.addToHistory(15f, 0)

        val history = store.history.first()
        assertEquals(15f, history.first())
    }

    @Test
    fun `addToHistory clamps position when list is shorter`() = runTest {
        val store = newStore("add-clamp")
        // fresh store has DEFAULT_HISTORY [5, 10, 30] (3 items)
        store.addToHistory(99f, 100)

        val history = store.history.first()
        assertEquals(99f, history.last())
    }

    @Test
    fun `addToHistory deduplicates existing occurrence before inserting`() = runTest {
        val store = newStore("add-dedup")

        store.addToHistory(5f, 2) // 5 is already in DEFAULT_HISTORY at index 0

        val history = store.history.first()
        assertEquals(1, history.count { it == 5f })
        assertEquals(5f, history[2])
    }

    @Test
    fun `addToHistory caps list at five entries`() = runTest {
        val store = newStore("add-cap")

        listOf(1f, 2f, 3f, 4f, 5f, 6f).forEach { store.addToHistory(it, 0) }

        assertEquals(5, store.history.first().size)
    }

    // removeFromHistory tests (task 10.2)

    @Test
    fun `removeFromHistory removes present value and persists`() = runTest {
        val store = newStore("remove-present")

        store.removeFromHistory(10f)

        assertFalse(10f in store.history.first())
    }

    @Test
    fun `removeFromHistory is no-op for absent value`() = runTest {
        val store = newStore("remove-absent")
        val before = store.history.first()

        store.removeFromHistory(99f)

        assertEquals(before, store.history.first())
    }

    @Test
    fun `migration helper preserves existing SharedPreferences values`() {
        val migratedRawHistory = "25,10,5"

        assertEquals(listOf(25f, 10f, 5f), TimerPreferenceStore.parseHistory(migratedRawHistory))
    }

    @Test(expected = CancellationException::class)
    fun `data store cancellation is rethrown`() = runTest {
        val store = TimerPreferenceStore(CancellingDataStore)

        store.history.first()
    }

    private fun TestScope.newStore(name: String): TimerPreferenceStore {
        val file = File.createTempFile(name, ".preferences_pb").also { tempFiles += it }
        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { file }
        )
        return TimerPreferenceStore(dataStore)
    }

    private object CancellingDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow {
            throw CancellationException("cancelled")
        }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            throw UnsupportedOperationException("Not used")
        }
    }
}
