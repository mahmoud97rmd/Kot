package com.tradingapp.metatrader.app.core.trading.commands

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderCommandBus @Inject constructor() {
    private val _flow = MutableSharedFlow<OrderCommand>(extraBufferCapacity = 64)
    val flow: SharedFlow<OrderCommand> = _flow.asSharedFlow()

    fun tryEmit(cmd: OrderCommand): Boolean = _flow.tryEmit(cmd)
    suspend fun emit(cmd: OrderCommand) { _flow.emit(cmd) }
}
