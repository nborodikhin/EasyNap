package me.easynap

import android.app.Application

class EasyNapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TimerController.init(this)
    }
}
