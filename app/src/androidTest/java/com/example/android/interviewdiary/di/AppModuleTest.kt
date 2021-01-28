package com.example.android.interviewdiary.di

import android.content.Context
import androidx.room.Room
import com.example.android.interviewdiary.model.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppModuleTest {

    @Provides
    @Named("test_db")
    fun provideAppDatabase(
        @ApplicationContext app: Context) = Room.inMemoryDatabaseBuilder(
        app, AppDatabase::class.java
    ).allowMainThreadQueries()
        .build()
}