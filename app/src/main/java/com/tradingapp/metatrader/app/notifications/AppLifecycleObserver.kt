package com.tradingapp.metatrader.app.notifications

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class AppLifecycleObserver(
    private val appContext: Context
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        // Foreground -> لا نحتاج ForegroundService
        TradingForegroundService.stop(appContext)
    }

    override fun onStop(owner: LifecycleOwner) {
        // Background -> شغل الخدمة فقط إذا مطلوب
        if (TradingServiceGate.shouldRunInBackground.value) {
            TradingForegroundService.start(appContext)
        }
    }
}
