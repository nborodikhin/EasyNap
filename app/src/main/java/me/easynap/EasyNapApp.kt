package me.easynap

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.easynap.notifications.EasyNapNotifications

@HiltAndroidApp
class EasyNapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        EasyNapNotifications.ensureChannels(this)
    }
}
