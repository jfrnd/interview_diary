package com.example.android.interviewdiary.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.AppDao
import com.example.android.interviewdiary.model.AppDatabase
import com.example.android.interviewdiary.repositories.DefaultAppRepository
import com.example.android.interviewdiary.repositories.AppRepository
import com.example.android.interviewdiary.other.Constants.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    // TODO Add Pixabay Api

    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
            .placeholder(R.drawable.ic_image)
            .error(R.drawable.ic_image)
    )

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext app: Context, callback: AppDatabase.Callback
    ) = Room.databaseBuilder(app, AppDatabase::class.java, DATABASE_NAME
    ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE).fallbackToDestructiveMigration().addCallback(callback).build()


    @Provides
    @Singleton
    fun provideDefaultAppRepository(
        dao: AppDao,
    ) = DefaultAppRepository(dao) as AppRepository


    @Provides
    @Singleton
    fun provideAppDao(db: AppDatabase) = db.getDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope