package me.easynap.service

import android.app.Notification
import android.app.NotificationChannel
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
import me.easynap.timer.formatRemainingTime

@AndroidEntryPoint
class NapTimerService : Service() {

    companion object {
        const val CHANNEL_TIMER = "timer_channel"
        const val CHANNEL_ALARM = "alarm_channel"
        const val NOTIF_ID_TIMER = 1
    }

    @Inject internal lateinit var store: TimerStore

    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var endAtMillis = 0L

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
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID_TIMER, buildNotification(0L))
        serviceScope.launch {
            val activeTimer = store.loadActiveTimer()
            if (activeTimer == null) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf(startId)
                return@launch
            }
            endAtMillis = activeTimer.endAtMillis
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

    private fun createNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val timerChannel = NotificationChannel(CHANNEL_TIMER, "Timer", NotificationManager.IMPORTANCE_LOW).apply {
            setSound(null, null)
        }
        nm.createNotificationChannel(timerChannel)

        val alarmChannel = NotificationChannel(CHANNEL_ALARM, "Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(alarmChannel)
    }

    private fun buildNotification(remainingMs: Long): Notification {
        val openAppPi = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_TIMER)
            .setContentTitle(getString(R.string.notif_app_name))
            .setContentText(getString(R.string.notif_timer_remaining, formatRemainingTime(remainingMs)))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPi)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(remainingMs: Long) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIF_ID_TIMER, buildNotification(remainingMs))
    }
}
