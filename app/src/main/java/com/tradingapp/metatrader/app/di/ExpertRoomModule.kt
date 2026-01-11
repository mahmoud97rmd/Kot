package com.tradingapp.metatrader.app.di

import android.content.Context
import androidx.room.Room
import com.tradingapp.metatrader.app.features.expert.data.room.dao.ExpertAttachmentDao
import com.tradingapp.metatrader.app.features.expert.data.room.dao.ExpertScriptDao
import com.tradingapp.metatrader.app.features.expert.data.room.db.ExpertDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExpertRoomModule {

    @Provides
    @Singleton
    fun provideExpertDb(@ApplicationContext ctx: Context): ExpertDatabase {
        return Room.databaseBuilder(ctx, ExpertDatabase::class.java, "experts.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideExpertScriptDao(db: ExpertDatabase): ExpertScriptDao = db.expertScriptDao()

    @Provides
    fun provideExpertAttachmentDao(db: ExpertDatabase): ExpertAttachmentDao = db.expertAttachmentDao()
}
