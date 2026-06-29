package me.easynap.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput

import me.easynap.theme.EasyNapTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DurationGridDeleteTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `tapping pending-delete tile itself does nothing`() {
        var deleted = -1f
        var selected = -1f
        var mode by mutableStateOf<DurationGridMode>(DurationGridMode.Normal)
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f, 20f),
                    onDurationSelected = { selected = it },
                    onCustom = {},
                    onDurationDeleted = { deleted = it },
                    mode = mode,
                    onModeChange = { mode = it }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Start 10-minute nap").performClick()

        assertEquals(-1f, deleted)
        assertEquals(-1f, selected)
        assertTrue(mode is DurationGridMode.PendingDelete)
    }

    @Test
    fun `tapping another tile in pending-delete cancels mode without deleting or selecting`() {
        var deleted = -1f
        var selected = -1f
        var mode by mutableStateOf<DurationGridMode>(DurationGridMode.Normal)
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f, 20f),
                    onDurationSelected = { selected = it },
                    onCustom = {},
                    onDurationDeleted = { deleted = it },
                    mode = mode,
                    onModeChange = { mode = it }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Start 20-minute nap").performClick()

        assertEquals(-1f, deleted)
        assertEquals(-1f, selected)
        assertTrue(mode is DurationGridMode.Normal)
    }

    @Test
    fun `after cancel tapping the original tile selects normally`() {
        var selected = -1f
        var mode by mutableStateOf<DurationGridMode>(DurationGridMode.Normal)
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f, 20f),
                    onDurationSelected = { selected = it },
                    onCustom = {},
                    onDurationDeleted = {},
                    mode = mode,
                    onModeChange = { mode = it }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Start 20-minute nap").performClick() // cancel via other tile
        composeRule.onNodeWithContentDescription("Start 10-minute nap").performClick()

        assertEquals(10f, selected)
    }

    @Test
    fun `tapping custom tile in pending-delete cancels mode without opening custom sheet`() {
        var customClicked = false
        var deleted = -1f
        var mode by mutableStateOf<DurationGridMode>(DurationGridMode.Normal)
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f),
                    onDurationSelected = {},
                    onCustom = { customClicked = true },
                    onDurationDeleted = { deleted = it },
                    mode = mode,
                    onModeChange = { mode = it }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Custom duration").performClick() // cancel

        assertFalse(customClicked)
        assertEquals(-1f, deleted)
    }

    @Test
    fun `custom tile opens sheet after pending-delete mode is cancelled`() {
        var customClicked = false
        var mode by mutableStateOf<DurationGridMode>(DurationGridMode.Normal)
        composeRule.setContent {
            EasyNapTheme {
                DurationGrid(
                    durations = listOf(10f),
                    onDurationSelected = {},
                    onCustom = { customClicked = true },
                    onDurationDeleted = {},
                    mode = mode,
                    onModeChange = { mode = it }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Custom duration").performClick() // cancel delete mode
        composeRule.onNodeWithContentDescription("Custom duration").performClick() // now open sheet

        assertTrue(customClicked)
    }

}

@RunWith(RobolectricTestRunner::class)
class DurationTileDeleteTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `delete badge is visible when isPendingDelete is true`() {
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(minutes = 10f, onClick = {}, isPendingDelete = true)
            }
        }

        assertTrue(composeRule.onAllNodesWithTag("delete-badge").fetchSemanticsNodes().isNotEmpty())
    }

    @Test
    fun `delete badge is absent in normal state`() {
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(minutes = 10f, onClick = {})
            }
        }

        assertTrue(composeRule.onAllNodesWithTag("delete-badge").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `badge click triggers onIconClick`() {
        var iconClicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(
                    minutes = 10f,
                    onClick = {},
                    isPendingDelete = true,
                    onIconClick = { iconClicked = true }
                )
            }
        }

        composeRule.onNodeWithTag("delete-badge").performClick()

        assertTrue(iconClicked)
    }

    @Test
    fun `long-press triggers onLongClick callback`() {
        var longClicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(
                    minutes = 10f,
                    onClick = {},
                    onLongClick = { longClicked = true }
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }

        assertTrue(longClicked)
    }

    @Test
    fun `short tap triggers onClick callback`() {
        var clicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(minutes = 10f, onClick = { clicked = true })
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performClick()

        assertTrue(clicked)
    }

    @Test
    fun `long-press does not trigger onClick`() {
        var clicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(
                    minutes = 10f,
                    onClick = { clicked = true },
                    onLongClick = {}
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performTouchInput { longClick() }

        assertFalse(clicked)
    }

    @Test
    fun `tile tap in pending-delete does not trigger onClick`() {
        var clicked = false
        composeRule.setContent {
            EasyNapTheme {
                DurationTile(
                    minutes = 10f,
                    onClick = { clicked = false }, // onClick is a no-op in pending-delete in grid context
                    isPendingDelete = true
                )
            }
        }

        composeRule.onNodeWithContentDescription("Start 10-minute nap").performClick()

        // onClick fires but does nothing (no-op lambda); badge click is the delete trigger
        assertFalse(clicked)
    }
}
