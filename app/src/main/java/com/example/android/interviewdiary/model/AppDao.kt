package com.example.android.interviewdiary.model

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    /**
     * Probably needed for the back up functionality.
     */
    @RawQuery
    fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery?): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracker(tracker: Tracker)

    @Update
    suspend fun updateTracker(tracker: Tracker)

    @Delete
    suspend fun deleteTracker(tracker: Tracker)

    @Query("DELETE FROM tracker_table WHERE tracker_id = :trackerId")
    suspend fun deleteTracker(trackerId: Int)

    @Query("DELETE FROM tracker_table")
    suspend fun clearAllTrackers()


    @Query("SELECT * FROM tracker_table WHERE tracker_id = :trackerId")
    suspend fun getTracker(trackerId: Int): Tracker?

    @Query("SELECT * FROM tracker_table")
    suspend fun getAllTrackers(): List<Tracker>

    @Query("SELECT ALL tracker_id FROM tracker_table")
    suspend fun getAllTrackerIds(): IntArray


    @Query("SELECT * FROM tracker_table WHERE tracker_id = :trackerId")
    fun streamTracker(trackerId: Int): Flow<Tracker?>

    @Query("SELECT * FROM tracker_table")
    fun streamAllTrackers(): Flow<List<Tracker>>

    @Query("SELECT * FROM tracker_table WHERE tracker_id IN (:trackerIds)")
    fun streamTrackers(trackerIds: IntArray): Flow<List<Tracker?>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecord(record: Record)

    @Update
    suspend fun updateRecord(record: Record)

    @Delete
    suspend fun deleteRecord(record: Record)

    @Query("DELETE FROM record_table WHERE record_id = :recordId")
    suspend fun deleteRecord(recordId: Int)

    @Query("DELETE FROM record_table WHERE tracker_id = :trackerId")
    suspend fun clearRecords(trackerId: Int)

    @Query("DELETE FROM record_table")
    suspend fun clearAllRecords()

    @Query("SELECT * FROM record_table WHERE tracker_id = :trackerId AND date = :date ")
    suspend fun getRecord(trackerId: Int, date: LocalDate): Record?

    @Query("SELECT * FROM record_table WHERE record_id = :recordID")
    suspend fun getRecord(recordID: Int): Record?

    @Query("SELECT * FROM record_table WHERE tracker_id = :trackerId ORDER BY date DESC")
    suspend fun getAllRecords(trackerId: Int): List<Record>

    @Query("SELECT * FROM record_table ORDER BY date DESC")
    suspend fun getAllRecords(): List<Record>


    // does not work properly because different devices sort the dates differently
//    @Query("SELECT * FROM record_table WHERE tracker_id = :trackerId AND date <= :date")
//    suspend fun getLatestRecord(trackerId: Int, date: LocalDate): Record?
//
//    @Query("SELECT * FROM (SELECT * FROM record_table WHERE tracker_id IN (:trackerIds) AND date <= :date ORDER BY date ASC) GROUP BY tracker_id")
//    suspend fun getLatestRecords(trackerIds: IntArray, date: LocalDate): List<Record?>


    @Query("SELECT * FROM record_table WHERE tracker_id IN (:trackerIds) AND date <= :date")
    fun streamPastRecords(trackerIds: IntArray, date: LocalDate): Flow<List<Record?>>

    @Query("SELECT * FROM record_table WHERE tracker_id = :trackerId AND date = :date ")
    fun streamRecord(trackerId: Int?, date: LocalDate): Flow<Record?>

    @Query("SELECT * FROM record_table WHERE tracker_id = :trackerId ORDER BY date DESC")
    fun streamAllRecords(trackerId: Int): Flow<List<Record>>

}