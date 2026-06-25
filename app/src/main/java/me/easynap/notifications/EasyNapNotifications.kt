package me.easynap.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

object EasyNapNotifications {
    const val CHANNEL_TIMER = "timer_channel"
    const val CHANNEL_ALARM = "alarm_channel"

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)

        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            context.getString(me.easynap.R.string.notification_channel_timer),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
        }
        nm.createNotificationChannel(timerChannel)

        val alarmChannel = NotificationChannel(
            CHANNEL_ALARM,
            context.getString(me.easynap.R.string.notification_channel_alarm),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        nm.createNotificationChannel(alarmChannel)
    }

    fun alarmNotificationsAvailable(
        appNotificationsEnabled: Boolean,
        alarmChannelImportance: Int?
    ): Boolean =
        appNotificationsEnabled &&
            alarmChannelImportance != null &&
            alarmChannelImportance != NotificationManager.IMPORTANCE_NONE

    fun areAlarmNotificationsAvailable(context: Context): Boolean {
        ensureChannels(context)
        val nm = context.getSystemService(NotificationManager::class.java)
        val alarmImportance = nm.getNotificationChannel(CHANNEL_ALARM)?.importance
        return alarmNotificationsAvailable(
            appNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            alarmChannelImportance = alarmImportance
        )
    }

    fun alarmNotificationSettingsIntent(context: Context): Intent {
        ensureChannels(context)
        return Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ALARM)
        }
    }

    fun appNotificationSettingsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
}
