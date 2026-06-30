package me.easynap.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import me.easynap.theme.EasyNapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DurationGridTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `only five duration tiles and custom tile are visible when six are passed`() {
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(600, 1200, 1800, 2400, 3000, 3600),
                    onDurationSelected = {},
                    onCustom = {}
                )
            }
        }

        listOf("10", "20", "30", "40", "50").forEach { label ->
            composeRule.onNodeWithContentDescription("Start $label-minute nap").assertIsDisplayed()
        }
        composeRule.onNodeWithContentDescription("Custom nap").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithContentDescription("Start 60-minute nap").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `custom tile is present when history is empty`() {
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(durations = emptyList(), onDurationSelected = {}, onCustom = {})
            }
        }

        composeRule.onNodeWithContentDescription("Custom nap").assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithContentDescription("Start 1-minute nap").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `tapping a duration tile invokes callback with correct value`() {
        var selected = -1
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(900),
                    onDurationSelected = { selected = it },
                    onCustom = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 15-minute nap").performClick()

        assertEquals(900, selected)
    }

    @Test
    fun `tapping custom tile invokes custom callback`() {
        var customClicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(600),
                    onDurationSelected = {},
                    onCustom = { customClicked = true }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Custom nap").performClick()

        assertTrue(customClicked)
    }
}
