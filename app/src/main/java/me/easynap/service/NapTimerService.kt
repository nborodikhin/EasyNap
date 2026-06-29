package me.easynap.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.easynap.MainActivity
import me.easynap.R
import me.easynap.data.TimerStore
import me.easynap.notifications.EasyNapNotifications
import me.easynap.timer.TimerController
import me.easynap.timer.durationTotalSeconds
import me.easynap.timer.durationDisplayMinutesOrNull
import me.easynap.timer.formatRemainingTime

@AndroidEntryPoint
class NapTimerService : Service() {

    companion object {
        const val CHANNEL_TIMER = EasyNapNotifications.CHANNEL_TIMER
        const val CHANNEL_ALARM = EasyNapNotifications.CHANNEL_ALARM
        const val NOTIF_ID_TIMER = 1
        const val ACTION_STOP = "me.easynap.ACTION_STOP_TIMER"

        private const val REQUEST_OPEN_APP = 0
        private const val REQUEST_STOP = 1
    }

    @Inject internal lateinit var store: TimerStore
    @Inject internal lateinit var timerController: TimerController

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var endAtMillis = 0L
    private var durationMinutes = 0f

    private val tick = object : Runnable {
        override fun run() {
            val remaining = endAtMillis - System.currentTimeMillis()
            if (remaining <= 0) {
                stopSelf()
                return
            }
            updateNotification(remaining)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        EasyNapNotifications.ensureChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            timerController.cancel()
            return START_NOT_STICKY
        }

        startForeground(NOTIF_ID_TIMER, buildNotification(0L))
        serviceScope.launch {
            val activeTimer = store.loadActiveTimer()
            if (activeTimer == null) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
                return@launch
            }
            endAtMillis = activeTimer.endAtMillis
            durationMinutes = activeTimer.durationMinutes
            val remaining = endAtMillis - System.currentTimeMillis()
            updateNotification(remaining)
            handler.removeCallbacks(tick)
            handler.post(tick)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(remainingMs: Long): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, REQUEST_OPEN_APP,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getForegroundService(
            this, REQUEST_STOP,
            Intent(this, NapTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val title = if (durationMinutes > 0f) {
            val wholeMinutes = durationDisplayMinutesOrNull(durationMinutes)
            val prefix = if (wholeMinutes != null) {
                resources.getQuantityString(R.plurals.notif_duration_min, wholeMinutes, wholeMinutes)
            } else {
                val seconds = durationTotalSeconds(durationMinutes).coerceAtLeast(0)
                resources.getQuantityString(R.plurals.notif_duration_sec, seconds, seconds)
            }
            getString(R.string.notif_timer_title, prefix)
        } else {
            getString(R.string.notif_app_name)
        }
        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setContentTitle(title)
            .setContentText(getString(R.string.notif_timer_remaining, formatRemainingTime(remainingMs)))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(
                R.drawable.ic_alarm_off_24,
                getString(R.string.notif_action_stop),
                stopIntent
            )
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_TIMER, buildNotification(remainingMs))
    }
}
