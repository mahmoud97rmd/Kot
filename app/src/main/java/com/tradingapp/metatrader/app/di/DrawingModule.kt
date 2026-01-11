package com.tradingapp.metatrader.app.di

import android.content.Context
import androidx.room.Room
import com.tradingapp.metatrader.data.local.drawing.DrawingDatabase
import com.tradingapp.metatrader.data.local.drawing.DrawingRepositoryImpl
import com.tradingapp.metatrader.domain.repository.DrawingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DrawingModule {

    @Provides
    @Singleton
    fun provideDrawingDb(@ApplicationContext context: Context): DrawingDatabase {
        return Room.databaseBuilder(context, DrawingDatabase::class.java, "drawing_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDrawingRepository(db: DrawingDatabase): DrawingRepository {
        return DrawingRepositoryImpl(db.dao())
    }
}
