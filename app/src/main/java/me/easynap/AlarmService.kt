package me.easynap

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

class AlarmService : Service() {

    companion object {
        const val NOTIF_ID_ALARM = 2
        const val ACTION_STOP = "me.easynap.ACTION_STOP_ALARM"

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
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var vibrator: Vibrator? = null
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val fadeStepsTotal = (FADE_IN_MS / FADE_STEP_MS).toInt()
    private var fadeStep = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }

        acquireWakeLock()

        val alarmActivityIntent = Intent(this, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPi = PendingIntent.getActivity(
            this, 0, alarmActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        startForeground(
            NOTIF_ID_ALARM,
            NotificationCompat.Builder(this, NapTimerService.CHANNEL_ALARM)
                .setContentTitle("EasyNap")
                .setContentText("Time to wake up!")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPi, true)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
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
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        sendBroadcast(Intent(AlarmActivity.ACTION_FINISH))
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
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmService, uri)
            isLooping = true
            prepare()
            setVolume(0f, 0f)
            start()
        }

        fadeStep = 0
        scheduleFadeInStep()
    }

    private fun scheduleFadeInStep() {
        if (fadeStep >= fadeStepsTotal) {
            mediaPlayer?.setVolume(1f, 1f)
            return
        }
        handler.postDelayed({
            val v = fadeStep.toFloat() / fadeStepsTotal
            mediaPlayer?.setVolume(v, v)
            fadeStep++
            scheduleFadeInStep()
        }, FADE_STEP_MS)
    }

    private fun startAudioFadeOut() {
        fadeStep = fadeStepsTotal
        scheduleFadeOutStep()
    }

    private fun scheduleFadeOutStep() {
        if (fadeStep <= 0) {
            mediaPlayer?.setVolume(0f, 0f)
            return
        }
        handler.postDelayed({
            val v = fadeStep.toFloat() / fadeStepsTotal
            mediaPlayer?.setVolume(v, v)
            fadeStep--
            scheduleFadeOutStep()
        }, FADE_STEP_MS)
    }
}
