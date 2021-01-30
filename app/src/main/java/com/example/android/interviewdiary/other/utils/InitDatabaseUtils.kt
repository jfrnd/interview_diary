package com.example.android.interviewdiary.other.utils

import android.content.Context
import android.net.Uri
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.Record
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import java.time.LocalDate
import kotlin.random.Random


object InitDatabaseUtils {
    fun createInitTrackers(context: Context): List<Tracker> {
        return listOf(
            Tracker(
                trackerId = 1,
                type = TrackerType.MULTIPLE_CHOICE,
                question = context.resources.getString(R.string.example_tracker_how_was_your_day),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_mood")
                    .toString(),
                configValues = listOf(0, 1, 2, 3, 4),
                answerOptions = mapOf(
                    0 to context.resources.getString(R.string.example_tracker_how_was_your_day_a_one),
                    1 to context.resources.getString(R.string.example_tracker_how_was_your_day_a_two),
                    2 to context.resources.getString(R.string.example_tracker_how_was_your_day_a_three),
                    3 to context.resources.getString(R.string.example_tracker_how_was_your_day_a_four),
                    4 to context.resources.getString(R.string.example_tracker_how_was_your_day_a_five),

                ),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 2,
                type = TrackerType.YES_NO,
                question = context.resources.getString(R.string.example_tracker_did_you_help),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_help")
                    .toString(),
                configValues = listOf(0, 1),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 3,
                type = TrackerType.NUMERIC,
                question = context.resources.getString(R.string.example_tracker_how_many_km),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_running")
                    .toString(),
                configValues = listOf(10, 0, 20),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = context.resources.getString(R.string.example_tracker_km),
            ),
            Tracker(
                trackerId = 4,
                type = TrackerType.TIME,
                question = context.resources.getString(R.string.example_tracker_how_long_run),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_running_time")
                    .toString(),
                configValues = listOf(0, 45, 0),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 5,
                type = TrackerType.TIME,
                question = context.resources.getString(R.string.example_tracker_how_long_gym),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_gym")
                    .toString(),
                configValues = listOf(1, 0, 0),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 6,
                type = TrackerType.NUMERIC,
                question = context.resources.getString(R.string.example_tracker_how_many_km_cycle),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_bike")
                    .toString(),
                configValues = listOf(0, 0, 150),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = context.resources.getString(R.string.example_tracker_km),
            ),
            Tracker(
                trackerId = 7,
                type = TrackerType.MULTIPLE_CHOICE,
                question = context.resources.getString(R.string.example_tracker_eat),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_hobby")
                    .toString(),
                configValues = listOf(0, 1, 2, 3),
                answerOptions = mapOf(
                    0 to context.resources.getString(R.string.example_tracker_eat_a_one),
                    1 to context.resources.getString(R.string.example_tracker_eat_a_two),
                    2 to context.resources.getString(R.string.example_tracker_eat_a_three),
                    3 to context.resources.getString(R.string.example_tracker_eat_a_four),
                ),
                multiSelectionEnabled = true,
                unit = "",
            ),
            Tracker(
                trackerId = 8,
                type = TrackerType.NUMERIC,
                question = context.resources.getString(R.string.example_tracker_weight),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_weight")
                    .toString(),
                configValues = listOf(75, 70, 80),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = context.resources.getString(R.string.example_tracker_kg),
            ),
            Tracker(
                trackerId = 9,
                type = TrackerType.TIME,
                question = context.resources.getString(R.string.example_tracker_how_long_sleep),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_sleep")
                    .toString(),
                configValues = listOf(7, 30, 0),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            ),
            Tracker(
                trackerId = 10,
                type = TrackerType.TIME,
                question = context.resources.getString(R.string.example_tracker_youtube),
                imageUri = Uri.parse("android.resource://" + context.packageName + "/drawable/iv_youtube")
                    .toString(),
                configValues = listOf(0, 30, 0),
                answerOptions = emptyMap(),
                multiSelectionEnabled = false,
                unit = "",
            )
        )

    }

    fun createInitRecords(trackers: List<Tracker>, startDate: LocalDate, amountEach: Long): List<Record> {
        var records = listOf<Record>()
        trackers.forEach { tracker->
            records = records + createListOfRandomRecords(tracker,startDate,amountEach)
        }
        return records
    }


    private fun createListOfRandomRecords(
        tracker: Tracker,
        startDate: LocalDate = LocalDate.now(),
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






