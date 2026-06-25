package me.easynap

import android.content.Intent
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import me.easynap.alarm.AlarmActivity
import me.easynap.alarm.AlarmService
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
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@UninstallModules(AppModule::class)
@Config(application = HiltTestApplication::class, packageName = "me.easynap")
@RunWith(RobolectricTestRunner::class)
class MainActivityAlarmRecoveryTest {

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
    fun `onResume starts AlarmActivity when AlarmService is running`() {
        AlarmService.isRunning = true

        val controller = Robolectric.buildActivity(MainActivity::class.java)
        controller.create().start().resume()

        assertTrue(
            "Expected AlarmActivity to be started",
            drainStartedActivities(controller.get())
                .any { it.component?.className == AlarmActivity::class.java.name }
        )
    }

    @Test
    fun `onResume does not start AlarmActivity when AlarmService is not running`() {
        AlarmService.isRunning = false

        val controller = Robolectric.buildActivity(MainActivity::class.java)
        controller.create().start().resume()

        assertFalse(
            "Expected AlarmActivity NOT to be started",
            drainStartedActivities(controller.get())
                .any { it.component?.className == AlarmActivity::class.java.name }
        )
    }

    private fun drainStartedActivities(activity: android.app.Activity): List<Intent> {
        val shadow = shadowOf(activity)
        val result = mutableListOf<Intent>()
        var intent = shadow.nextStartedActivity
        while (intent != null) {
            result.add(intent)
            intent = shadow.nextStartedActivity
        }
        return result
    }
}
