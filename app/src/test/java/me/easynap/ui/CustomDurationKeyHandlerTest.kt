package me.easynap.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.isCustomDurationInRange
import me.easynap.timer.parseCustomDurationSeconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class CustomDurationKeyHandlerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun setUpBody(onStart: (Int) -> Unit) {
        composeRule.setContent {
            EasyNapTheme {
                var buf by remember { mutableStateOf("") }
                val parsed = parseCustomDurationSeconds(buf)
                CustomDurationSheetBody(
                    inputBuffer = buf,
                    parsedSeconds = parsed,
                    isOutOfRange = parsed != null && !isCustomDurationInRange(parsed),
                    isStartEnabled = parsed != null && isCustomDurationInRange(parsed),
                    cursorAlpha = 1f,
                    onBufferChange = { buf = it },
                    onStart = onStart
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun `typing 0 semicolon 34 backspace 5 Enter starts nap at 35s`() {
        var startedSeconds: Int? = null
        setUpBody { startedSeconds = it }

        val keys = listOf(
            Key.Zero,
            Key.Semicolon,
            Key.Three,
            Key.Four,
            Key.Backspace,
            Key.Five,
            Key.Enter
        )

        for (key in keys) {
            composeRule.onRoot().performKeyInput { pressKey(key) }
            composeRule.waitForIdle()
        }

        assertEquals(35, startedSeconds)
    }

    @Test
    fun `typing non-zero minutes semicolon ignores colon and starts whole minutes`() {
        var startedSeconds: Int? = null
        setUpBody { startedSeconds = it }

        val keys = listOf(
            Key.One,
            Key.Semicolon,
            Key.Enter
        )

        for (key in keys) {
            composeRule.onRoot().performKeyInput { pressKey(key) }
            composeRule.waitForIdle()
        }

        assertEquals(60, startedSeconds)
    }

    @Test
    fun `Enter with empty buffer does not start`() {
        var startedSeconds: Int? = null
        setUpBody { startedSeconds = it }

        composeRule.onRoot().performKeyInput { pressKey(Key.Enter) }
        composeRule.waitForIdle()

        assertNull(startedSeconds)
    }
}
