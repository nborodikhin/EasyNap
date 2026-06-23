package me.easynap

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerController {

    private const val PREFS_NAME = "easynap_prefs"
    private const val KEY_END_AT = "end_at_millis"
    private const val PAD_MS = 5_000L

    private lateinit var prefs: SharedPreferences
    private lateinit var appContext: Context

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    fun init(context: Context) {
        if (::appContext.isInitialized) return
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val endAt = prefs.getLong(KEY_END_AT, 0L)
        _state.value = if (endAt > System.currentTimeMillis()) TimerState.Running(endAt) else TimerState.Idle
    }

    fun start(durationMinutes: Float) {
        val durationMs = (durationMinutes * 60_000).toLong()
        val pad = if (durationMinutes >= 1f) PAD_MS else 0L
        val endAt = System.currentTimeMillis() + durationMs + pad
        prefs.edit().putLong(KEY_END_AT, endAt).apply()
        _state.value = TimerState.Running(endAt)
        startCountdownService()
        scheduleAlarm(endAt)
    }

    fun cancel() {
        prefs.edit().remove(KEY_END_AT).apply()
        _state.value = TimerState.Idle
        stopCountdownService()
        cancelAlarm()
    }

    fun completeTimer() {
        prefs.edit().remove(KEY_END_AT).apply()
        _state.value = TimerState.Idle
    }

    fun stopCountdownService() {
        appContext.stopService(Intent(appContext, NapTimerService::class.java))
    }

    private fun startCountdownService() {
        appContext.startForegroundService(Intent(appContext, NapTimerService::class.java))
    }

    private fun scheduleAlarm(endAt: Long) {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = alarmPendingIntent()
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(endAt, pi), pi)
    }

    private fun cancelAlarm() {
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(appContext, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
