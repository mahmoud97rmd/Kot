package com.tradingapp.metatrader.app.features.oanda.transactions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionStreamManager @Inject constructor(
    private val client: OandaTransactionsStreamClient
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job != null) return
        job = scope.launch { client.runStreamLoop() }
    }

    fun stop() {
        client.stop()
        job?.cancel()
        job = null
    }
}
