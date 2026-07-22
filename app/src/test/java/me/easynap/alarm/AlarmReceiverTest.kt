package me.easynap.alarm

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import me.easynap.AppModule
import me.easynap.data.TimerStore
import me.easynap.timer.FakeTimerStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@HiltAndroidTest
@UninstallModules(AppModule::class)
@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
class AlarmReceiverTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @BindValue @JvmField
    val fakeStore: TimerStore = FakeTimerStore()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `onReceive stops NapTimerService and starts AlarmService`() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val receiver = AlarmReceiver()

        receiver.onReceive(application, Intent())

        val shadow = shadowOf(application)
        val startedService = shadow.getNextStartedService()
        assertNotNull("Expected a service to be started", startedService)
        assertEquals(AlarmService::class.java.name, startedService.component?.className)
    }
}
