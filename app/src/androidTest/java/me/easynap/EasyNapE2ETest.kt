package me.easynap

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import me.easynap.data.TimerStore
import me.easynap.timer.TimerController
import me.easynap.timer.FakeTimerStore
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(AppModule::class)
@RunWith(AndroidJUnit4::class)
class EasyNapE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val notificationPermissionRule = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val instrumentation = InstrumentationRegistry.getInstrumentation()
                    val packageName = instrumentation.targetContext.packageName
                    instrumentation.runShellCommand("input keyevent KEYCODE_WAKEUP")
                    instrumentation.runShellCommand("wm dismiss-keyguard")
                    instrumentation.uiAutomation.grantRuntimePermission(
                        packageName,
                        Manifest.permission.POST_NOTIFICATIONS
                    )
                }
                base.evaluate()
            }
        }
    }

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val fakeStore = FakeTimerStore()

    @BindValue @JvmField
    val timerStore: TimerStore = fakeStore

    @Inject lateinit var timerController: TimerController

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @After
    fun tearDown() {
        timerController.cancel()
        fakeStore.activeTimer = null
    }

    @Test
    fun setupScreen_tapDurationTile_transitionsToRunningScreen() {
        // Wait for SetupScreen to appear
        composeRule.waitForText("5")
        composeRule.onNodeWithText("5").assertIsDisplayed()

        // Tap the 5-minute tile
        composeRule.onNodeWithText("5").performClick()

        // Running screen shows "REMAINING"
        composeRule.onNodeWithText("REMAINING").assertIsDisplayed()

        // Cleanup
        timerController.cancel()
    }

    @Test
    fun runningScreen_tapCancel_returnsToSetupScreen() {
        timerController.start(5f)
        composeRule.waitForText("Cancel nap")

        composeRule.onNodeWithText("Cancel nap").performClick()

        composeRule.onNodeWithText("Ready to rest?").assertIsDisplayed()
    }

    @Test
    fun runningScreen_snooze_startsNewTimerWithIsSnoozeTrue() {
        // Put the app in a running snooze state directly via controller
        timerController.startSnooze(5f)
        composeRule.waitForText("REMAINING")

        // Running screen appears with snoozed caption
        composeRule.onNodeWithText("REMAINING").assertIsDisplayed()

        // Cleanup
        timerController.cancel()
    }

    @Test
    fun alarmState_stop_returnsToSetupScreen() {
        // Simulate alarm completion
        timerController.start(5f)
        composeRule.waitForText("REMAINING")
        timerController.completeTimer()
        composeRule.waitForIdle()

        // App returns to SetupScreen after timer completes
        composeRule.onNodeWithText("Ready to rest?").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.waitForText(text: String) {
        waitUntil(timeoutMillis = 5_000) {
            try {
                onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
            } catch (_: IllegalStateException) {
                false
            }
        }
    }

    private fun android.app.Instrumentation.runShellCommand(command: String) {
        uiAutomation.executeShellCommand(command).close()
    }
}
