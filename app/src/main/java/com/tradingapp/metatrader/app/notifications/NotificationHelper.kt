package com.tradingapp.metatrader.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val serviceChannel = NotificationChannel(
            NotificationChannels.SERVICE_CHANNEL_ID,
            NotificationChannels.SERVICE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps market streaming alive in background"
            setShowBadge(false)
        }

        val tradingChannel = NotificationChannel(
            NotificationChannels.TRADING_CHANNEL_ID,
            NotificationChannels.TRADING_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Trade execution alerts"
            enableVibration(true)
            setShowBadge(true)
        }

        nm.createNotificationChannel(serviceChannel)
        nm.createNotificationChannel(tradingChannel)
    }
}
