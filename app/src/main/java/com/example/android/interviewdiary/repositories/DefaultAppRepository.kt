package com.example.android.interviewdiary.repositories

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.AppDao
import com.example.android.interviewdiary.model.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject

class DefaultAppRepository @Inject constructor(
    private val dao: AppDao
) : AppRepository {

    /**
     * Probably needed for the back up functionality.
     */
    override fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?): Int {
        return dao.checkpoint(supportSQLiteQuery)
    }

    override suspend fun insertTracker(tracker: Tracker) {
        withContext(Dispatchers.IO) {
            dao.insertTracker(tracker)
        }
    }

    override suspend fun updateTracker(tracker: Tracker) {
        withContext(Dispatchers.IO) {
            dao.updateTracker(tracker)
        }
    }

    override suspend fun deleteTracker(tracker: Tracker) {
        withContext(Dispatchers.IO) {
            dao.deleteTracker(tracker)
        }
    }

    override suspend fun deleteTracker(trackerId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteTracker(trackerId)
        }
    }

    override suspend fun clearAllTrackers() {
        withContext(Dispatchers.IO) {
            dao.clearAllTrackers()
        }
    }

    override suspend fun getTracker(trackerId: Int): Tracker? {
        return dao.getTracker(trackerId)
    }

    override suspend fun getAllTrackers(): List<Tracker> {
        return dao.getAllTrackers()
    }

    override fun streamAllTrackerIds(): Flow<IntArray> {
        return dao.streamAllTrackerIds()
    }

    override fun streamTracker(trackerId: Int): Flow<Tracker?> {
        return dao.streamTracker(trackerId)
    }

    override fun streamAllTrackers(): Flow<List<Tracker>> {
        return dao.streamAllTrackers()
    }

    override fun streamTrackers(trackerIds: IntArray): Flow<List<Tracker?>> {
        return dao.streamTrackers(trackerIds)
    }

    override suspend fun insertRecord(record: Record) {
        withContext(Dispatchers.IO) {
            dao.insertRecord(record)
        }
    }

    override suspend fun updateRecord(record: Record) {
        withContext(Dispatchers.IO) {
            dao.updateRecord(record)
        }
    }

    override suspend fun deleteRecord(record: Record) {
        withContext(Dispatchers.IO) {
            dao.deleteRecord(record)
        }
    }

    override suspend fun deleteRecord(recordId: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteRecord(recordId)
        }
    }

    override suspend fun clearRecords(trackerId: Int) {
        withContext(Dispatchers.IO) {
            dao.clearRecords(trackerId)
        }
    }

    override suspend fun clearAllRecords() {
        withContext(Dispatchers.IO) {
            dao.clearAllRecords()
        }
    }

    override suspend fun getRecord(trackerId: Int, date: LocalDate): Record? {
        return dao.getRecord(trackerId, date)
    }

    override suspend fun getRecord(recordId: Int): Record? {
        return dao.getRecord(recordId)
    }

    override suspend fun getAllRecords(trackerId: Int): List<Record> {
        return dao.getAllRecords(trackerId)
    }

    override suspend fun getAllRecords(): List<Record> {
        return dao.getAllRecords()
    }

    override fun streamRecord(trackerId: Int?, date: LocalDate): Flow<Record?> {
        return dao.streamRecord(trackerId, date)
    }

    override fun streamAllRecords(trackerId: Int): Flow<List<Record>> {
        return dao.streamAllRecords(trackerId)
    }

    override fun streamPastRecords(trackerIds: IntArray, date: LocalDate): Flow<List<Record?>> {
        return dao.streamPastRecords(trackerIds, date)
    }

}