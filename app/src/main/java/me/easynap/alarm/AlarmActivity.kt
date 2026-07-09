package me.easynap.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.easynap.MainActivity
import me.easynap.R
import me.easynap.theme.CalmTealAlarmBackground
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.SNOOZE_OPTIONS
import me.easynap.timer.TimerController
import me.easynap.timer.durationDisplayMinutesOrNull

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    companion object {
        const val ACTION_FINISH = "me.easynap.ACTION_ALARM_FINISH"
    }

    @Inject lateinit var timerController: TimerController

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }
    private var receiverRegistered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AlarmService.isRunning) {
            finish()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        ContextCompat.registerReceiver(
            this,
            finishReceiver,
            IntentFilter(ACTION_FINISH),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiverRegistered = true

        enableEdgeToEdge()

        setContent {
            EasyNapTheme {
                BackHandler { stopAlarmService() }

                val durationSeconds by timerController.napDurationSeconds.collectAsStateWithLifecycle()
                val wholeMinutes = durationDisplayMinutesOrNull(durationSeconds)
                val body = if (wholeMinutes != null) {
                    pluralStringResource(
                        R.plurals.alarm_body_minutes,
                        wholeMinutes,
                        wholeMinutes
                    )
                } else {
                    val s = durationSeconds.coerceAtLeast(0)
                    pluralStringResource(R.plurals.alarm_body_seconds, s, s)
                }
                Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .consumeWindowInsets(padding)
                            .padding(horizontal = 20.dp)
                    ) {
                        AlarmContent(
                            headline = stringResource(R.string.alarm_headline),
                            body = body,
                            stopText = stringResource(R.string.alarm_stop),
                            snoozeLabel = stringResource(R.string.alarm_snooze_label),
                            compactLandscape = maxWidth > maxHeight && maxHeight < 520.dp,
                            onStop = { stopAlarmService() },
                            onSnooze = { snooze(it) }
                        )
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (val seconds = snoozeDurationForVolumeKey(keyCode)) {
        null -> when (keyCode) {
            KeyEvent.KEYCODE_ESCAPE -> { stopAlarmService(); true }
            else -> super.onKeyDown(keyCode, event)
        }
        else -> { snooze(seconds); true }
    }

    override fun onDestroy() {
        if (receiverRegistered) unregisterReceiver(finishReceiver)
        super.onDestroy()
    }

    private fun stopAlarmService() {
        startService(Intent(this, AlarmService::class.java).apply { action = AlarmService.ACTION_STOP })
        returnToMain()
    }

    private fun snooze(seconds: Int) {
        startService(Intent(this, AlarmService::class.java).apply { action = AlarmService.ACTION_STOP })
        timerController.startSnooze(seconds)
        returnToMain()
    }

    private fun returnToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }
}

internal fun snoozeDurationForVolumeKey(keyCode: Int): Int? = when (keyCode) {
    KeyEvent.KEYCODE_VOLUME_DOWN -> SNOOZE_OPTIONS[0]
    KeyEvent.KEYCODE_VOLUME_UP -> SNOOZE_OPTIONS[1]
    else -> null
}

@Composable
private fun snoozeShortcutLabel(index: Int): String? = when (index) {
    0 -> stringResource(R.string.alarm_snooze_shortcut_volume_down)
    1 -> stringResource(R.string.alarm_snooze_shortcut_volume_up)
    else -> null
}

@Composable
private fun snoozeButtonDescription(durationDescription: String, shortcutLabel: String?): String =
    if (shortcutLabel == null) {
        durationDescription
    } else {
        stringResource(R.string.snooze_button_desc_with_shortcut, durationDescription, shortcutLabel)
    }

@Composable
private fun AlarmContent(
    headline: String,
    body: String,
    stopText: String,
    snoozeLabel: String,
    compactLandscape: Boolean,
    onStop: () -> Unit,
    onSnooze: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (compactLandscape) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AlarmMessage(
                headline = headline,
                body = body,
                iconSize = 96.dp,
                modifier = Modifier
                    .weight(0.95f)
                    .widthIn(max = 520.dp)
            )
            AlarmActions(
                stopText = stopText,
                snoozeLabel = snoozeLabel,
                stopHeight = 58.dp,
                snoozeHeight = 72.dp,
                onStop = onStop,
                onSnooze = onSnooze,
                modifier = Modifier
                    .weight(1.05f)
                    .widthIn(max = 620.dp)
            )
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AlarmMessage(headline = headline, body = body, iconSize = 104.dp)
            Spacer(Modifier.heightIn(min = 40.dp))
            AlarmActions(
                stopText = stopText,
                snoozeLabel = snoozeLabel,
                stopHeight = 68.dp,
                snoozeHeight = 72.dp,
                onStop = onStop,
                onSnooze = onSnooze
            )
        }
    }
}

