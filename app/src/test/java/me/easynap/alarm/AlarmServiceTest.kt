package me.easynap.alarm

import android.app.Notification
import android.content.Intent
import android.os.Looper
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.runBlocking
import me.easynap.AppModule
import me.easynap.data.TimerStore
import me.easynap.timer.FakeTimerStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@UninstallModules(AppModule::class)
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class AlarmServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    val fakeStore = FakeTimerStore()

    @BindValue @JvmField
    val timerStore: TimerStore = fakeStore

    @Before
    fun setUp() {
        hiltRule.inject()
        AlarmService.isRunning = false
    }

    @After
    fun tearDown() {
        AlarmService.isRunning = false
    }

    @Test
    fun `isRunning is false before service starts`() {
        assertFalse(AlarmService.isRunning)
    }

    @Test
    fun `isRunning is true after onStartCommand and false after onDestroy`() {
        val controller = Robolectric.buildService(AlarmService::class.java, Intent()).create()
        assertFalse(AlarmService.isRunning)

        controller.startCommand(0, 1)
        assertTrue(AlarmService.isRunning)

        controller.destroy()
        assertFalse(AlarmService.isRunning)
    }

    @Test
    fun `notification title shows whole-minute duration as N min nap is over`() {
        runBlocking { fakeStore.startTimer(System.currentTimeMillis() + 20 * 60_000L, 1200) }
        shadowOf(Looper.getMainLooper()).idle()

        val controller = Robolectric.buildService(AlarmService::class.java, Intent()).create()
        val service = controller.get()
        controller.startCommand(0, 1)

        val title = shadowOf(service).lastForegroundNotification
            ?.extras?.getString(Notification.EXTRA_TITLE)
        assertEquals("20 min nap is over", title)
    }

    @Test
    fun `notification has Snooze as first action and Stop as second`() {
        val controller = Robolectric.buildService(AlarmService::class.java, Intent()).create()
        val service = controller.get()
        controller.startCommand(0, 1)

        val actions = shadowOf(service).lastForegroundNotification?.actions
        assertEquals(2, actions?.size)
        assertEquals("Snooze", actions?.get(0)?.title?.toString())
        assertEquals("Stop", actions?.get(1)?.title?.toString())
    }

    @Test
    fun `alarm starts and stays running when no system alarm sound is available`() {
        // Robolectric has no real RingtoneManager or raw resource support, so both
        // the system URI path and the R.raw.helium path will fail — this verifies
        // the service survives both failures and remains running (vibration-only mode).
        val controller = Robolectric.buildService(AlarmService::class.java, Intent()).create()
        controller.startCommand(0, 1)
        assertTrue("Service should still be running even when audio setup fails", AlarmService.isRunning)
        controller.destroy()
    }

    @Test
    fun `ACTION_SNOOZE stops the alarm and starts a one-minute countdown`() {
        val controller = Robolectric.buildService(AlarmService::class.java, Intent()).create()
        val service = controller.get()
        controller.startCommand(0, 1)
        assertTrue(AlarmService.isRunning)

        service.onStartCommand(Intent(AlarmService.ACTION_SNOOZE), 0, 2)
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse("Alarm should stop after Snooze action", AlarmService.isRunning)
        val timer = fakeStore.activeTimer
        assertNotNull("A snooze countdown should have started", timer)
        assertEquals("Snooze countdown should be 1 minute", 60, timer!!.durationSeconds)
    }
}
