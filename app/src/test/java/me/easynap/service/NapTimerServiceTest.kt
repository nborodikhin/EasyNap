package me.easynap.service

import android.app.NotificationManager
import android.content.Context
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
@Config(application = HiltTestApplication::class, packageName = "me.easynap")
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
        fakeStore.activeTimer = PersistedTimer(System.currentTimeMillis() + 10 * 60_000L, 10f)

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
}
