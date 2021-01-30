package com.example.android.interviewdiary.repositories

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AppRepository {

    /**
     * Probably needed for the back up functionality.
     */
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?): Int

    suspend fun insertTracker(tracker: Tracker)
    suspend fun updateTracker(tracker: Tracker)
    suspend fun deleteTracker(tracker: Tracker)
    suspend fun deleteTracker(trackerId: Int)

    suspend fun clearAllTrackers()

    suspend fun getTracker(trackerId: Int): Tracker?
    suspend fun getAllTrackers(): List<Tracker>
    fun streamAllTrackerIds(): Flow<IntArray>

    fun streamTracker(trackerId: Int): Flow<Tracker?>
    fun streamAllTrackers(): Flow<List<Tracker>>
    fun streamTrackers(trackerIds: IntArray): Flow<List<Tracker?>>

    suspend fun insertRecord(record: Record)
    suspend fun updateRecord(record: Record)
    suspend fun deleteRecord(record: Record)
    suspend fun deleteRecord(recordId: Int)

    suspend fun clearRecords(trackerId: Int)
    suspend fun clearAllRecords()

    suspend fun getRecord(trackerId: Int, date: LocalDate): Record?
    suspend fun getRecord(recordId: Int): Record?
    suspend fun getAllRecords(trackerId: Int): List<Record>
    suspend fun getAllRecords(): List<Record>

    fun streamRecord(trackerId: Int?, date: LocalDate): Flow<Record?>
    fun streamAllRecords(trackerId: Int): Flow<List<Record>>

    fun streamPastRecords(trackerIds: IntArray, date: LocalDate): Flow<List<Record?>>


}