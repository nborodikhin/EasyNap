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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import me.easynap.timer.TimerController
import me.easynap.timer.formatDurationLabel
import me.easynap.timer.SNOOZE_OPTIONS

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

                val durationMinutes by timerController.napDurationMinutes.collectAsStateWithLifecycle()
                val durationLabel = formatDurationLabel(durationMinutes)
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
                            body = stringResource(R.string.alarm_body, durationLabel),
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> { snooze(1f); true }
        KeyEvent.KEYCODE_ESCAPE -> { stopAlarmService(); true }
        else -> super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        if (receiverRegistered) unregisterReceiver(finishReceiver)
        super.onDestroy()
    }

    private fun stopAlarmService() {
        startService(Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        })
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }

    private fun snooze(minutes: Float) {
        startService(Intent(this, AlarmService::class.java).apply {
            action = AlarmService.ACTION_STOP
        })
        timerController.startSnooze(minutes)
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }
}

@Composable
private fun AlarmContent(
    headline: String,
    body: String,
    stopText: String,
    snoozeLabel: String,
    compactLandscape: Boolean,
    onStop: () -> Unit,
    onSnooze: (Float) -> Unit,
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
                snoozeHeight = 52.dp,
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
            Spacer(Modifier.height(40.dp))
            AlarmActions(
                stopText = stopText,
                snoozeLabel = snoozeLabel,
                stopHeight = 68.dp,
                snoozeHeight = 68.dp,
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
        Spacer(Modifier.height(24.dp))
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
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
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AlarmActions(
    stopText: String,
    snoozeLabel: String,
    stopHeight: Dp,
    snoozeHeight: Dp,
    onStop: () -> Unit,
    onSnooze: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onStop,
            modifier = Modifier
                .fillMaxWidth()
                .height(stopHeight),
            shape = CircleShape
        ) {
            Text(stopText, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = snoozeLabel,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.32.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SNOOZE_OPTIONS.forEach { (label, minutes) ->
                FilledTonalButton(
                    onClick = { onSnooze(minutes) },
                    modifier = Modifier
                        .weight(1f)
                        .height(snoozeHeight),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Alarm - compact landscape", widthDp = 960, heightDp = 430)
@Composable
private fun AlarmCompactLandscapePreview() {
    EasyNapTheme {
        Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
            AlarmContent(
                headline = "Time to wake up",
                body = "Your 0:05-minute nap is done. Hope you feel refreshed.",
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
