package me.easynap.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import me.easynap.service.NapTimerService
import me.easynap.timer.TimerController

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var timerController: TimerController

    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, NapTimerService::class.java))
        timerController.completeTimer()
        context.startForegroundService(Intent(context, AlarmService::class.java))
    }
}
