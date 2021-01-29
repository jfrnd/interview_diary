package com.example.android.interviewdiary.other.utils

import android.content.Context
import android.net.Uri
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.random.Random


object InitTestDatabaseUtils {

    fun createFakeTrackers(): List<Tracker> {
        return listOf(
            Tracker(
                trackerId = 1,
                type = TrackerType.MULTIPLE_CHOICE,
                question = "Q1",
                imageUri = "Q1",
                configValues = listOf(0, 1, 2, 3, 4),
                answerOptions = mapOf(
                    0 to "A1",
                    1 to "A2",
                    2 to "A3",
                    3 to "A4",
                    4 to "A5",
                ),
                multiSelectionEnabled = true,
                unit = "",
            ),
            Tracker(
                trackerId = 2,
                type = TrackerType.MULTIPLE_CHOICE,
                question = "Q2",
                imageUri = "Q2",
                configValues = listOf(0, 1),
                answerOptions = mapOf(
                    0 to "A0",
                    1 to "A1"
                ),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 3,
                type = TrackerType.NUMERIC,
                question = "Q3",
                imageUri = "Q3",
                configValues = listOf(10, 0, 20),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "km",
            ),
            Tracker(
                trackerId = 4,
                type = TrackerType.TIME,
                question = "Q4",
                imageUri = "Q4",
                configValues = listOf(0, 45, 0),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            ),
        )
    }

    fun createFakeRecords(
        trackers: List<Tracker>,
        startDate: LocalDate,
//        startDate: OffsetDateTime,
        amountEach: Long
    ): List<Record> {
        var records = listOf<Record>()
        trackers.forEach { tracker ->
            records = records + createListOfRandomRecords(tracker, startDate, amountEach)
        }
        return records
    }


    private fun createListOfRandomRecords(
        tracker: Tracker,
        startDate: LocalDate = LocalDate.now(),
//        startDate: OffsetDateTime = OffsetDateTime.now(),
        amount: Long = 1,
        notes: List<String> = listOf(""),
    ): List<Record> {
        if (amount < 2) return listOf(createRandomRecord(tracker, startDate))
        val records = arrayListOf<Record>()

        for (i in 0..amount) {
            records.add(createRandomRecord(tracker, startDate.minusDays(i), notes.shuffled()[0]))
        }
        return records
    }


    private fun createRandomRecord(
        tracker: Tracker,
        date: LocalDate = LocalDate.now(),
//        date: OffsetDateTime = OffsetDateTime.now(),
        note: String = "",
    ): Record {
        return when {
            tracker.type == TrackerType.MULTIPLE_CHOICE && !tracker.multiSelectionEnabled -> Record(
                trackerId = tracker.trackerId,
                date = date,
                note = note,
                values = tracker.configValues.shuffled().take(1) as List<Int>
            )
            tracker.type == TrackerType.MULTIPLE_CHOICE && tracker.multiSelectionEnabled -> {
                Record(
                    trackerId = tracker.trackerId,
                    date = date,
                    note = note,
                    values = tracker.configValues.shuffled()
                        .take(Random.nextInt(3)) as List<Int>
                )
            }
            tracker.type == TrackerType.TIME -> Record(
                trackerId = tracker.trackerId,
                date = date,
                note = note,
                values = listOf(
                    tracker.configValues[0]!! + Random.nextInt(2),
                    Random.nextInt(60),
                    0
                )
            )
            else -> Record(
                trackerId = tracker.trackerId,
                date = date,
                note = note,
                values = listOf(
                    Random.nextInt(
                        tracker.configValues[1]!!,
                        tracker.configValues[2]!!
                    )
                )
            )
        }
    }

}






