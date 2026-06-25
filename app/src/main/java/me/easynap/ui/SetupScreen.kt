package me.easynap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.easynap.R
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.TimerController
import me.easynap.timer.appendToBuffer
import me.easynap.timer.formatDurationLabel
import me.easynap.timer.formatDurationUnit
import me.easynap.timer.formatNapDescription
import me.easynap.timer.isCustomDurationInRange
import me.easynap.timer.parseCustomDurationSeconds
import me.easynap.data.TimerPreferenceStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    timerController: TimerController,
    alarmNotificationsAvailable: Boolean = true,
    onEnableNotificationsClick: () -> Unit = {}
) {
    var showCustomSheet by rememberSaveable { mutableStateOf(false) }
    val history by timerController.history.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(18.dp))
                Text(
                    stringResource(R.string.home_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    stringResource(R.string.home_headline),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(42.dp))
                DurationGrid(
                    durations = history,
                    onDurationSelected = { timerController.start(it) },
                    onCustom = { showCustomSheet = true }
                )
                Spacer(Modifier.height(28.dp))
            }

            EnableNotificationsPrompt(
                alarmNotificationsAvailable = alarmNotificationsAvailable,
                onClick = onEnableNotificationsClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            )
        }
    }

    if (showCustomSheet) {
        CustomDurationSheet(
            onDismiss = { showCustomSheet = false },
            onStart = { seconds ->
                showCustomSheet = false
                timerController.start(seconds / 60f)
            }
        )
    }
}

@Composable
internal fun EnableNotificationsPrompt(
    alarmNotificationsAvailable: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!alarmNotificationsAvailable) {
        Text(
            text = stringResource(R.string.enable_notifications_prompt),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = modifier.clickable(onClick = onClick)
        )
    }
}

