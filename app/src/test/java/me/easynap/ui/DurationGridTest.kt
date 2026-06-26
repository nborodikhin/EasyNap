package me.easynap.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
                    durations = listOf(10f, 20f, 30f, 40f, 50f, 60f),
                    onDurationSelected = {},
                    onCustom = {}
                )
            }
        }

        listOf("10", "20", "30", "40", "50").forEach { label ->
            composeRule.onNodeWithText(label, substring = true).assertIsDisplayed()
        }
        composeRule.onNodeWithText("+", substring = true).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("60", substring = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `custom tile is present when history is empty`() {
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(durations = emptyList(), onDurationSelected = {}, onCustom = {})
            }
        }

        composeRule.onNodeWithText("+", substring = true).assertIsDisplayed()
        assertTrue(composeRule.onAllNodesWithText("min", substring = true).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `tapping a duration tile invokes callback with correct value`() {
        var selected = -1f
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(15f),
                    onDurationSelected = { selected = it },
                    onCustom = {}
                )
            }
        }

        composeRule.onNodeWithText("15", substring = true).performClick()

        assertEquals(15f, selected)
    }

    @Test
    fun `tapping custom tile invokes custom callback`() {
        var customClicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f),
                    onDurationSelected = {},
                    onCustom = { customClicked = true }
                )
            }
        }

        composeRule.onNodeWithText("+", substring = true).performClick()

        assertTrue(customClicked)
    }
}
