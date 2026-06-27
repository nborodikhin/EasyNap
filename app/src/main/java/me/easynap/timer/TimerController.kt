package me.easynap.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.easynap.alarm.AlarmReceiver
import me.easynap.data.TimerStore
import me.easynap.service.NapTimerService

@Singleton
class TimerController @Inject constructor(
    private val store: TimerStore,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PAD_MS_SHORT = 990L
        private const val PAD_MS_LONG = 1_990L
        private const val UNDO_WINDOW_MS = 5_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _history = MutableStateFlow(me.easynap.data.TimerPreferenceStore.DEFAULT_HISTORY)
    val history: StateFlow<List<Float>> = _history.asStateFlow()

    private val _napDurationMinutes = MutableStateFlow(0f)
    val napDurationMinutes: StateFlow<Float> = _napDurationMinutes.asStateFlow()

    private data class PendingUndo(val minutes: Float, val position: Int)
    private var pendingUndo: PendingUndo? = null

    init {
        scope.launch {
            store.history.collect { _history.value = it }
        }
        scope.launch {
            store.napDurationMinutes.collect { _napDurationMinutes.value = it }
        }
        scope.launch {
            val activeTimer = store.loadActiveTimer()
            _state.value = if (activeTimer != null) {
                TimerState.Running(activeTimer.endAtMillis, activeTimer.durationMinutes)
            } else {
                store.clearActiveTimer()
                TimerState.Idle
            }
        }
    }

    fun addTimer(durationMinutes: Float, position: Int = 0) {
        scope.launch { store.addToHistory(durationMinutes, position) }
    }

    fun removeFromHistory(durationMinutes: Float) {
        val position = _history.value.indexOf(durationMinutes).takeIf { it >= 0 } ?: return
        pendingUndo = PendingUndo(durationMinutes, position)
        scope.launch {
            store.removeFromHistory(durationMinutes)
            delay(UNDO_WINDOW_MS)
            pendingUndo = null
        }
    }

    fun undo() {
        val undo = pendingUndo ?: return
        pendingUndo = null
        scope.launch { store.addToHistory(undo.minutes, undo.position) }
    }

    fun start(durationMinutes: Float) {
        _napDurationMinutes.value = durationMinutes
        startInternal(durationMinutes, isSnooze = false)
    }

    fun startSnooze(durationMinutes: Float) {
        startInternal(durationMinutes, isSnooze = true)
    }

    private fun startInternal(durationMinutes: Float, isSnooze: Boolean) {
        val durationMs = (durationMinutes * 60_000).toLong()
        val pad = if (durationMinutes >= 1f) PAD_MS_LONG else PAD_MS_SHORT
        val endAt = System.currentTimeMillis() + durationMs + pad
        scope.launch {
            store.startTimer(endAt, durationMinutes)
            _state.value = TimerState.Running(endAt, durationMinutes, isSnooze = isSnooze)
            startCountdownService()
            scheduleAlarm(endAt)
        }
    }

    fun cancel() {
        _state.value = TimerState.Idle
        scope.launch { store.clearActiveTimer() }
        stopCountdownService()
        cancelAlarm()
    }

    fun completeTimer() {
        _state.value = TimerState.Idle
        scope.launch { store.clearActiveTimer() }
    }

    fun stopCountdownService() {
        context.stopService(Intent(context, NapTimerService::class.java))
    }

    private fun startCountdownService() {
        context.startForegroundService(Intent(context, NapTimerService::class.java))
    }

    private fun scheduleAlarm(endAt: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = alarmPendingIntent()
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(endAt, pi), pi)
    }

    private fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(alarmPendingIntent())
    }

    private fun alarmPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
