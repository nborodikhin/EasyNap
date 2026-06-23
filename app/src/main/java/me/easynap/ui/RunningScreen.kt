package me.easynap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.easynap.TimerController
import me.easynap.TimerState
import me.easynap.formatRemainingTime

@Composable
fun RunningScreen(state: TimerState.Running) {
    var remainingMs by remember { mutableLongStateOf(state.endAtMillis - System.currentTimeMillis()) }

    LaunchedEffect(state.endAtMillis) {
        while (remainingMs > 0) {
            delay(1_000)
            remainingMs = state.endAtMillis - System.currentTimeMillis()
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatRemainingTime(remainingMs.coerceAtLeast(0)),
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = { TimerController.cancel() },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text("Cancel", fontSize = 18.sp)
            }
        }
    }
}
