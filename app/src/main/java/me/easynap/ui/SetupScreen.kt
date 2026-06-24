package me.easynap.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.easynap.TimerController
import me.easynap.appendToBuffer
import me.easynap.formatDurationLabel
import me.easynap.formatDurationUnit
import me.easynap.formatNapDescription
import me.easynap.isCustomDurationInRange
import me.easynap.parseCustomDurationSeconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen() {
    var showCustomSheet by remember { mutableStateOf(false) }
    val history by TimerController.history.collectAsStateWithLifecycle()

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(32.dp))
            Text(
                "Easy Nap",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Ready to rest?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap a length to start your nap.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            DurationGrid(
                durations = history,
                onDurationSelected = { TimerController.start(it) },
                onCustom = { showCustomSheet = true }
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showCustomSheet) {
        CustomDurationSheet(
            onDismiss = { showCustomSheet = false },
            onStart = { seconds ->
                showCustomSheet = false
                TimerController.start(seconds / 60f)
            }
        )
    }
}

@Composable
private fun DurationGrid(
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
private fun DurationTile(minutes: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
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
private fun CustomTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
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
private fun CustomDurationSheet(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var inputBuffer by remember { mutableStateOf("") }

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
        scrimColor = Color(0x8C080C0B)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Custom length",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(20.dp))

            ValueDisplay(inputBuffer = inputBuffer, cursorAlpha = cursorAlpha)
            Spacer(Modifier.height(6.dp))

            val helperColor = if (isOutOfRange) MaterialTheme.colorScheme.error
                              else MaterialTheme.colorScheme.onSurface
            val helperText = when {
                isOutOfRange -> "Enter 0:05 – 120 min"
                isStartEnabled && parsedSeconds != null -> formatNapDescription(parsedSeconds)
                else -> ""
            }
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = helperColor
            )
            Spacer(Modifier.height(20.dp))

            NumericKeypad(
                onKey = { key -> inputBuffer = appendToBuffer(inputBuffer, key) },
                colonEnabled = ':' !in inputBuffer
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { if (parsedSeconds != null) onStart(parsedSeconds) },
                enabled = isStartEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = CircleShape
            ) {
                Text("Start nap", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ValueDisplay(inputBuffer: String, cursorAlpha: Float) {
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
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Light,
                    fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "|",
                style = TextStyle(fontSize = 52.sp, fontWeight = FontWeight.Light),
                color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha)
            )
        }
    }
}

@Composable
private fun NumericKeypad(onKey: (String) -> Unit, colonEnabled: Boolean) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(":", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isSpecial: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.38f)
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 23.sp,
                color = if (isSpecial) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