@Composable
internal fun DurationGrid(
    durations: List<Float>,
    onDurationSelected: (Float) -> Unit,
    onCustom: () -> Unit
) {
    val rows = (durations.map { it } + listOf(Float.NEGATIVE_INFINITY)).chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item ->
                    if (item == Float.NEGATIVE_INFINITY) {
                        CustomTile(onClick = onCustom, modifier = Modifier.weight(1f))
                    } else {
                        DurationTile(
                            minutes = item,
                            onClick = { onDurationSelected(item) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
internal fun DurationTile(minutes: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isWhole = minutes % 1f == 0f
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatDurationLabel(minutes),
                style = TextStyle(
                    fontSize = if (isWhole) 28.sp else 22.sp,
                    fontWeight = FontWeight.Normal,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = formatDurationUnit(minutes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun CustomTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Custom",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomDurationSheet(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputBuffer by rememberSaveable { mutableStateOf("") }

    val parsedSeconds = parseCustomDurationSeconds(inputBuffer)
    val isOutOfRange = parsedSeconds != null && !isCustomDurationInRange(parsedSeconds)
    val isStartEnabled = parsedSeconds != null && isCustomDurationInRange(parsedSeconds)

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            CustomDurationSheetContent(
                inputBuffer = inputBuffer,
                cursorAlpha = cursorAlpha,
                parsedSeconds = parsedSeconds,
                isOutOfRange = isOutOfRange,
                isStartEnabled = isStartEnabled,
                title = stringResource(R.string.custom_duration_title),
                errorText = stringResource(R.string.custom_duration_error),
                startText = stringResource(R.string.custom_duration_start),
                compactLandscape = maxWidth > maxHeight && maxHeight < 600.dp,
                onKey = { key -> inputBuffer = appendToBuffer(inputBuffer, key) },
                onStart = onStart
            )
        }
    }
}

@Composable
internal fun ValueDisplay(
    inputBuffer: String,
    cursorAlpha: Float,
    fontSize: TextUnit = 52.sp
) {
    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = primary,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
            .padding(bottom = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = inputBuffer,
                style = TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Light,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "|",
                style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha)
            )
        }
    }
}

@Composable
internal fun NumericKeypad(
    onKey: (String) -> Unit,
    colonEnabled: Boolean,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    spacing: Dp = 8.dp,
    keyFontSize: TextUnit = 23.sp
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(":", "0", "⌫")
    )
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { key ->
                    val isSpecial = key == ":" || key == "⌫"
                    val enabled = if (key == ":") colonEnabled else true
                    KeypadButton(
                        label = key,
                        onClick = { onKey(key) },
                        enabled = enabled,
                        isSpecial = isSpecial,
                        height = keyHeight,
                        fontSize = keyFontSize,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isSpecial: Boolean,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    fontSize: TextUnit = 23.sp
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(height),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = fontSize,
                color = if (isSpecial) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CustomDurationSheetContent(
    inputBuffer: String,
    cursorAlpha: Float,
    parsedSeconds: Int?,
    isOutOfRange: Boolean,
    isStartEnabled: Boolean,
    title: String,
    errorText: String,
    startText: String,
    compactLandscape: Boolean,
    onKey: (String) -> Unit,
    onStart: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val helperColor = if (isOutOfRange) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val helperText = when {
        isOutOfRange -> errorText
        isStartEnabled && parsedSeconds != null -> formatNapDescription(parsedSeconds)
        else -> ""
    }

    if (compactLandscape) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                ValueDisplay(inputBuffer = inputBuffer, cursorAlpha = cursorAlpha, fontSize = 44.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = helperColor
                )
                Spacer(Modifier.height(16.dp))
                StartDurationButton(
                    text = startText,
                    enabled = isStartEnabled,
                    parsedSeconds = parsedSeconds,
                    onStart = onStart
                )
            }
            NumericKeypad(
                onKey = onKey,
                colonEnabled = inputBuffer.isNotEmpty() && ':' !in inputBuffer,
                modifier = Modifier.weight(1.25f),
                keyHeight = 48.dp,
                keyFontSize = 21.sp
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(20.dp))
            ValueDisplay(inputBuffer = inputBuffer, cursorAlpha = cursorAlpha)
            Spacer(Modifier.height(6.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = helperColor
            )
            Spacer(Modifier.height(20.dp))
            NumericKeypad(
                onKey = onKey,
                colonEnabled = inputBuffer.isNotEmpty() && ':' !in inputBuffer
            )
            Spacer(Modifier.height(20.dp))
            StartDurationButton(
                text = startText,
                enabled = isStartEnabled,
                parsedSeconds = parsedSeconds,
                onStart = onStart
            )
        }
    }
}

@Composable
private fun StartDurationButton(
    text: String,
    enabled: Boolean,
    parsedSeconds: Int?,
    onStart: (Int) -> Unit
) {
    Button(
        onClick = { if (parsedSeconds != null) onStart(parsedSeconds) },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = CircleShape
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "SetupScreen")
@Composable
private fun SetupScreenPreview() {
    EasyNapTheme {
        SetupScreenStateless(
            history = TimerPreferenceStore.DEFAULT_HISTORY,
            onDurationSelected = {},
            onCustom = {}
        )
    }
}

@Preview(showBackground = true, name = "DurationGrid - seed durations")
@Composable
private fun DurationGridPreview() {
    EasyNapTheme {
        DurationGrid(
            durations = TimerPreferenceStore.DEFAULT_HISTORY,
            onDurationSelected = {},
            onCustom = {}
        )
    }
}

@Preview(showBackground = true, name = "DurationTile - whole minute")
@Composable
private fun DurationTileWholePreview() {
    EasyNapTheme { DurationTile(minutes = 10f, onClick = {}) }
}

@Preview(showBackground = true, name = "DurationTile - fractional")
@Composable
private fun DurationTileFractionalPreview() {
    EasyNapTheme { DurationTile(minutes = 1.5f, onClick = {}) }
}

@Preview(showBackground = true, name = "CustomTile")
@Composable
private fun CustomTilePreview() {
    EasyNapTheme { CustomTile(onClick = {}) }
}

@Preview(showBackground = true, name = "CustomDurationSheet - empty buffer")
@Composable
private fun CustomDurationSheetEmptyPreview() {
    EasyNapTheme {
        CustomDurationSheetStateless(inputBuffer = "", cursorAlpha = 1f, onKey = {}, onStart = {})
    }
}

@Preview(showBackground = true, name = "CustomDurationSheet - filled buffer")
@Composable
private fun CustomDurationSheetFilledPreview() {
    EasyNapTheme {
        CustomDurationSheetStateless(inputBuffer = "12:30", cursorAlpha = 0f, onKey = {}, onStart = {})
    }
}

@Preview(
    showBackground = true,
    name = "CustomDurationSheet - compact landscape",
    widthDp = 960,
    heightDp = 420
)
@Composable
private fun CustomDurationSheetCompactLandscapePreview() {
    EasyNapTheme {
        CustomDurationSheetStateless(
            inputBuffer = "12:30",
            cursorAlpha = 0f,
            onKey = {},
            onStart = {},
            compactLandscape = true
        )
    }
}

@Preview(showBackground = true, name = "ValueDisplay - empty")
@Composable
private fun ValueDisplayEmptyPreview() {
    EasyNapTheme { ValueDisplay(inputBuffer = "", cursorAlpha = 1f) }
}

@Preview(showBackground = true, name = "ValueDisplay - filled")
@Composable
private fun ValueDisplayFilledPreview() {
    EasyNapTheme { ValueDisplay(inputBuffer = "12:30", cursorAlpha = 0f) }
}

@Preview(showBackground = true, name = "NumericKeypad - colon enabled")
@Composable
private fun NumericKeypadColonEnabledPreview() {
    EasyNapTheme { NumericKeypad(onKey = {}, colonEnabled = true) }
}

@Preview(showBackground = true, name = "NumericKeypad - colon disabled")
@Composable
private fun NumericKeypadColonDisabledPreview() {
    EasyNapTheme { NumericKeypad(onKey = {}, colonEnabled = false) }
}

@Preview(showBackground = true, name = "KeypadButton - normal")
@Composable
private fun KeypadButtonNormalPreview() {
    EasyNapTheme { KeypadButton(label = "5", onClick = {}, enabled = true, isSpecial = false) }
}

@Preview(showBackground = true, name = "KeypadButton - special")
@Composable
private fun KeypadButtonSpecialPreview() {
    EasyNapTheme { KeypadButton(label = "⌫", onClick = {}, enabled = true, isSpecial = true) }
}

@Preview(showBackground = true, name = "KeypadButton - disabled")
@Composable
private fun KeypadButtonDisabledPreview() {
    EasyNapTheme { KeypadButton(label = ":", onClick = {}, enabled = false, isSpecial = true) }
}

// Stateless helpers for previews that need to avoid real controller state

@Composable
private fun SetupScreenStateless(
    history: List<Float>,
    onDurationSelected: (Float) -> Unit,
    onCustom: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(18.dp))
            Text("EasyNap", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(28.dp))
            Text("Take a nap", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(8.dp))
            Text("Choose your duration", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(42.dp))
            DurationGrid(durations = history, onDurationSelected = onDurationSelected, onCustom = onCustom)
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun CustomDurationSheetStateless(
    inputBuffer: String,
    cursorAlpha: Float,
    onKey: (String) -> Unit,
    onStart: (Int) -> Unit,
    compactLandscape: Boolean = false
) {
    val parsedSeconds = parseCustomDurationSeconds(inputBuffer)
    val isOutOfRange = parsedSeconds != null && !isCustomDurationInRange(parsedSeconds)
    val isStartEnabled = parsedSeconds != null && isCustomDurationInRange(parsedSeconds)
    CustomDurationSheetContent(
        inputBuffer = inputBuffer,
        cursorAlpha = cursorAlpha,
        parsedSeconds = parsedSeconds,
        isOutOfRange = isOutOfRange,
        isStartEnabled = isStartEnabled,
        title = "Set duration",
        errorText = "Duration out of range",
        startText = "Start nap",
        compactLandscape = compactLandscape,
        onKey = onKey,
        onStart = onStart
    )
}
