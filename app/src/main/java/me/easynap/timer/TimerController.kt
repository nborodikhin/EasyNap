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
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _history = MutableStateFlow(me.easynap.data.TimerPreferenceStore.DEFAULT_HISTORY)
    val history: StateFlow<List<Int>> = _history.asStateFlow()

    private val _napDurationSeconds = MutableStateFlow(0)
    val napDurationSeconds: StateFlow<Int> = _napDurationSeconds.asStateFlow()

    private data class PendingUndo(val seconds: Int, val position: Int)
    private var pendingUndo: PendingUndo? = null

    init {
        scope.launch {
            store.history.collect { _history.value = it }
        }
        scope.launch {
            store.napDurationSeconds.collect { _napDurationSeconds.value = it }
        }
        scope.launch {
            val activeTimer = store.loadActiveTimer()
            _state.value = if (activeTimer != null) {
                TimerState.Running(activeTimer.endAtMillis, activeTimer.durationSeconds)
            } else {
                store.clearActiveTimer()
                TimerState.Idle
            }
        }
    }

    fun addTimer(durationSeconds: Int, position: Int = 0) {
        scope.launch { store.addToHistory(durationSeconds, position) }
    }

    fun removeFromHistory(durationSeconds: Int) {
        val position = _history.value.indexOf(durationSeconds).takeIf { it >= 0 } ?: return
        pendingUndo = PendingUndo(durationSeconds, position)
        scope.launch {
            store.removeFromHistory(durationSeconds)
            delay(UNDO_WINDOW_MS)
            pendingUndo = null
        }
    }

    fun undo() {
        val undo = pendingUndo ?: return
        pendingUndo = null
        scope.launch { store.addToHistory(undo.seconds, undo.position) }
    }

    fun start(durationSeconds: Int) {
        _napDurationSeconds.value = durationSeconds
        startInternal(durationSeconds, isSnooze = false)
    }

    fun startSnooze(durationSeconds: Int) {
        startInternal(durationSeconds, isSnooze = true)
    }

    private fun startInternal(durationSeconds: Int, isSnooze: Boolean) {
        val durationMs = durationSeconds * 1000L
        val pad = if (durationSeconds >= 60) PAD_MS_LONG else PAD_MS_SHORT
        val endAt = System.currentTimeMillis() + durationMs + pad
        scope.launch {
            store.startTimer(endAt, durationSeconds, updateNapDuration = !isSnooze)
            _state.value = TimerState.Running(endAt, durationSeconds, isSnooze = isSnooze)
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
        val pi = alarmPendingIntent()
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(endAt, pi), pi)
    }

    private fun cancelAlarm() {
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
