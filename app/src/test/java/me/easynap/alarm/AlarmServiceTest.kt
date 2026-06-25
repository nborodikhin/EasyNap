package me.easynap.alarm

import android.content.Intent
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import me.easynap.AppModule
import me.easynap.data.TimerStore
import me.easynap.timer.FakeTimerStore
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@UninstallModules(AppModule::class)
@Config(application = HiltTestApplication::class, packageName = "me.easynap")
@RunWith(RobolectricTestRunner::class)
class AlarmServiceTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue @JvmField
    val fakeStore: TimerStore = FakeTimerStore()

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
}
