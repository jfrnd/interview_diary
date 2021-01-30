package com.example.android.interviewdiary.repositories

import androidx.sqlite.db.SupportSQLiteQuery
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class FakeAppRepository : AppRepository {

    private var fakeTrackers = mutableListOf<Tracker>()
    private var fakeRecords = mutableListOf<Record>()

    private var fakeTrackersStream = flowOf(fakeTrackers)
    private var fakeRecordsStream = flowOf(fakeRecords)

    private fun updateFakeTrackersStream(trackers: MutableList<Tracker>) {
        fakeTrackersStream = flowOf(trackers)
    }

    private fun updateFakeRecordsStream(records: MutableList<Record>) {
        fakeRecordsStream = flowOf(records)
    }

    override suspend fun insertTracker(tracker: Tracker) {
        fakeTrackers.add(tracker)
        updateFakeTrackersStream(fakeTrackers)
    }

    override suspend fun updateTracker(tracker: Tracker) {
        fakeTrackers = fakeTrackers.map { currentTracker ->
            if (currentTracker.trackerId == tracker.trackerId) tracker else currentTracker
        }.toMutableList()
        updateFakeTrackersStream(fakeTrackers)
    }

    override suspend fun deleteTracker(tracker: Tracker) {
        fakeTrackers.remove(tracker)
        updateFakeTrackersStream(fakeTrackers)
    }

    override suspend fun deleteTracker(trackerId: Int) {
        fakeTrackers = fakeTrackers.filter { tracker ->
            tracker.trackerId != trackerId
        }.toMutableList()
        updateFakeTrackersStream(fakeTrackers)
    }

    override suspend fun clearAllTrackers() {
        fakeTrackers.clear()
        updateFakeTrackersStream(fakeTrackers)
    }

    override suspend fun getTracker(trackerId: Int): Tracker? {
        return fakeTrackers.find { it.trackerId == trackerId }
    }

    override suspend fun getAllTrackers(): List<Tracker> {
        return fakeTrackers
    }

    override fun streamAllTrackerIds(): Flow<IntArray> {
        return fakeTrackersStream.flatMapLatest { trackers ->
            flowOf(trackers.map { it.trackerId }.toIntArray())
        }
    }

    override fun streamTracker(trackerId: Int): Flow<Tracker?> {
        return fakeTrackersStream.flatMapLatest { trackers ->
            flowOf(trackers.find { it.trackerId == trackerId })
        }
    }

    override fun streamAllTrackers(): Flow<List<Tracker>> {
        return fakeTrackersStream
    }

    override fun streamTrackers(trackerIds: IntArray): Flow<List<Tracker?>> {
        return fakeTrackersStream.flatMapLatest { trackers ->
            flowOf(trackers.filter { trackerIds.contains(it.trackerId) })
        }
    }

    override suspend fun insertRecord(record: Record) {
        fakeRecords.add(record)
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun updateRecord(newRecord: Record) {
        fakeRecords = fakeRecords.map { currentRecord ->
            if (currentRecord.recordId == newRecord.recordId) newRecord else currentRecord
        }.toMutableList()
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun deleteRecord(record: Record) {
        fakeRecords.remove(record)
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun deleteRecord(recordId: Int) {
        fakeRecords.removeIf { it.recordId == recordId }
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun clearRecords(trackerId: Int) {
        fakeRecords.removeIf { it.trackerId == trackerId }
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun clearAllRecords() {
        fakeRecords.clear()
        updateFakeRecordsStream(fakeRecords)
    }

    override suspend fun getRecord(trackerId: Int, date: LocalDate): Record? {
        return fakeRecords.find {
            it.trackerId == trackerId && it.date == date
        }
    }

    override suspend fun getRecord(recordId: Int): Record? {
        return fakeRecords.find {
            it.recordId == recordId
        }
    }

    override suspend fun getAllRecords(trackerId: Int): List<Record> {
        return fakeRecords.filter { it.trackerId == trackerId }
    }

    override suspend fun getAllRecords(): List<Record> {
        return fakeRecords
    }

    override fun streamRecord(trackerId: Int?, date: LocalDate): Flow<Record?> {
        return fakeRecordsStream.flatMapLatest { records ->
            flowOf(records.find { it.trackerId == trackerId && it.date == date })
        }
    }

    override fun streamAllRecords(trackerId: Int): Flow<List<Record>> {
        return fakeRecordsStream.flatMapLatest { records ->
            flowOf(records.filter { it.trackerId == trackerId })
        }
    }

    override fun streamPastRecords(
        trackerIds: IntArray,
        date: LocalDate
    ): Flow<List<Record?>> {
        return fakeRecordsStream.flatMapLatest { records ->
            flowOf(records.filter { it.date.isBefore(date.plusDays(1)) })
        }
    }

    override fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?): Int {
        TODO("Not yet implemented")
    }
}
