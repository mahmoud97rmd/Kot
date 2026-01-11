package com.tradingapp.metatrader.app.features.expert.data

import com.tradingapp.metatrader.app.features.expert.data.room.dao.ExpertAttachmentDao
import com.tradingapp.metatrader.app.features.expert.data.room.entities.ExpertAttachmentEntity
import com.tradingapp.metatrader.domain.models.expert.ExpertAttachment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpertAttachmentRepository @Inject constructor(
    private val dao: ExpertAttachmentDao
) {

    fun observeAll(): Flow<List<ExpertAttachment>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun getBySymbolTf(symbol: String, timeframe: String): ExpertAttachment? =
        dao.getBySymbolTf(symbol, timeframe)?.toDomain()

    /**
     * Upsert attachment for (symbol,timeframe). Unique index ensures single attachment per chart.
     */
    suspend fun attach(scriptId: String, symbol: String, timeframe: String, active: Boolean = true): ExpertAttachment {
        val now = System.currentTimeMillis()
        val existing = dao.getBySymbolTf(symbol, timeframe)
        val entity = if (existing == null) {
            ExpertAttachmentEntity(
                id = UUID.randomUUID().toString(),
                scriptId = scriptId,
                symbol = symbol,
                timeframe = timeframe,
                isActive = active,
                createdAtMs = now,
                updatedAtMs = now
            )
        } else {
            existing.copy(
                scriptId = scriptId,
                isActive = active,
                updatedAtMs = now
            )
        }

        dao.upsert(entity)
        return entity.toDomain()
    }

    suspend fun detach(symbol: String, timeframe: String) {
        dao.deleteBySymbolTf(symbol, timeframe)
    }

    suspend fun setActive(id: String, active: Boolean) {
        dao.setActive(id, active, System.currentTimeMillis())
    }

    private fun ExpertAttachmentEntity.toDomain(): ExpertAttachment =
        ExpertAttachment(
            id = id,
            scriptId = scriptId,
            symbol = symbol,
            timeframe = timeframe,
            isActive = isActive,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs
        )
}