@Composable
private fun AlarmMessage(
    headline: String,
    body: String,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AlarmCheckIcon(size = iconSize)
        Spacer(Modifier.heightIn(min = 24.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.heightIn(min = 12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Ltr),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun AlarmCheckIcon(size: Dp) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f
            val ringWidth = 9.dp.toPx()
            drawCircle(color = primary, radius = radius)
            drawCircle(color = CalmTealAlarmBackground, radius = radius - ringWidth)
        }
        Text(
            text = "✓",
            fontSize = 42.sp,
            fontWeight = FontWeight.Light,
            color = onPrimaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.clearAndSetSemantics {}
        )
    }
}

@Composable
internal fun AlarmActions(
    stopText: String,
    snoozeLabel: String,
    stopHeight: Dp,
    snoozeHeight: Dp,
    onStop: () -> Unit,
    onSnooze: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onStop,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .heightIn(min = stopHeight),
            shape = CircleShape
        ) {
            Text(stopText, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.heightIn(min = 16.dp))
        Text(
            text = snoozeLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.32.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Spacer(Modifier.heightIn(min = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SNOOZE_OPTIONS.forEachIndexed { index, seconds ->
                val wholeMinutes = durationDisplayMinutesOrNull(seconds)
                val value = wholeMinutes?.toString() ?: seconds.toString()
                val unit = if (wholeMinutes != null) {
                    stringResource(R.string.duration_unit_min)
                } else {
                    stringResource(R.string.duration_unit_sec)
                }
                val durationDesc = if (wholeMinutes != null) {
                    pluralStringResource(R.plurals.snooze_button_desc_minutes, wholeMinutes, wholeMinutes)
                } else {
                    pluralStringResource(R.plurals.snooze_button_desc_seconds, seconds, seconds)
                }
                val shortcutLabel = snoozeShortcutLabel(index)
                val buttonDesc = snoozeButtonDescription(durationDesc, shortcutLabel)
                Column(
                    modifier = Modifier
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalButton(
                        onClick = { onSnooze(seconds) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = snoozeHeight)
                            .semantics { contentDescription = buttonDesc },
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                value,
                                style = androidx.compose.ui.text.TextStyle(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Normal,
                                    fontFeatureSettings = "tnum",
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                unit,
                                style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    if (shortcutLabel != null) {
                        Spacer(Modifier.heightIn(min = 8.dp))
                        Text(
                            shortcutLabel,
                            style = MaterialTheme.typography.labelSmall.copy(textDirection = TextDirection.Ltr),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Alarm - compact landscape", device = "spec:width=960dp,height=430dp,orientation=landscape")
@Composable
private fun AlarmCompactLandscapePreview() {
    EasyNapTheme {
        Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
            AlarmContent(
                headline = "Time to wake up",
                body = "Your 5-minute nap is done. Hope you feel refreshed.",
                stopText = "Stop",
                snoozeLabel = "SNOOZE",
                compactLandscape = true,
                onStop = {},
                onSnooze = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Alarm - portrait")
@Composable
private fun AlarmPortraitPreview() {
    EasyNapTheme {
        Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
            AlarmContent(
                headline = "Time to wake up",
                body = "Your 20-minute nap is done. Hope you feel refreshed.",
                stopText = "Stop",
                snoozeLabel = "SNOOZE",
                compactLandscape = false,
                onStop = {},
                onSnooze = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Alarm - 200% font scale", fontScale = 2f)
@Composable
private fun AlarmFontScalePreview() {
    EasyNapTheme {
        Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
            AlarmContent(
                headline = "Time to wake up",
                body = "Your 20-minute nap is done. Hope you feel refreshed.",
                stopText = "Stop",
                snoozeLabel = "SNOOZE",
                compactLandscape = false,
                onStop = {},
                onSnooze = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Alarm - RTL", locale = "ar")
@Composable
private fun AlarmRtlPreview() {
    EasyNapTheme {
        Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
            AlarmContent(
                headline = "Time to wake up",
                body = "Your 20-minute nap is done. Hope you feel refreshed.",
                stopText = "Stop",
                snoozeLabel = "SNOOZE",
                compactLandscape = false,
                onStop = {},
                onSnooze = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}
