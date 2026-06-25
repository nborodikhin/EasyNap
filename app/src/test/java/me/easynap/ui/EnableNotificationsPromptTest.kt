package me.easynap.ui

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
    fun prompt_is_visible_when_alarm_notifications_are_unavailable() {
        composeRule.setContent {
            EasyNapTheme {
                EnableNotificationsPrompt(
                    alarmNotificationsAvailable = false,
                    onClick = {}
                )
            }
        }

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
}
