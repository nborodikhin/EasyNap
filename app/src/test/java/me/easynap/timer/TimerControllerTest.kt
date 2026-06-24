package me.easynap.timer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        controller.start(10f)
        advanceUntilIdle()
        assertTrue(controller.state.value is TimerState.Running)
    }

    @Test
    fun `cancel transitions state back to Idle`() = runTest {
        controller.start(10f)
        advanceUntilIdle()
        controller.cancel()
        assertEquals(TimerState.Idle, controller.state.value)
    }

    @Test
    fun `startSnooze produces Running state with isSnooze true`() = runTest {
        controller.startSnooze(5f)
        advanceUntilIdle()
        val state = controller.state.value
        assertTrue(state is TimerState.Running && state.isSnooze)
    }

    @Test
    fun `start does not set isSnooze`() = runTest {
        controller.start(10f)
        advanceUntilIdle()
        val state = controller.state.value
        assertTrue(state is TimerState.Running && !state.isSnooze)
    }

    @Test
    fun `completeTimer transitions state to Idle`() = runTest {
        controller.start(5f)
        advanceUntilIdle()
        controller.completeTimer()
        assertEquals(TimerState.Idle, controller.state.value)
    }
}
