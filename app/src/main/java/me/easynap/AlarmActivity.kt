package me.easynap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.easynap.theme.CalmTealAlarmBackground
import me.easynap.theme.EasyNapTheme
import me.easynap.formatDurationLabel

class AlarmActivity : ComponentActivity() {

    companion object {
        const val ACTION_FINISH = "me.easynap.ACTION_ALARM_FINISH"
    }

    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        registerReceiver(finishReceiver, IntentFilter(ACTION_FINISH), RECEIVER_NOT_EXPORTED)

        enableEdgeToEdge()

        setContent {
            EasyNapTheme {
                BackHandler { stopAlarmService() }

                val durationMinutes by TimerController.napDurationMinutes.collectAsStateWithLifecycle()
                val durationLabel = formatDurationLabel(durationMinutes)
                val primary = MaterialTheme.colorScheme.primary
                val onPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer

                Scaffold(containerColor = CalmTealAlarmBackground) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(104.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val radius = size.minDimension / 2f
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
                        Spacer(Modifier.height(32.dp))
                        Text(
                            text = "Time to wake up",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Your $durationLabel-minute nap is done. Hope you feel refreshed.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(Modifier.height(40.dp))
                        Button(
                            onClick = { stopAlarmService() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(68.dp),
                            shape = CircleShape
                        ) {
                            Text("Stop", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = "SNOOZE",
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
                                    onClick = { snooze(minutes) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(18.dp),
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(finishReceiver)
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
        TimerController.startSnooze(minutes)
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        finish()
    }
}
