package me.easynap.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.easynap.R
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.TimerController
import me.easynap.timer.appendToBuffer
import me.easynap.timer.durationDisplayMinutesOrNull
import me.easynap.timer.isCustomDurationInRange
import me.easynap.timer.isDigitAllowed
import me.easynap.timer.keypadState
import me.easynap.timer.parseCustomDurationSeconds
import me.easynap.data.TimerPreferenceStore

sealed interface DurationGridMode {
    data object Normal : DurationGridMode
    data class PendingDelete(val seconds: Int) : DurationGridMode
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    timerController: TimerController,
    alarmNotificationsAvailable: Boolean = true,
    onEnableNotificationsClick: () -> Unit = {}
) {
    var showCustomSheet by rememberSaveable { mutableStateOf(false) }
    val history by timerController.history.collectAsStateWithLifecycle()
    var pendingUndoSeconds by rememberSaveable { mutableStateOf<Int?>(null) }
    var gridMode by remember { mutableStateOf<DurationGridMode>(DurationGridMode.Normal) }
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val snackbarUndoText = stringResource(R.string.snackbar_undo)

    LaunchedEffect(pendingUndoSeconds) {
        val seconds = pendingUndoSeconds ?: return@LaunchedEffect
        val wholeMinutes = durationDisplayMinutesOrNull(seconds)
        val label = if (wholeMinutes != null) {
            resources.getQuantityString(R.plurals.nap_caption_minutes, wholeMinutes, wholeMinutes)
        } else {
            val s = seconds.coerceAtLeast(0)
            resources.getQuantityString(R.plurals.nap_caption_seconds, s, s)
        }
        val message = resources.getString(R.string.snackbar_timer_deleted, label)
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = snackbarUndoText,
            duration = SnackbarDuration.Short
        )
        when (result) {
            SnackbarResult.ActionPerformed -> {
                timerController.undo()
                pendingUndoSeconds = null
            }
            SnackbarResult.Dismissed -> pendingUndoSeconds = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    if (gridMode is DurationGridMode.PendingDelete) gridMode = DurationGridMode.Normal
                }
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
                    mode = gridMode,
                    onModeChange = { gridMode = it },
                    onDurationSelected = { seconds ->
                        timerController.addTimer(seconds, 0)
                        timerController.start(seconds)
                    },
                    onCustom = { showCustomSheet = true },
                    onDurationDeleted = { seconds ->
                        timerController.removeFromHistory(seconds)
                        pendingUndoSeconds = seconds
                    }
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
                timerController.addTimer(seconds, 0)
                timerController.start(seconds)
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
    val isPromptVisible = !alarmNotificationsAvailable
    var initialised by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    AnimatedVisibility(
        visible = isPromptVisible,
        enter = if (initialised && !reduceMotion) fadeIn() else EnterTransition.None,
        exit = if (initialised && !reduceMotion) fadeOut() else ExitTransition.None,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.enable_notifications_prompt),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(role = Role.Button, onClick = onClick)
        )
    }

    LaunchedEffect(Unit) {
        initialised = true
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun DurationGrid(
    durations: List<Int>,
    onDurationSelected: (Int) -> Unit,
    onCustom: () -> Unit,
    onDurationDeleted: (Int) -> Unit = {},
    mode: DurationGridMode = DurationGridMode.Normal,
    onModeChange: (DurationGridMode) -> Unit = {}
) {
    val rows = remember(durations) { (durations.take(5).map { it as Int? } + null).chunked(3) }
    Column(
        verticalArrangement = Arrangement.spacedBy(9.dp),
        modifier = Modifier.clickable(
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
            indication = null
        ) {
            if (mode is DurationGridMode.PendingDelete) onModeChange(DurationGridMode.Normal)
        }
    ) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { item ->
                    if (item == null) {
                        val dimmed = mode is DurationGridMode.PendingDelete
                        CustomTile(
                            onClick = {
                                if (mode is DurationGridMode.PendingDelete) onModeChange(DurationGridMode.Normal)
                                else onCustom()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (dimmed) 0.38f else 1f)
                        )
                    } else {
                        val isPendingDelete = (mode as? DurationGridMode.PendingDelete)?.seconds == item
                        val isDimmed = mode is DurationGridMode.PendingDelete && !isPendingDelete
                        DurationTile(
                            seconds = item,
                            isPendingDelete = isPendingDelete,
                            onClick = {
                                when {
                                    isPendingDelete -> {
                                        // do nothing
                                    }
                                    mode is DurationGridMode.PendingDelete -> onModeChange(DurationGridMode.Normal)
                                    else -> onDurationSelected(item)
                                }
                            },
                            onLongClick = if (mode is DurationGridMode.Normal) {
                                { onModeChange(DurationGridMode.PendingDelete(item)) }
                            } else {
                                null
                            },
                            onIconClick = {
                                if (mode is DurationGridMode.PendingDelete) {
                                    onModeChange(DurationGridMode.Normal)
                                    onDurationDeleted(item)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .alpha(if (isDimmed) 0.38f else 1f)
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
internal fun DurationTile(
    seconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPendingDelete: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onIconClick: (() -> Unit)? = null
) {
    val wholeMinutes = durationDisplayMinutesOrNull(seconds)
    val isWhole = wholeMinutes != null
    val errorColor = MaterialTheme.colorScheme.error
    val errorBorderColor = errorColor.copy(alpha = 0.7f)
    val errorBgColor = errorColor.copy(alpha = 0.08f)
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val badgeIconColor = MaterialTheme.colorScheme.onErrorContainer

    val unit = if (isWhole) {
        stringResource(R.string.duration_unit_min)
    } else {
        stringResource(R.string.duration_unit_sec)
    }
    val caption = if (wholeMinutes != null) {
        pluralStringResource(R.plurals.nap_caption_minutes, wholeMinutes, wholeMinutes)
    } else {
        pluralStringResource(R.plurals.nap_caption_seconds, seconds, seconds)
    }
    val startLabel = stringResource(R.string.tile_action_start, caption)
    val deleteLabel = stringResource(R.string.tile_action_delete, caption)

    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .then(
                    if (isPendingDelete) Modifier.border(2.dp, errorBorderColor, MaterialTheme.shapes.extraLarge)
                    else Modifier
                )
                .clip(MaterialTheme.shapes.extraLarge)
                .combinedClickable(
                    role = Role.Button,
                    onClickLabel = startLabel,
                    onLongClickLabel = if (onLongClick != null) deleteLabel else null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .clearAndSetSemantics {
                    contentDescription = startLabel
                    role = Role.Button
                    onClick(label = startLabel) {
                        onClick()
                        true
                    }
                    if (onLongClick != null) {
                        onLongClick(label = deleteLabel) {
                            onLongClick()
                            true
                        }
                    }
                },
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = if (isPendingDelete)
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 1f).let {
                        Color(
                            red = it.red * 0.92f + errorBgColor.red * 0.08f,
                            green = it.green * 0.92f + errorBgColor.green * 0.08f,
                            blue = it.blue * 0.92f + errorBgColor.blue * 0.08f,
                            alpha = 1f
                        )
                    }
                else MaterialTheme.colorScheme.surfaceContainerHigh
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
                    text = wholeMinutes?.toString() ?: seconds.toString(),
                    style = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                        fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (isPendingDelete) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .offset(x = 21.dp, y = (-21).dp)
                    .semantics { contentDescription = deleteLabel }
                    .clickable(onClick = onIconClick ?: {})
                    .padding(13.dp)
                    .background(errorContainerColor, CircleShape)
                    .testTag("delete-badge"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = badgeIconColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
internal fun CustomTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val customDesc = stringResource(R.string.tile_custom_desc)
    Card(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 72.dp)
            .clearAndSetSemantics {
                contentDescription = customDesc
                role = Role.Button
                onClick(label = customDesc) {
                    onClick()
                    true
                }
            }
            .testTag("custom-tile"),
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
                textAlign = TextAlign.Center,
                modifier = Modifier.clearAndSetSemantics {}
            )
            Text(
                text = stringResource(R.string.tile_custom),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}

private fun mapCustomDurationKey(key: Key): String? = when (key) {
    Key.Zero, Key.NumPad0 -> "0"
    Key.One, Key.NumPad1 -> "1"
    Key.Two, Key.NumPad2 -> "2"
    Key.Three, Key.NumPad3 -> "3"
    Key.Four, Key.NumPad4 -> "4"
    Key.Five, Key.NumPad5 -> "5"
    Key.Six, Key.NumPad6 -> "6"
    Key.Seven, Key.NumPad7 -> "7"
    Key.Eight, Key.NumPad8 -> "8"
    Key.Nine, Key.NumPad9 -> "9"
    Key.Backspace, Key.Delete -> "⌫"
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CustomDurationSheet(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputBuffer by rememberSaveable { mutableStateOf("") }

    val parsedSeconds = parseCustomDurationSeconds(inputBuffer)
    val isStartEnabled = parsedSeconds != null && isCustomDurationInRange(parsedSeconds)
    val isOutOfRange = !isStartEnabled && parsedSeconds != null

    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val animatedCursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )
    val cursorAlpha = if (reduceMotion) 1f else animatedCursorAlpha

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f)
    ) {
        CustomDurationSheetBody(
            inputBuffer = inputBuffer,
            parsedSeconds = parsedSeconds,
            isOutOfRange = isOutOfRange,
            isStartEnabled = isStartEnabled,
            cursorAlpha = cursorAlpha,
            onBufferChange = { inputBuffer = it },
            onStart = onStart,
            onDismiss = onDismiss
        )
    }
}

@Composable
internal fun CustomDurationSheetBody(
    inputBuffer: String,
    parsedSeconds: Int?,
    isOutOfRange: Boolean,
    isStartEnabled: Boolean,
    cursorAlpha: Float,
    onBufferChange: (String) -> Unit,
    onStart: (Int) -> Unit,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .testTag("CustomDurationSheetBody")
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.Escape -> { onDismiss(); true }
                    Key.Enter, Key.NumPadEnter -> {
                        if (isStartEnabled && parsedSeconds != null) {
                            onStart(parsedSeconds); true
                        } else false
                    }
                    else -> {
                        val action = mapCustomDurationKey(event.key)
                        if (action != null) { onBufferChange(appendToBuffer(inputBuffer, action)); true }
                        else false
                    }
                }
            }
    ) {
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
            onKey = { key -> onBufferChange(appendToBuffer(inputBuffer, key)) },
            onStart = onStart
        )
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
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
                color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha),
                modifier = Modifier.clearAndSetSemantics {}
            )
        }
    }
}

