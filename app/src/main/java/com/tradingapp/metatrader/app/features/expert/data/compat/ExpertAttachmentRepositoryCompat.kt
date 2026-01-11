package com.tradingapp.metatrader.app.features.expert.data.compat

import com.tradingapp.metatrader.app.features.expert.data.ExpertAttachmentRepository
import com.tradingapp.metatrader.app.features.expert.data.model.ExpertAttachment
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * If your repository already has getAllNow(), ignore this file.
 * Otherwise you can call compat.getAllNow() anywhere.
 */
@Singleton
class ExpertAttachmentRepositoryCompat @Inject constructor(
    private val repo: ExpertAttachmentRepository
) {
    suspend fun getAllNow(): List<ExpertAttachment> = repo.observeAll().first()
}
