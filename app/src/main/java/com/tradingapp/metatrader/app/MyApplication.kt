package com.tradingapp.metatrader.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.tradingapp.metatrader.app.notifications.AppLifecycleObserver
import com.tradingapp.metatrader.app.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(AppLifecycleObserver(applicationContext))
    }
}
