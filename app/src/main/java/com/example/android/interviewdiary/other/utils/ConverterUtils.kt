package com.example.android.interviewdiary.other.utils

import android.content.Context
import com.example.android.interviewdiary.R
import com.example.android.interviewdiary.model.Tracker
import com.example.android.interviewdiary.model.TrackerType
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val databaseFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

object ConverterUtil {

    fun String.toLocalDate(): LocalDate = databaseFormatter.parse(this, LocalDate::from)


    fun LocalDate.differenceToToday(): Int {
        val today = LocalDate.now()
        return when {
            this == today -> 0
            this.isBefore(today) -> -1 * Duration.between(this.atStartOfDay(), today.atStartOfDay())
                .toDays().toInt()
            this.isAfter(today) -> Duration.between(this.atStartOfDay(), today.atStartOfDay())
                .toDays().toInt()
            else -> 0
        }

    }

    fun List<Int>.toDisplayedString(tracker: Tracker): String {
        return when (tracker.type) {
            TrackerType.MULTIPLE_CHOICE -> this.map { answerId ->
                tracker.answerOptions[answerId]
            }.joinToString(", ")
            TrackerType.NUMERIC -> this.first().toString() + " " + tracker.unit
            TrackerType.TIME -> this.joinToString(":") { it.toString().padStart(2, '0') }
        }
    }


    fun LocalDate?.toDisplayedString(detailed: Boolean = false, context: Context): String {
        val tomorrow = LocalDate.now().plusDays(1)
        val today = LocalDate.now()
        val yesterday = LocalDate.now().minusDays(1)
        if (!detailed)
            return when (this) {
                null -> ""
                today -> context.resources.getString(R.string.today)
                yesterday -> context.resources.getString(R.string.yesterday)
                tomorrow -> context.resources.getString(R.string.tomorrow)
                else -> "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
            }
        else
            return when (this) {
                null -> ""
                today -> context.resources.getString(R.string.today) + ", " + "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
                yesterday -> context.resources.getString(R.string.yesterday) + ", " + "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
                tomorrow -> context.resources.getString(R.string.tomorrow) + ", " + "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
                else -> "$dayOfWeek".toLowerCase(Locale.ROOT).capitalize() + ", " + "${dayOfMonth.toString().padStart(2, '0')} $month".take(6)
            }
    }

    fun List<Int?>.toExcelString(tracker: Tracker, context: Context): List<String> {
        return if (this.isNullOrEmpty() || this.contains(null)) listOf(context.resources.getString(R.string.record_list_empty_record))
        else
            when (tracker.type) {
                TrackerType.MULTIPLE_CHOICE -> this.map { tracker.answerOptions[it]!! }
                TrackerType.NUMERIC -> listOf(this.first().toString(), tracker.unit)
                TrackerType.TIME -> listOf(this.joinToString(":") {
                    it.toString().padStart(2, '0')
                })
            }
    }


}