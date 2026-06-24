package me.easynap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import me.easynap.R
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.TimerController
import me.easynap.timer.TimerState
import me.easynap.timer.anticipatedProgressMs
import me.easynap.timer.formatDurationCaption
import me.easynap.timer.formatRemainingTimeRoundUp

@Composable
fun RunningScreen(state: TimerState.Running, timerController: TimerController) {
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
    val originalDurationMinutes by timerController.napDurationMinutes.collectAsStateWithLifecycle()
    val captionBase = formatDurationCaption(if (state.isSnooze) originalDurationMinutes else state.durationMinutes)
    val caption = if (state.isSnooze) stringResource(R.string.countdown_caption_snoozed, captionBase) else captionBase

    Scaffold { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            RunningScreenContent(
                caption = caption,
                remainingText = formatRemainingTimeRoundUp(displayMs),
                remainingLabel = stringResource(R.string.countdown_remaining),
                cancelText = stringResource(R.string.countdown_cancel_nap),
                fraction = fraction,
                compactLandscape = maxWidth > maxHeight && maxHeight < 520.dp,
                onCancel = { timerController.cancel() }
            )
        }
    }
}

@Composable
private fun RunningScreenContent(
    caption: String,
    remainingText: String,
    remainingLabel: String,
    cancelText: String,
    fraction: Float,
    compactLandscape: Boolean,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (compactLandscape) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountdownCluster(
                caption = caption,
                remainingText = remainingText,
                remainingLabel = remainingLabel,
                fraction = fraction,
                ringSize = 218.dp,
                timeFontSize = 56.sp,
                modifier = Modifier.weight(1f)
            )
            CancelNapButton(
                text = cancelText,
                onCancel = onCancel,
                modifier = Modifier
                    .weight(0.95f)
                    .widthIn(max = 560.dp)
            )
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            CountdownRing(
                remainingText = remainingText,
                remainingLabel = remainingLabel,
                fraction = fraction,
                ringSize = 236.dp,
                timeFontSize = 66.sp,
                modifier = Modifier.align(Alignment.Center)
            )

            CancelNapButton(
                text = cancelText,
                onCancel = onCancel,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun CountdownCluster(
    caption: String,
    remainingText: String,
    remainingLabel: String,
    fraction: Float,
    ringSize: Dp,
    timeFontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CountdownRing(
            remainingText = remainingText,
            remainingLabel = remainingLabel,
            fraction = fraction,
            ringSize = ringSize,
            timeFontSize = timeFontSize
        )
    }
}

@Composable
private fun CountdownRing(
    remainingText: String,
    remainingLabel: String,
    fraction: Float,
    ringSize: Dp,
    timeFontSize: TextUnit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { fraction },
            modifier = Modifier.size(ringSize),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            strokeWidth = 10.dp,
            strokeCap = StrokeCap.Round,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = remainingText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = timeFontSize,
                    fontFeatureSettings = "tnum",
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = remainingLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CancelNapButton(
    text: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onCancel,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true, name = "Running - normal")
@Composable
private fun RunningScreenPreview() {
    EasyNapTheme {
        // Preview cannot provide a real TimerController; use a stub state directly
        val endAt = System.currentTimeMillis() + 15 * 60_000L
        RunningScreenStateless(
            state = TimerState.Running(endAt, 15f, isSnooze = false),
            originalDurationMinutes = 15f,
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Running - snooze")
@Composable
private fun RunningScreenSnoozePreview() {
    EasyNapTheme {
        val endAt = System.currentTimeMillis() + 5 * 60_000L
        RunningScreenStateless(
            state = TimerState.Running(endAt, 5f, isSnooze = true),
            originalDurationMinutes = 30f,
            onCancel = {}
        )
    }
}

@Preview(showBackground = true, name = "Running - compact landscape", widthDp = 960, heightDp = 430)
@Composable
private fun RunningScreenCompactLandscapePreview() {
    EasyNapTheme {
        val endAt = System.currentTimeMillis() + 5 * 60_000L
        RunningScreenStateless(
            state = TimerState.Running(endAt, 5f, isSnooze = false),
            originalDurationMinutes = 5f,
            onCancel = {},
            compactLandscape = true
        )
    }
}

@Composable
private fun RunningScreenStateless(
    state: TimerState.Running,
    originalDurationMinutes: Float,
    onCancel: () -> Unit,
    compactLandscape: Boolean = false
) {
    val totalMs = (state.durationMinutes * 60_000).toLong().coerceAtLeast(1L)
    val remainingMs = (state.endAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
    val fraction = (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
    val displayMs = remainingMs.coerceIn(0L, totalMs)
    val captionBase = formatDurationCaption(if (state.isSnooze) originalDurationMinutes else state.durationMinutes)
    val caption = if (state.isSnooze) "Snoozed — $captionBase" else captionBase

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            RunningScreenContent(
                caption = caption,
                remainingText = formatRemainingTimeRoundUp(displayMs),
                remainingLabel = "remaining",
                cancelText = "Cancel nap",
                fraction = fraction,
                compactLandscape = compactLandscape,
                onCancel = onCancel
            )
        }
    }
}
