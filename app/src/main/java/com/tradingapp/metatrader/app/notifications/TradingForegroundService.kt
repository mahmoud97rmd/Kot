package com.tradingapp.metatrader.app.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tradingapp.metatrader.app.R
import com.tradingapp.metatrader.domain.models.trading.TradingEvent
import com.tradingapp.metatrader.domain.usecases.trading.ObserveTradingEventsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class TradingForegroundService : Service() {

    @Inject lateinit var observeTradingEvents: ObserveTradingEventsUseCase

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        startForeground(FOREGROUND_ID, buildForegroundNotification())

        scope.launch(Dispatchers.IO) {
            observeTradingEvents().collectLatest { ev ->
                when (ev) {
                    is TradingEvent.PositionOpened -> {
                        postTradingAlert(
                            title = "Trade Opened",
                            text = "${ev.position.side.name} ${ev.position.instrument} @ ${fmt(ev.position.entryPrice)}"
                        )
                    }
                    is TradingEvent.PendingTriggered -> {
                        postTradingAlert(
                            title = "Pending Triggered",
                            text = "Order ${short(ev.orderId)} → Position ${short(ev.openedPositionId)}"
                        )
                    }
                    is TradingEvent.PositionClosed -> {
                        postTradingAlert(
                            title = "Trade Closed",
                            text = "${ev.trade.side.name} ${ev.trade.instrument} P/L ${fmt(ev.trade.profit)}"
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, NotificationChannels.SERVICE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("Market streaming active")
            .setContentText("Running in background for live prices & alerts")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun postTradingAlert(title: String, text: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val n = NotificationCompat.Builder(this, NotificationChannels.TRADING_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(nextId(), n)
    }

    private fun nextId(): Int = (abs(System.currentTimeMillis()) % Int.MAX_VALUE).toInt()

    private fun fmt(x: Double): String = String.format("%.2f", x)

    private fun short(id: String): String = if (id.length <= 6) id else id.take(6)

    companion object {
        private const val FOREGROUND_ID = 1001

        fun start(context: Context) {
            val i = Intent(context, TradingForegroundService::class.java)
            context.startForegroundService(i)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TradingForegroundService::class.java))
        }
    }
}
