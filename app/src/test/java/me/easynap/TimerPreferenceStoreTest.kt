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

        assertEquals(listOf(300, 600, 1800), store.history.first())
    }

    @Test
    fun `start timer persists active timer and duration`() = runTest {
        val store = newStore("active-timer")
        val endAt = 2_000L

        store.startTimer(endAtMillis = endAt, durationSeconds = 750)

        assertEquals(PersistedTimer(endAt, 750), store.loadActiveTimer(nowMillis = 1_000L))
        assertEquals(750, store.getNapDurationSeconds())
    }

    @Test
    fun `expired active timer is ignored`() = runTest {
        val store = newStore("expired-timer")

        store.startTimer(endAtMillis = 2_000L, durationSeconds = 600)

        assertNull(store.loadActiveTimer(nowMillis = 2_000L))
    }

    @Test
    fun `seed durations are preserved after first use`() = runTest {
        val store = newStore("seed-preserved")

        store.addToHistory(1200, 0)

        val history = store.history.first()
        assertTrue(1200 in history)
        assertTrue(300 in history)
        assertTrue(600 in history)
        assertTrue(1800 in history)
    }

    // addToHistory tests

    @Test
    fun `addToHistory dedupes moves selected duration to front and caps at five`() = runTest {
        val store = newStore("history-rules")

        listOf(300, 600, 1800, 2700, 3600, 5400, 600).forEach { store.addToHistory(it, 0) }

        assertEquals(listOf(600, 5400, 3600, 2700, 1800), store.history.first())
    }

    @Test
    fun `addToHistory at position 0 prepends to history`() = runTest {
        val store = newStore("add-position-0")

        store.addToHistory(900, 0)

        val history = store.history.first()
        assertEquals(900, history.first())
    }

    @Test
    fun `addToHistory clamps position when list is shorter`() = runTest {
        val store = newStore("add-clamp")
        // fresh store has DEFAULT_HISTORY [300, 600, 1800] (3 items)
        store.addToHistory(5940, 100)

        val history = store.history.first()
        assertEquals(5940, history.last())
    }

    @Test
    fun `addToHistory deduplicates existing occurrence before inserting`() = runTest {
        val store = newStore("add-dedup")

        store.addToHistory(300, 2) // 300 is already in DEFAULT_HISTORY at index 0

        val history = store.history.first()
        assertEquals(1, history.count { it == 300 })
        assertEquals(300, history[2])
    }

    @Test
    fun `addToHistory caps list at five entries`() = runTest {
        val store = newStore("add-cap")

        listOf(60, 120, 180, 240, 300, 360).forEach { store.addToHistory(it, 0) }

        assertEquals(5, store.history.first().size)
    }

    // removeFromHistory tests

    @Test
    fun `removeFromHistory removes present value and persists`() = runTest {
        val store = newStore("remove-present")

        store.removeFromHistory(600)

        assertFalse(600 in store.history.first())
    }

    @Test
    fun `removeFromHistory is no-op for absent value`() = runTest {
        val store = newStore("remove-absent")
        val before = store.history.first()

        store.removeFromHistory(5940)

        assertEquals(before, store.history.first())
    }

    @Test
    fun `parseHistory parses comma-separated integers`() {
        assertEquals(listOf(1500, 600, 300), TimerPreferenceStore.parseHistory("1500,600,300"))
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
