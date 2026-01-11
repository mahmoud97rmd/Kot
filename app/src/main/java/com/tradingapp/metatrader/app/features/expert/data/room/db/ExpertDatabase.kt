package com.tradingapp.metatrader.app.features.expert.data.room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tradingapp.metatrader.app.features.expert.data.room.dao.ExpertAttachmentDao
import com.tradingapp.metatrader.app.features.expert.data.room.dao.ExpertScriptDao
import com.tradingapp.metatrader.app.features.expert.data.room.entities.ExpertAttachmentEntity
import com.tradingapp.metatrader.app.features.expert.data.room.entities.ExpertScriptEntity

@Database(
    entities = [
        ExpertScriptEntity::class,
        ExpertAttachmentEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class ExpertDatabase : RoomDatabase() {
    abstract fun expertScriptDao(): ExpertScriptDao
    abstract fun expertAttachmentDao(): ExpertAttachmentDao
}