@Composable
internal fun NumericKeypad(
    onKey: (String) -> Unit,
    buffer: String,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
    spacing: Dp = 8.dp,
    keyFontSize: TextUnit = 23.sp
) {
    val digitRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9")
    )
    val state = keypadState(buffer)
    val backspaceDesc = stringResource(R.string.keypad_backspace_desc)

    @Composable
    fun RowScope.digitKey(digit: String) {
        KeypadButton(
            label = digit,
            onClick = { onKey(digit) },
            enabled = isDigitAllowed(state, buffer, digit.toInt()),
            isSpecial = false,
            height = keyHeight,
            fontSize = keyFontSize,
            modifier = Modifier.weight(1f)
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        digitRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { digit -> digitKey(digit) }
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            digitKey("0")
            KeypadButton(
                label = "⌫",
                onClick = { onKey("⌫") },
                enabled = buffer.isNotEmpty(),
                isSpecial = true,
                contentDescription = backspaceDesc,
                height = keyHeight,
                fontSize = keyFontSize,
                modifier = Modifier.weight(1f)
            )
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
    contentDescription: String? = null,
    height: Dp = 56.dp,
    fontSize: TextUnit = 23.sp
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = height),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = height),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = fontSize,
                color = if (isSpecial) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = if (contentDescription != null)
                    Modifier.clearAndSetSemantics { this.contentDescription = contentDescription }
                else
                    Modifier
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
        isStartEnabled && parsedSeconds != null -> {
            val minutes = parsedSeconds / 60
            val secs = parsedSeconds % 60
            when {
                minutes == 0 -> pluralStringResource(R.plurals.nap_desc_seconds, secs, secs)
                else -> pluralStringResource(R.plurals.nap_desc_minutes, minutes, minutes)
            }
        }
        else -> ""
    }
    val emptyDesc = stringResource(R.string.custom_duration_empty_desc)
    val valueDisplayDesc = when {
        inputBuffer.isEmpty() -> emptyDesc
        isOutOfRange -> errorText
        helperText.isNotEmpty() -> helperText
        else -> inputBuffer
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
                // Pin value display and keypad to LTR so digit order doesn't mirror in RTL locales.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics { contentDescription = valueDisplayDesc },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        ValueDisplay(inputBuffer = inputBuffer, cursorAlpha = cursorAlpha, fontSize = 44.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = helperText,
                        style = MaterialTheme.typography.bodySmall,
                        color = helperColor
                    )
                }
                Spacer(Modifier.height(16.dp))
                StartDurationButton(
                    text = startText,
                    enabled = isStartEnabled,
                    parsedSeconds = parsedSeconds,
                    onStart = onStart
                )
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                NumericKeypad(
                    onKey = onKey,
                    buffer = inputBuffer,
                    modifier = Modifier.weight(1.25f),
                    keyHeight = 48.dp,
                    keyFontSize = 21.sp
                )
            }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clearAndSetSemantics { contentDescription = valueDisplayDesc },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    ValueDisplay(inputBuffer = inputBuffer, cursorAlpha = cursorAlpha)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall,
                    color = helperColor
                )
            }
            Spacer(Modifier.height(20.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                NumericKeypad(
                    onKey = onKey,
                    buffer = inputBuffer
                )
            }
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
            .heightIn(min = 56.dp),
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

@Preview(showBackground = true, name = "SetupScreen - 200% font scale", fontScale = 2f)
@Composable
private fun SetupScreenFontScalePreview() {
    EasyNapTheme {
        SetupScreenStateless(
            history = TimerPreferenceStore.DEFAULT_HISTORY,
            onDurationSelected = {},
            onCustom = {}
        )
    }
}

@Preview(showBackground = true, name = "SetupScreen - RTL", locale = "ar")
@Composable
private fun SetupScreenRtlPreview() {
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
    EasyNapTheme { DurationTile(seconds = 600, onClick = {}) }
}

@Preview(showBackground = true, name = "DurationTile - pending delete")
@Composable
private fun DurationTilePendingDeletePreview() {
    EasyNapTheme { DurationTile(seconds = 600, onClick = {}, isPendingDelete = true) }
}

@Preview(showBackground = true, name = "DurationTile - fractional")
@Composable
private fun DurationTileFractionalPreview() {
    EasyNapTheme { DurationTile(seconds = 90, onClick = {}) }
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
        CustomDurationSheetStateless(inputBuffer = "0:30", cursorAlpha = 0f, onKey = {}, onStart = {})
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
            inputBuffer = "0:30",
            cursorAlpha = 0f,
            onKey = {},
            onStart = {},
            compactLandscape = true
        )
    }
}

