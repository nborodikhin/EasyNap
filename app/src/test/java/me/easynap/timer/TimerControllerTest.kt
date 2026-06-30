package me.easynap.timer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TimerControllerTest {

    private lateinit var fakeStore: FakeTimerStore
    private lateinit var controller: TimerController

    @Before
    fun setUp() {
        fakeStore = FakeTimerStore()
        val context = ApplicationProvider.getApplicationContext<Context>()
        controller = TimerController(fakeStore, context)
    }

    @Test
    fun `initial state is Idle when no active timer in store`() = runTest {
        advanceUntilIdle()
        assertEquals(TimerState.Idle, controller.state.value)
    }

    @Test
    fun `start transitions state to Running`() = runTest {
        controller.start(600)
        advanceUntilIdle()
        assertTrue(controller.state.value is TimerState.Running)
    }

    @Test
    fun `cancel transitions state back to Idle`() = runTest {
        controller.start(600)
        advanceUntilIdle()
        controller.cancel()
        assertEquals(TimerState.Idle, controller.state.value)
    }

    @Test
    fun `startSnooze produces Running state with isSnooze true`() = runTest {
        controller.startSnooze(300)
        advanceUntilIdle()
        val state = controller.state.value
        assertTrue(state is TimerState.Running && state.isSnooze)
    }

    @Test
    fun `start does not set isSnooze`() = runTest {
        controller.start(600)
        advanceUntilIdle()
        val state = controller.state.value
        assertTrue(state is TimerState.Running && !state.isSnooze)
    }

    @Test
    fun `completeTimer transitions state to Idle`() = runTest {
        controller.start(300)
        advanceUntilIdle()
        controller.completeTimer()
        assertEquals(TimerState.Idle, controller.state.value)
    }

    @Test
    fun `start does not update history`() = runTest {
        val historyBefore = fakeStore.history.first()
        controller.start(5940)
        advanceUntilIdle()
        assertEquals(historyBefore, fakeStore.history.first())
    }

    @Test
    fun `addTimer updates history`() = runTest {
        controller.addTimer(5940, 0)
        advanceUntilIdle()
        assertTrue(5940 in fakeStore.history.first())
    }

    @Test
    fun `removeFromHistory removes duration and undo restores at original position`() = runTest {
        fakeStore = FakeTimerStore(initialHistory = listOf(600, 1200, 1800))
        val context = ApplicationProvider.getApplicationContext<Context>()
        controller = TimerController(fakeStore, context)
        advanceUntilIdle()

        controller.removeFromHistory(1200)
        advanceUntilIdle()
        assertTrue(1200 !in fakeStore.history.first())

        controller.undo()
        advanceUntilIdle()
        assertEquals(1200, fakeStore.history.first()[1])
    }

    @Test
    fun `second undo call is no-op`() = runTest {
        fakeStore = FakeTimerStore(initialHistory = listOf(600, 1200, 1800))
        val context = ApplicationProvider.getApplicationContext<Context>()
        controller = TimerController(fakeStore, context)
        advanceUntilIdle()

        controller.removeFromHistory(1200)
        advanceUntilIdle()
        controller.undo()
        advanceUntilIdle()
        val historyAfterUndo = fakeStore.history.first().toList()

        controller.undo() // second call — no pending undo
        advanceUntilIdle()

        assertEquals(historyAfterUndo, fakeStore.history.first())
    }

    @Test
    fun `undo with no pending delete is no-op`() = runTest {
        val historyBefore = fakeStore.history.first()
        controller.undo()
        advanceUntilIdle()
        assertEquals(historyBefore, fakeStore.history.first())
    }
}
