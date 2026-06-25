package me.easynap.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.easynap.theme.EasyNapTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class EnableNotificationsPromptTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun prompt_is_visible_immediately_when_alarm_notifications_are_unavailable() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = false,
                    onClick = {}
                )
            }
        }
        // Before LaunchedEffect runs or any animation plays, the prompt must already be visible.
        composeRule.onNodeWithText("Enable notifications").assertIsDisplayed()
    }

    @Test
    fun prompt_is_hidden_when_alarm_notifications_are_available() {
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = true,
                    onClick = {}
                )
            }
        }

        assertTrue(
            composeRule.onAllNodesWithText("Enable notifications")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }

    @Test
    fun prompt_click_invokes_handler() {
        var clicked = false
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = false,
                    onClick = { clicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Enable notifications").performClick()

        assertTrue(clicked)
    }

    @Test
    fun prompt_fades_in_when_alarm_notifications_become_unavailable() {
        var alarmAvailable by mutableStateOf(true)
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = alarmAvailable,
                    onClick = {}
                )
            }
        }
        // Let LaunchedEffect run so subsequent changes animate instead of snapping.
        composeRule.waitForIdle()

        alarmAvailable = false
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Enable notifications").assertIsDisplayed()
    }

    @Test
    fun prompt_fades_out_when_alarm_notifications_become_available() {
        var alarmAvailable by mutableStateOf(false)
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = alarmAvailable,
                    onClick = {}
                )
            }
        }
        composeRule.waitForIdle()

        alarmAvailable = true
        composeRule.waitForIdle()

        assertTrue(
            composeRule.onAllNodesWithText("Enable notifications")
                .fetchSemanticsNodes()
                .isEmpty()
        )
    }
}
