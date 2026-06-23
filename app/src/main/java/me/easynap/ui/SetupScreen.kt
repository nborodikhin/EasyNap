package me.easynap.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.easynap.TimerController
import me.easynap.parseDurationMinutes

@Composable
fun SetupScreen() {
    var durationInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = durationInput,
                    onValueChange = { durationInput = it; inputError = false },
                    label = { Text("Timer duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = inputError,
                    supportingText = if (inputError) ({ Text("Enter a positive number") }) else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val minutes = parseDurationMinutes(durationInput)
                        if (minutes != null) {
                            TimerController.start(minutes)
                        } else {
                            inputError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("Start Timer")
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    "Or quick start:",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
            }

            val quickStartMinutes = (5..60 step 5).toList()
            items(quickStartMinutes) { minutes ->
                Button(
                    onClick = { TimerController.start(minutes.toFloat()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                        .padding(bottom = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        "$minutes minutes",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
