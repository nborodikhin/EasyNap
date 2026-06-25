package me.easynap

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.easynap.alarm.AlarmActivity
import me.easynap.alarm.AlarmService
import me.easynap.notifications.EasyNapNotifications
import me.easynap.theme.EasyNapTheme
import me.easynap.timer.TimerController
import me.easynap.timer.TimerState
import me.easynap.ui.RunningScreen
import me.easynap.ui.SetupScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var timerController: TimerController
    private var alarmNotificationsAvailable by mutableStateOf(true)
    private var alarmLaunchJob: Job? = null

    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshAlarmNotificationAvailability() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EasyNapNotifications.ensureChannels(this)
        refreshAlarmNotificationAvailability()
        requestNotificationPermissionIfNeeded()

        enableEdgeToEdge()
        setContent {
            EasyNapTheme {
                val timerState by timerController.state.collectAsStateWithLifecycle()
                when (timerState) {
                    is TimerState.Idle -> SetupScreen(
                        timerController = timerController,
                        alarmNotificationsAvailable = alarmNotificationsAvailable,
                        onEnableNotificationsClick = ::openNotificationSettings
                    )
                    is TimerState.Running -> RunningScreen(timerState as TimerState.Running, timerController)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAlarmNotificationAvailability()
        if (AlarmService.isRunning) startAlarmActivity()
        scheduleAlarmActivityLaunch()
    }

    override fun onPause() {
        super.onPause()
        alarmLaunchJob?.cancel()
    }

    private fun startAlarmActivity() {
        startActivity(Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
    }

    private fun scheduleAlarmActivityLaunch() {
        alarmLaunchJob?.cancel()
        alarmLaunchJob = lifecycleScope.launch {
            var innerJob: Job? = null
            timerController.state.collect { state ->
                innerJob?.cancel()
                if (state is TimerState.Running) {
                    innerJob = launch {
                        val delayMs = state.endAtMillis - System.currentTimeMillis()
                        if (delayMs > 0) delay(delayMs)
                        startAlarmActivity()
                    }
                } else {
                    innerJob = null
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

    private fun refreshAlarmNotificationAvailability() {
        alarmNotificationsAvailable = EasyNapNotifications.areAlarmNotificationsAvailable(this)
    }

    private fun openNotificationSettings() {
        try {
            startActivity(EasyNapNotifications.alarmNotificationSettingsIntent(this))
        } catch (_: ActivityNotFoundException) {
            startActivity(EasyNapNotifications.appNotificationSettingsIntent(this))
        }
    }
}
