package com.example.android.interviewdiary.model

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.interviewdiary.di.ApplicationScope
import com.example.android.interviewdiary.other.utils.InitDatabaseUtils.createInitRecords
import com.example.android.interviewdiary.other.utils.InitDatabaseUtils.createInitTrackers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Tracker::class, Record::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getDao(): AppDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope,
        @ApplicationContext private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().getDao()

            applicationScope.launch {
                val initTrackers = createInitTrackers(context)
                initTrackers.forEach { tracker ->
                    dao.insertTracker(tracker)
                }
                createInitRecords(initTrackers, LocalDate.now().minusDays(1), 30).forEach { record ->
                    dao.insertRecord(record)
                }
            }
        }
    }

}

