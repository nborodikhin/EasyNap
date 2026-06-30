package me.easynap.alarm

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.easynap.R
import me.easynap.notifications.EasyNapNotifications
import me.easynap.service.NapTimerService
import me.easynap.timer.TimerController
import me.easynap.timer.durationDisplayMinutesOrNull
import me.easynap.timer.notifDurationPrefix

@AndroidEntryPoint
class AlarmService : Service() {

    companion object {
        const val NOTIF_ID_ALARM = 2
        const val ACTION_STOP = "me.easynap.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE = "me.easynap.ACTION_SNOOZE_ALARM"
        @Volatile var isRunning: Boolean = false

        // Alarm sequence timeline:
        // 0 s        vibration starts, no sound
        // 5 s        audio starts at volume 0, fades in over 10 s
        // 15 s       full volume
        // 45 s       volume fades out over 10 s
        // 55 s       auto-stop (vibration and sound end together)
        private const val SOUND_START_MS = 5_000L
        private const val FADE_IN_MS = 10_000L
        private const val FULL_VOLUME_MS = 30_000L
        private const val FADE_OUT_START_MS = SOUND_START_MS + FADE_IN_MS + FULL_VOLUME_MS  // 45 s
        private const val FADE_OUT_MS = 10_000L
        private const val TOTAL_TIMEOUT_MS = FADE_OUT_START_MS + FADE_OUT_MS               // 55 s
        private const val FADE_STEP_MS = 100L

        private const val REQUEST_FULL_SCREEN = 0
        private const val REQUEST_SNOOZE = 1
        private const val REQUEST_STOP = 2
    }

    @Inject internal lateinit var timerController: TimerController

    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val fadeStepsTotal = (FADE_IN_MS / FADE_STEP_MS).toInt()
    private var fadeStep = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopAlarm()
                return START_NOT_STICKY
            }
            ACTION_SNOOZE -> {
                stopAlarm()
                timerController.startSnooze(60)
                return START_NOT_STICKY
            }
        }
        isRunning = true

        EasyNapNotifications.ensureChannels(this)
        acquireWakeLock()

        val alarmActivityIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPi = PendingIntent.getActivity(
            this, REQUEST_FULL_SCREEN, alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = PendingIntent.getForegroundService(
            this, REQUEST_SNOOZE,
            Intent(this, AlarmService::class.java).apply { action = ACTION_SNOOZE },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getForegroundService(
            this, REQUEST_STOP,
            Intent(this, AlarmService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val durationSeconds = timerController.napDurationSeconds.value
        val title = if (durationSeconds > 0) {
            getString(R.string.notif_alarm_title, resources.notifDurationPrefix(durationSeconds))
        } else {
            getString(R.string.notif_app_name)
        }

        startForeground(
            NOTIF_ID_ALARM,
            NotificationCompat.Builder(this, EasyNapNotifications.CHANNEL_ALARM)
                .setContentTitle(title)
                .setContentText(getString(R.string.notif_alarm_text))
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPi, true)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(R.drawable.ic_snooze_24dp, getString(R.string.notif_action_snooze), snoozeIntent)
                .addAction(R.drawable.ic_alarm_off_24, getString(R.string.notif_action_stop), stopIntent)
                .build()
        )

        // Alarm-clock exemption grants a background-activity-start window.
        startActivity(alarmActivityIntent)

        // Vibration runs for the full duration of the alarm sequence.
        startVibration()

        // At 5 s: start audio at 0 volume and fade in.
        handler.postDelayed({ startAudioFadeIn() }, SOUND_START_MS)

        // At 45 s: begin fade-out.
        handler.postDelayed({ startAudioFadeOut() }, FADE_OUT_START_MS)

        // At 55 s: auto-stop everything.
        handler.postDelayed({ stopAlarm() }, TOTAL_TIMEOUT_MS)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        wakeLock?.release()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopAlarm() {
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        sendBroadcast(Intent(AlarmActivity.ACTION_FINISH).setPackage(packageName))
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EasyNap:AlarmWakeLock")
        wakeLock?.acquire(TOTAL_TIMEOUT_MS + 5_000L)
    }

    @Suppress("DEPRECATION")
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
    }

    private fun startAudioFadeIn() {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(attrs)
                setDataSource(this@AlarmService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                isLooping = true
                prepare()
                setVolume(0f, 0f)
                start()
            }
        } catch (_: Exception) {
            mediaPlayer?.release()
            mediaPlayer = null
            try {
                val afd = resources.openRawResourceFd(R.raw.helium)
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(attrs)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    isLooping = true
                    prepare()
                    setVolume(0f, 0f)
                    start()
                }
            } catch (_: Exception) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        fadeStep = 0
        scheduleFadeStep(FadeDirection.FadeIn)
    }

    private enum class FadeDirection { FadeIn, FadeOut }

    private fun startAudioFadeOut() {
        fadeStep = fadeStepsTotal
        scheduleFadeStep(FadeDirection.FadeOut)
    }

    private fun scheduleFadeStep(direction: FadeDirection) {
        val done = if (direction == FadeDirection.FadeIn) fadeStep >= fadeStepsTotal else fadeStep <= 0
        if (done) {
            val finalVol = if (direction == FadeDirection.FadeIn) 1f else 0f
            mediaPlayer?.setVolume(finalVol, finalVol)
            return
        }
        handler.postDelayed({
            val v = fadeStep.toFloat() / fadeStepsTotal
            mediaPlayer?.setVolume(v, v)
            fadeStep += if (direction == FadeDirection.FadeIn) 1 else -1
            scheduleFadeStep(direction)
        }, FADE_STEP_MS)
    }
}
