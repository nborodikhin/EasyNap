package me.easynap.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import me.easynap.R
import me.easynap.TimerController
import me.easynap.TimerState
import me.easynap.anticipatedProgressMs
import me.easynap.formatDurationCaption
import me.easynap.formatRemainingTimeRoundUp

@Composable
fun RunningScreen(state: TimerState.Running) {
    val totalMs = (state.durationMinutes * 60_000).toLong().coerceAtLeast(1L)
    val syncInterval = 1_000L
    var remainingMs by remember { mutableLongStateOf(state.endAtMillis - System.currentTimeMillis()) }

    LaunchedEffect(state.endAtMillis) {
        while (true) {
            delay(syncInterval)
            val actual = state.endAtMillis - System.currentTimeMillis()
            remainingMs = actual
            if (actual <= 0) break
        }
    }

    val anticipatedMs = anticipatedProgressMs(remainingMs, syncInterval)
    val rawFraction = (anticipatedMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    val fraction by animateFloatAsState(
        targetValue = rawFraction,
        animationSpec = tween(durationMillis = syncInterval.toInt(), easing = LinearEasing),
        label = "timerProgress"
    )

    val displayMs = remainingMs.coerceIn(0L, totalMs)
    val originalDurationMinutes by TimerController.napDurationMinutes.collectAsStateWithLifecycle()
    val captionBase = formatDurationCaption(if (state.isSnooze) originalDurationMinutes else state.durationMinutes)
    val caption = if (state.isSnooze) stringResource(R.string.countdown_caption_snoozed, captionBase) else captionBase

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.size(236.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant,
                        strokeWidth = 10.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatRemainingTimeRoundUp(displayMs),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 66.sp,
                                fontFeatureSettings = "tnum",
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.countdown_remaining),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { TimerController.cancel() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(60.dp),
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(stringResource(R.string.countdown_cancel_nap), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
