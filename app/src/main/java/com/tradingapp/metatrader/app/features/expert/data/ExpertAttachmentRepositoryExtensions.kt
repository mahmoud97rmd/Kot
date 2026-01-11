package com.tradingapp.metatrader.app.features.expert.data

import kotlinx.coroutines.flow.first

suspend fun ExpertAttachmentRepository.getAllNow() = observeAll().first()
