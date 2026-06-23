package me.easynap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, NapTimerService::class.java))
        TimerController.completeTimer()
        context.startForegroundService(Intent(context, AlarmService::class.java))
    }
}
