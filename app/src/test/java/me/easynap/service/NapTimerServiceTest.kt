package me.easynap.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import me.easynap.AppModule
import me.easynap.data.PersistedTimer
import me.easynap.data.TimerStore
import me.easynap.timer.FakeTimerStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
class NapTimerServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    val fakeStore = FakeTimerStore()

    @BindValue @JvmField
    val timerStore: TimerStore = fakeStore

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `service consults timer store on start when active timer exists`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 10 * 60_000L, 600)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        assertTrue("Service should load active timer from store",
            fakeStore.loadActiveTimerCallCount > 0)
    }

    @Test
    fun `service posts no notification when no active timer exists`() {
        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        assertTrue("Expected no notifications when no active timer",
            shadowOf(nm).allNotifications.isEmpty())
    }

    @Test
    fun `notification title shows whole-minute duration as N min nap is active`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 20 * 60_000L, 1200)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = shadowOf(nm).allNotifications.firstOrNull()
            ?.extras?.getString(Notification.EXTRA_TITLE)
        assertEquals("20 min nap is active", title)
    }

    @Test
    fun `notification title floors duration over one minute to whole minutes`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 90_000L, 90)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = shadowOf(nm).allNotifications.firstOrNull()
            ?.extras?.getString(Notification.EXTRA_TITLE)
        assertEquals("1 min nap is active", title)
    }

    @Test
    fun `notification title shows sub-minute duration as seconds`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 30_000L, 30)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val title = shadowOf(nm).allNotifications.firstOrNull()
            ?.extras?.getString(Notification.EXTRA_TITLE)
        assertEquals("30 sec nap is active", title)
    }

    @Test
    fun `notification has a single Stop action`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 10 * 60_000L, 600)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        val context = ApplicationProvider.getApplicationContext<Context>()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val actions = shadowOf(nm).allNotifications.firstOrNull()?.actions
        assertEquals(1, actions?.size)
        assertEquals("Stop", actions?.get(0)?.title?.toString())
    }

    @Test
    fun `ACTION_STOP clears the active timer`() {
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 10 * 60_000L, 600)

        val service = Robolectric.buildService(NapTimerService::class.java).create().get()
        service.onStartCommand(null, 0, 1)
        shadowOf(Looper.getMainLooper()).idle()

        service.onStartCommand(Intent(NapTimerService.ACTION_STOP), 0, 2)
        shadowOf(Looper.getMainLooper()).idle()

        assertNull("Active timer should be cleared after Stop action", fakeStore.activeTimer)
    }
}
