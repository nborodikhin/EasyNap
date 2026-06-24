package me.easynap

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.TimerController
import me.easynap.timer.TimerState
import me.easynap.ui.RunningScreen
import me.easynap.ui.SetupScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var timerController: TimerController

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* proceed regardless of grant */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            EasyNapTheme {
                val timerState by timerController.state.collectAsStateWithLifecycle()
                when (timerState) {
                    is TimerState.Idle -> SetupScreen(timerController)
                    is TimerState.Running -> RunningScreen(timerState as TimerState.Running, timerController)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
