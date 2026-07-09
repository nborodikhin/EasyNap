package me.easynap.alarm

import android.view.KeyEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.SNOOZE_OPTIONS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AlarmActivityTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `volume keys map to first and second snooze options`() {
        assertEquals(SNOOZE_OPTIONS[0], snoozeDurationForVolumeKey(KeyEvent.KEYCODE_VOLUME_DOWN))
        assertEquals(SNOOZE_OPTIONS[1], snoozeDurationForVolumeKey(KeyEvent.KEYCODE_VOLUME_UP))
        assertNull(snoozeDurationForVolumeKey(KeyEvent.KEYCODE_ESCAPE))
    }

    @Test
    fun `alarm snooze buttons show shortcut labels on first two options only`() {
        composeRule.setContent {
            EasyNapTheme {
                AlarmActions(
                    stopText = "Stop",
                    snoozeLabel = "SNOOZE",
                    stopHeight = 68.dp,
                    snoozeHeight = 68.dp,
                    onStop = {},
                    onSnooze = {}
                )
            }
        }

        composeRule.onNodeWithText("Vol-", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("Vol+", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
        assertEquals(3, composeRule.onAllNodesWithText("min", useUnmergedTree = true).fetchSemanticsNodes().size)
        assertTrue(composeRule.onAllNodesWithText("+1 min", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
        assertTrue(composeRule.onAllNodesWithText("Vol", useUnmergedTree = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `alarm snooze button descriptions include shortcut when present`() {
        composeRule.setContent {
            EasyNapTheme {
                AlarmActions(
                    stopText = "Stop",
                    snoozeLabel = "SNOOZE",
                    stopHeight = 68.dp,
                    snoozeHeight = 68.dp,
                    onStop = {},
                    onSnooze = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Snooze 1 minute, Vol-").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Snooze 5 minutes, Vol+").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Snooze 10 minutes").assertIsDisplayed()
    }
}