@Preview(showBackground = true, name = "CustomDurationSheet - 200% font scale", fontScale = 2f)
@Composable
private fun CustomDurationSheetFontScalePreview() {
    EasyNapTheme {
        CustomDurationSheetStateless(inputBuffer = "0:30", cursorAlpha = 1f, onKey = {}, onStart = {})
    }
}

@Preview(showBackground = true, name = "CustomDurationSheet - RTL", locale = "ar")
@Composable
private fun CustomDurationSheetRtlPreview() {
    EasyNapTheme {
        CustomDurationSheetStateless(inputBuffer = "0:30", cursorAlpha = 1f, onKey = {}, onStart = {})
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
    EasyNapTheme { ValueDisplay(inputBuffer = "0:30", cursorAlpha = 0f) }
}

@Preview(showBackground = true, name = "NumericKeypad - empty buffer")
@Composable
private fun NumericKeypadEmptyBufferPreview() {
    EasyNapTheme { NumericKeypad(onKey = {}, buffer = "") }
}

@Preview(showBackground = true, name = "NumericKeypad - constrained buffer")
@Composable
private fun NumericKeypadConstrainedBufferPreview() {
    EasyNapTheme { NumericKeypad(onKey = {}, buffer = "13") }
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
    history: List<Int>,
    onDurationSelected: (Int) -> Unit,
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
            Text("Easy Nap", style = MaterialTheme.typography.titleLarge)
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
    val isStartEnabled = parsedSeconds != null && isCustomDurationInRange(parsedSeconds)
    val isOutOfRange = !isStartEnabled && parsedSeconds != null
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
